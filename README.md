# QA_Automation_SDDK_GKTS_Case_Study

> **Production-Ready Cucumber BDD + Selenium + REST Assured — Hooks ve Step Definitions ayrı dosyalarda**

[![CI/CD](https://github.com/YOUR_USERNAME/insider-qa-automation/actions/workflows/bdd-automation.yml/badge.svg)](https://github.com/YOUR_USERNAME/insider-qa-automation/actions)

---

## Teknoloji Stack

| Katman | Araç | Versiyon |
|--------|------|----------|
| Dil | Java | 11 |
| Build | Maven | 3.8.1+ |
| BDD Framework | Cucumber | 7.14.0 |
| UI Testing | Selenium | 4.15.0 |
| API Testing | REST Assured | 5.4.0 |
| Test Runner | TestNG | 7.10.2 |
| Reporting | Allure + Cucumber HTML | 2.25.0 |
| Logging | SLF4J + Logback | 2.0.11 |
| Load Testing | Apache JMeter | 5.6.3 |
| CI/CD | GitHub Actions | - |

---

## Proje Yapısı

```
src/test/
├── java/com/insider/
│   │
│   ├── context/
│   │   └── ScenarioContext.java          ← Step'ler arası ThreadLocal veri paylaşımı
│   │
│   ├── hooks/                            ← @Before / @After — ayrı dosyalar
│   │   ├── UIHooks.java                  ← UI: Driver başlat/kapat, screenshot
│   │   └── APIHooks.java                 ← API: RestAssured konfigüre, response log
│   │
│   ├── ui/
│   │   ├── pages/                        ← Page Object Model
│   │   │   ├── BasePage.java
│   │   │   ├── HomePage.java
│   │   │   ├── CareersPage.java
│   │   │   └── JobListingPage.java
│   │   └── steps/                        ← UI Step Definitions — ayrı dosyalar
│   │       ├── BaseSteps.java            ← Ortak step yardımcıları
│   │       ├── HomePageSteps.java        ← Ana sayfa adımları
│   │       ├── CareersPageSteps.java     ← Kariyer sayfası + filtreleme adımları
│   │       └── JobApplicationSteps.java  ← Apply butonu + Lever doğrulama
│   │
│   ├── api/
│   │   ├── models/
│   │   │   └── Pet.java
│   │   ├── services/
│   │   │   └── PetStoreAPI.java          ← Singleton CRUD servisi
│   │   └── steps/                        ← API Step Definitions — ayrı dosyalar
│   │       ├── PetCrudSteps.java         ← Create, Read, Update, Delete, FindByStatus
│   │       └── PetAssertionSteps.java    ← Status, name, list assertion'ları
│   │
│   ├── runners/
│   │   └── CucumberTestRunner.java       ← TestNG + Cucumber runner
│   │
│   └── utils/
│       ├── DriverManager.java            ← Singleton + ThreadLocal WebDriver
│       ├── ConfigReader.java             ← Singleton config okuyucu
│       ├── WaitHelper.java               ← Explicit wait yardımcısı
│       ├── ScreenshotUtil.java           ← Hata anı ekran görüntüsü
│       └── ReportListener.java           ← TestNG listener
│
└── resources/
    ├── features/
    │   ├── ui_careers.feature            ← Kariyer sayfası UI senaryoları
    │   ├── ui_AI_lever_filters.feature   ← AI destekli Lever filtre senaryoları
    │   └── api_petstore.feature          ← PetStore API senaryoları
    ├── config.properties
    ├── logback.xml
    └── testng.xml

load-tests/
├── n11_search_load_test.jmx              ← JMeter test planı
├── n11_search_scenarios.md               ← Senaryo dokümantasyonu
└── results/
    ├── results.jtl                       ← Ham sonuç dosyası
    └── 20260530-n11_search_load_test.csv ← Tarihli CSV raporu
```

---

## Hook Mimarisi

```
@Before(order=10, value="@ui")   →  UIHooks.setUpUI()
  ├── ScenarioContext.clear()
  ├── DriverManager.getInstance().initDriver()
  └── Page Objects → ScenarioContext'e yaz

@Before(order=10, value="@api")  →  APIHooks.setUpAPI()
  ├── ScenarioContext.clear()
  ├── RestAssured.baseURI = config'den oku
  └── PetStoreAPI.getInstance() → ScenarioContext'e yaz

@After(order=10, value="@ui")    →  UIHooks.tearDownUI()
  ├── scenario.isFailed() → screenshot al + rapora ekle
  ├── DriverManager.quitDriver()
  └── ScenarioContext.remove()

@After(order=10, value="@api")   →  APIHooks.tearDownAPI()
  ├── scenario.isFailed() → son response'u rapora ekle
  ├── RestAssured.reset()
  └── ScenarioContext.remove()
```

---

## Kurulum

```bash
mvn clean install -DskipTests
```

---

## Testleri Çalıştır

```bash
# Tüm senaryolar
mvn clean test

# Sadece UI
mvn clean test -Dcucumber.filter.tags="@ui"

# Sadece API
mvn clean test -Dcucumber.filter.tags="@api"

# Smoke
mvn clean test -Dcucumber.filter.tags="@smoke"

# Regression
mvn clean test -Dcucumber.filter.tags="@regression"

# Headless (CI/CD)
mvn clean test -Dbrowser.headless=true

# Paralel (4 thread)
mvn test -Dcucumber.threads=4
```

### Load Testleri Maven ile Çalıştır

```bash
# Load testleri çalıştır + HTML raporu otomatik üret ve aç
mvn clean verify -P load-test

# Sadece load testleri (Cucumber testleri olmadan)
mvn verify -P load-test -DskipTests
```

---

## Load Testler (JMeter)

### Genel Bakış

Load testler Apache JMeter 5.6.3 kullanılarak hazırlanmıştır. Hedef: **n11.com arama modülünün** performansını gerçek tarayıcı başlıkları eşliğinde ölçmek.

Test planı ([load-tests/n11_search_load_test.jmx](load-tests/n11_search_load_test.jmx)) üç senaryo içerir:

| Senaryo | Açıklama | Thread | Loop |
|---------|----------|--------|------|
| Senaryo 1 | Tek kullanıcı baseline | 1 | 10 |
| Senaryo 2 | Parametrik arama (farklı terimler) | 1 | 5/terim |
| Senaryo 3 | Sayfalama performansı | 1 | Sıralı |

### Ön Koşullar

- Apache JMeter 5.6.3+ kurulu olmalı
- `JMETER_HOME` ortam değişkeni tanımlanmış olmalı **veya** `jmeter` komutu PATH'te bulunmalı

```bash
# macOS (Homebrew)
brew install jmeter

# Manuel kurulum
export JMETER_HOME=/opt/jmeter
export PATH=$JMETER_HOME/bin:$PATH
```

### Load Testleri Çalıştır

```bash
# GUI mod — lokal geliştirme / plan görüntüleme
jmeter -t load-tests/n11_search_load_test.jmx

# Non-GUI mod — CI/CD veya lokal çalıştırma (önerilen)
jmeter -n \
  -t load-tests/n11_search_load_test.jmx \
  -l load-tests/results/results.jtl \
  -e -o load-tests/html-report/

# Sonuçtan HTML rapor oluştur (mevcut .jtl dosyasından)
jmeter -g load-tests/results/results.jtl \
       -o load-tests/html-report/
```

> **Not:** `-n` bayrağı GUI'siz (headless) çalıştırır. `-e -o` raporu otomatik üretir.

### Test Akışı

```
JMeter Başla
  └── HTTP Request Defaults (BASE_URL: www.n11.com, PROTOCOL: https)
  └── HTTP Header Manager    (User-Agent, Accept-Language: tr-TR, ...)
  └── HTTP Cookie Manager    (oturum çerezleri)
  └── HTTP Cache Manager     (önbellek simülasyonu)
  │
  ├── Thread Group: 1 Kullanıcı
  │   ├── 01_Homepage     → GET /
  │   ├── 02_Search       → GET /arama?q=laptop
  │   ├── Response Assertion  → HTTP 200 beklenir
  │   └── Duration Assertion  → < 3000ms beklenir
  │
  └── Listeners
      ├── Summary Report
      └── results.jtl (CSV)
```

### Beklenen Performans Eşikleri

| Metrik | Hedef |
|--------|-------|
| Ortalama Yanıt Süresi | < 3000ms |
| 90. Persentil | < 5000ms |
| Hata Oranı | < %1 |
| Throughput | ≥ 1 istek/sn |

### Sonuçları Görüntüle

Rapor [load-tests/html-report/](load-tests/html-report/) dizinine üretilir. Tarayıcıda açmak için:

```bash
open load-tests/html-report/index.html          # macOS
xdg-open load-tests/html-report/index.html      # Linux
start load-tests/html-report/index.html         # Windows
```

---

## Raporlar

```bash
mvn allure:serve
mvn net.masterthought:maven-cucumber-reporting:generate
```

---

## Design Patterns

| Pattern | Kullanım |
|---------|---------|
| Singleton | DriverManager, ConfigReader, PetStoreAPI |
| ThreadLocal Singleton | DriverManager (paralel test uyumlu) |
| Page Object Model | HomePage, CareersPage, JobListingPage |
| BDD | Feature files + ayrı Step Definition sınıfları |
| Context Object | ScenarioContext (step'ler arası veri aktarımı) |

---

## CI/CD

GitHub Actions — her gün UTC 01:00, push ve PR'da çalışır.
Ayrı job'lar: `ui-tests`, `api-tests`, `full-regression`
