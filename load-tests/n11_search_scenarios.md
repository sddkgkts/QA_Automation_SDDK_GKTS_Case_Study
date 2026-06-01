# n11.com Arama Modülü - Yük Testi Senaryoları

## Genel Açıklama

Bu dosya n11.com arama modülü için yük testi senaryolarını içerir.
Araç: Apache JMeter

---

## Test Senaryoları

### Senaryo 1: Tek Kullanıcı Arama Akışı (Temel)

**Amaç:** Arama endpoint'inin 1 kullanıcıyla baseline performansını ölçmek.

**Adımlar:**
1. n11.com ana sayfasını aç (GET https://www.n11.com/)
2. Arama kutusuna "laptop" yaz
3. Arama formunu gönder (GET https://www.n11.com/arama?q=laptop)
4. Sonuç sayfasını doğrula (200 OK, response time < 3s)

**JMeter Konfigürasyonu:**
```
Thread Count: 1
Ramp-Up Period: 1 second
Loop Count: 10
```

**Beklenen Metrikler:**
- Average Response Time: < 3000ms
- Error Rate: < 1%
- Throughput: > 1 req/sec

---

### Senaryo 2: Farklı Arama Terimleriyle Parametrik Test

**Amaç:** Farklı sorgu türlerinin performans etkisini test etmek.

**Arama Terimleri:**
| Terim | Kategori |
|-------|----------|
| laptop | Elektronik |
| ayakkabı | Giyim |
| iphone 15 | Telefon |
| kahve makinesi | Ev Aletleri |
| kitap | Kültür |

**JMeter Konfigürasyonu:**
```
Thread Count: 1
CSV Data Set Config: search_terms.csv
Loop Count: 5 (per term)
```

---

### Senaryo 3: Sayfalama Testi

**Amaç:** Arama sonuçları sayfalama performansını test etmek.

**Adımlar:**
1. Arama yap: GET /arama?q=telefon
2. Sayfa 2'ye git: GET /arama?q=telefon&pg=2
3. Sayfa 3'e git: GET /arama?q=telefon&pg=3
4. Filtre uygula: GET /arama?q=telefon&pg=1&srt=OP

**JMeter Konfigürasyonu:**
```
Thread Count: 1
Sequential requests with 1s think time
```

---

## JMeter Test Planı (XML) - Temel Yapı

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testname="n11 Search Load Test" enabled="true">
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">https://www.n11.com</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- Thread Group: 1 User -->
      <ThreadGroup guiclass="ThreadGroupGui" testname="1 User Search" enabled="true">
        <intProp name="ThreadGroup.num_threads">1</intProp>
        <intProp name="ThreadGroup.ramp_time">1</intProp>
        <intProp name="ThreadGroup.num_loops">10</intProp>
      </ThreadGroup>
      <hashTree>
        <!-- HTTP Request: Homepage -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testname="01_Homepage" enabled="true">
          <stringProp name="HTTPSampler.domain">www.n11.com</stringProp>
          <stringProp name="HTTPSampler.protocol">https</stringProp>
          <stringProp name="HTTPSampler.path">/</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
        </HTTPSamplerProxy>

        <!-- HTTP Request: Search -->
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testname="02_Search_Laptop" enabled="true">
          <stringProp name="HTTPSampler.domain">www.n11.com</stringProp>
          <stringProp name="HTTPSampler.protocol">https</stringProp>
          <stringProp name="HTTPSampler.path">/arama</stringProp>
          <stringProp name="HTTPSampler.method">GET</stringProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="q" elementType="HTTPArgument">
                <stringProp name="Argument.name">q</stringProp>
                <stringProp name="Argument.value">laptop</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
        </HTTPSamplerProxy>

        <!-- Response Assertion -->
        <ResponseAssertion testname="Assert 200 OK">
          <collectionProp name="Asserion.test_strings">
            <stringProp name="49586">200</stringProp>
          </collectionProp>
          <intProp name="Assertion.test_type">8</intProp>
        </ResponseAssertion>

        <!-- Duration Assertion: < 3 seconds -->
        <DurationAssertion testname="Assert Response Time < 3s">
          <longProp name="DurationAssertion.duration">3000</longProp>
        </DurationAssertion>

        <!-- Listeners -->
        <ResultCollector testname="Summary Report">
          <objProp>
            <name>saveConfig</name>
            <value class="SampleSaveConfiguration">
              <time>true</time>
              <latency>true</latency>
              <responseCode>true</responseCode>
            </value>
          </objProp>
          <stringProp name="filename">target/jmeter-results/results.jtl</stringProp>
        </ResultCollector>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

---

## Çalıştırma Komutları

```bash
# JMeter'i indir ve çalıştır (GUI mod - lokal test)
./jmeter -t load-tests/n11_search_load_test.jmx -l results.jtl

# Non-GUI mod (CI/CD için)
./jmeter -n -t load-tests/n11_search_load_test.jmx -l results.jtl -e -o report/

# Rapor oluştur
./jmeter -g results.jtl -o report/
```

---

## Beklenen Sonuçlar (1 Kullanıcı)

| Metrik | Hedef |
|--------|-------|
| Average Response Time | < 3000ms |
| 90th Percentile | < 5000ms |
| Error Rate | < 1% |
| Throughput | ≥ 1 req/s |
