# Iteration 02 — Kod Review


## Prompt

Role: Senior QA Automation Engineer
Görev: Tüm frameworkdaki kodları review et:
- Kod dublication olmasın
- Code mimarisi POM'e uygun olduğunu kontrol et
- Clean, DRY koda uygun olduğunu doğrula
- Kod okunabilir ve bakımı kolay olması için
  tüm değişkenlerin config.properties dosyasında
  kontrol edildiğini doğrula



## Çıktı Değerlendirmesi

**Kabul ettiklerim:**

Genel yapı beklendiği gibi çıktı. Page class'ları `BasePage`'i extend ediyor, locator'lar page seviyesinde tanımlanmış, step class'ları doğrudan Selenium'a dokunmuyor. WaitHelper kullanımı merkezi ve tutarlıydı. Logger her step ve page metodunda mevcuttu. Bunları olduğu gibi bıraktım.

**Düzelttiklerim:**

*Kod dublicasyonu:*
`openInsiderHomepage` adımı sayfa URL'sini kontrol ediyordu, hemen arkasından `waitForPageLoad` adımı aynı modülleri (LOGO, HERO, FOOTER) tekrar bekleyip assert ediyordu. İkisini tek adımda birleştirdim. Aynı şekilde `navigateToCareers()` sayfa sonuna kadar scroll yapıyordu, oysa bir önceki adım bunu zaten yapıyordu; ikinci scroll'u kaldırdım.

*POM mimarisi:*
`clickButton` step'inde başlangıçta `WebDriver`, `WaitHelper` ve `By` doğrudan step class içinde kullanılıyordu. Bunu `BasePage.clickButtonByText()` metoduna taşıdım. Birden fazla sayfada kullanılan bu adımı da `BaseSteps` adında ayrı bir class'a çıkardım, böylece tek bir step definition hem kariyer akışında hem başvuru akışında çalışıyor.

*Hardcoded assertion'lar:*
`areAllJobsQA()`, `areAllDepartmentsQA()`, `areAllLocationsIstanbul()` gibi üç ayrı metot tamamen hardcoded değerler içeriyordu. Bunları tek bir `allJobsContainInField(String expected, String field)` metoduyla değiştirdim. Cucumber step'lerini `{string}` parametrik yaptım; artık Gherkin'deki ifade değiştiğinde kod değişmeden çalışıyor. Virgülle ayrılmış birden fazla terim de OR mantığıyla destekleniyor.

*Kullanılmayan kod:*
`createPetWithDetails()`, `waitForElementToBeInvisible()`, `waitUntilVisible()`, `waitForElementsToBeVisible()`, `isHomePageDisplayed()`, `areTeamCardsVisible()`, `navigateToQaIstanbulJobsPage()` gibi metodları ve bunlara ait sabit ve import'ları temizledim. Feature dosyasında tanımı olmayan iki adımı da (`Insider logo is visible`, `Hero section is displayed`) kaldırdım.

*Config.properties:*
`waitUntilCondition(..., 5)` olarak hardcoded olan tek timeout değeri `ConfigReader`'a bağladım. Kullanılmayan 19 property'yi (`test.*`, `cucumber.*`, `allure.*`, `log.level`, `log.file.path`, `environment` vb.) config dosyasından çıkardım.

*Senaryo akışı:*
Filter testi sırasında Location filtresi uygulanıyor ama reset edilmeden Team filtresine geçiliyordu; kirli state sorunu vardı. Location reset adımını ve URL kontrolünü ekledim. `The job list is displayed after filtering` container görünürlüğü yerine gerçek iş ilanı sayısını kontrol eden `The job count is greater than zero` assertion'ıyla değiştirdim.


## İterasyon Notları

Review sonunda en çok dikkatimi çeken şey, başlangıçta iyi niyetle yazılmış ama zaman içinde devre dışı kalan metodların birikmesiydi. Özellikle assertion metodlarındaki hardcoding — `areAllJobsQA()` gibi — sonraki geliştirmelerde test değeri değiştiğinde sessizce yanlış sonuç verebilirdi. `{string}` parametrik yapıya geçmek hem DRY hem de kırılganlık açısından en önemli düzeltme oldu.

POM tarafında step class'larının page method'larını doğrudan çağırmasında bir sorun yoktu ama `clickButton` gibi utility niteliğindeki adımların `BaseSteps`'te toplanması ileride eklenecek adımlar için temiz bir yer sağladı.
