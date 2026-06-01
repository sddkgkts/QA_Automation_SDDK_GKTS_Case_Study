# Iteration 01 — AI Senaryosu: Lever Filter Validation


## Prompt
```
Role: Senior QA Automation Engineer
Görev: Mevcut framework'ü incele ve yapısına uygun, clean ve DRY prensiplerini
izleyerek aşağıdaki senaryoyu ekle:

Home page URL ile navigate et, sayfanın tam yüklenmesini bekle, footer'a scroll
yap, "We're Hiring" linkine tıkla ve careers sayfasına geç. "See all teams"'e
tıkla, açılan kartlardan "Quality Assurance" ekibinin "Open Positions" linkine
tıkla ve iş ilanlarının bulunduğu Lever sayfasına git. Bu sayfadaki her bir
filter dropdown'ının (Work type, Location type, Location, Team) doğru çalıştığını
doğrulayan adımlar ekle.

Yeni adımların özellikleri:
- Duplicate olmasın
- Parametrik olsun; hangi filtre ve değer verilirse o çalışsın
- Her filter için URL değişikliği kontrol edilsin
```


## Çıktı Değerlendirmesi

**Kabul ettiklerim:**

Kariyer sayfasına ulaşana kadarki navigation için mevcut step'leri kullandı, yeniden yazmadı. Filter işlemleri için oluşturduğu `applyFilter(filterName, optionText)` page metodunu ve Cucumber adımlarını (`Apply {string} filter with option {string}`, `The URL contains filter parameter {string}`) yapısal olarak beğendim, kabul ettim. Yeni class'lar, import'lar ve `@ui @ai-generated` tag yapısını olduğu gibi bıraktım.

**Düzelttiklerim:**

- Filter dropdown'larını bulmak için önerdiği locator'lar statik CSS sınıfına dayanıyordu. Bunları `[aria-label*='Filter by {filterName}']` formatında dinamik aria-label tabanlı locator'a çevirdim; böylece hangi filtre adı Gherkin'den gelirse selector kendiliğinden oluşuyor.

- Bekleme stratejisi implicit wait olarak gelmişti. Tümünü explicit wait ile değiştirdim: dropdown'ın `aria-expanded="true"` olmasını bekleyen `waitUntilCondition`, sayfa geçişleri için `waitForPageLoad`.

- Location selector `[aria-label*='Filter by Location']` olarak gelmişti ama bu hem "Location" hem de "Location type" dropdown'ını yakalıyordu. HTML'e bakınca aria-label değerlerinin `"Filter by Location:"` ve `"Filter by Location type:"` şeklinde yazıldığını gördüm. `filterAriaSelector()` metodunda "Location" için sona `:` ekleyerek belirsizliği çözdüm.

- Work type seçeneği `"Full-Time"` olarak gelmişti. URL encoding testini de kapsasın diye `"Full-Time (Remote)"` olarak değiştirdim; parantez karakterleri URL'de encode edildiği için decoder'dan geçirerek karşılaştırma yapılıyor.

- `selectTeamByName` metodu içeride doğrudan `jobs.lever.co?team=...&location=Istanbul` URL'ine gidiyordu. Bunun URL navigasyonu olduğunu fark edince DOM'daki takım kartından XPath ile `Open Positions` linkini bulup tıklayan gerçek E2E akışa geçirdim.

- Filter senaryosunda Location filtresi uygulandıktan sonra reset adımı yoktu; Team filtresi kirli state üzerinde test ediliyordu. Her filter bloğunun kendi reset + URL kontrolüyle kapanması gerektiğini gördüm, Location için de reset adımını ekledim.

- Filter sonrası assertion olarak `The job list is displayed after filtering` container görünürlüğünü kontrol ediyordu. Gerçek iş ilanı yüklenip yüklenmediğini anlamak için `The job count is greater than zero` assertion'ına geçtim; tüm filter bloklarında tutarlı hale getirdim.

- Navigation akışında `openInsiderHomepage` adımı sayfayı zaten yükleyip scroll yapıyordu, hemen ardından `navigateToCareers()` tekrar `scrollToBottomAndWaitForLazyContent()` çağırıyordu. Çift scroll olduğunu belirtince ikincisini kaldırdım.


## İterasyon Notları

En çok düzeltme gerektiren yer locator stratejisiydi. AI, sayfanın Lever embed'inden oluştuğunu tam kavrayamadığından genel CSS sınıflarıyla gitmeye çalıştı. Gerçek HTML'e bakınca aria-label'ların gayet tutarlı bir yapıda olduğunu gördüm; bu yüzden aria tabanlı dinamik selector daha temiz bir çözüm oldu. Location selector belirsizliği de aynı sebepten kaynaklandı: Lever'ın iki ayrı "Location" başlıklı filter'ı var ve AI bunları ayırt edemedi.
