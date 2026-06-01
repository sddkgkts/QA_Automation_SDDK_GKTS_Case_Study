

### 1. Context, prompt, skill ve agent terimleri

**Context** AI'ya "şunu yaz" demeden önce ona vermek istediğim arka plan bilgileridir: mevcut kod dosyaları, proje yapısı, framework versiyonları, dokumentasyon. Bu bilgiler AI'nın görevimi daha iyi anlamasını sağlar. Rolünün ne olduğunu ve sınırlarını belirleyen bilgisel temeldir.

**Prompt** Context'in içinde verdiğim açık ve detaylı talimattır. Mümkün olduğunca detay veririm; eğer yeni kod ekleyeceksem mevcut kodu incelemesini, framework'ü anlamasını ve verdiğim görevin tam detaylarını içeren net bir talimat hazırlarım.

**Skill** AI'nın önceden eğitildiği ve sahip olduğu belirli yeteneklerdir. Context ve Prompt'ta ne kadar detay verirsem, AI bu skill'leri o kadar etkili şekilde kullanabilir. 

**Agent** AI'nın sahip olduğu skill'leri kullanarak otonom olarak hareket eden yapıdır. Framework'ün bir yerinde yaptığım değişikliği anlar ve gerekli diğer alanları otomatik olarak güncelleyebilir. Sorunu çözmek için adımlar planlayıp sırayla gerçekleştirir, hata varsa düzeltir. Tek komutla çoklu görevleri başlatabilir.




### 2. AI çıktısını nasıl doğruladın? Hangi kısımları reddeddin ve neden?

1- Kod değişikliklerinden sonra önce `mvn test-compile` ile derleme hatası olmadığını kontrol ettim,
2- sayfa açıldığında tüm cerezleri kabul edilmesi adımını ekledim.
3- Test frameworkünü çalıştırarak gerçek sayfada da geçip geçmediğine baktım — ikisi de geçmeden "tamam" demiyorum. 
4- Testleri çalıştırdığımda sayfanınn tümü yüklendiğini kontorol etmiyordu - sayfa sonuna kadar scroll down yaparak sayfanın tümünün yüklendiğini kontrol etmesini sağladım
5- Careers sayfasına giderken direk URL'i yazıp navigate ediyordu bu adımıda kabul etmedim, home page de footer kısmından "We're hiring" ifadesini tıklayarak gitmesini sağladım,



### 3. AI'nın zayıf kaldığı bir noktayı ve bunu nasıl manuel çözdüğünü anlat.

AI locatorları oluşturuken bazen hatalı locatorlar şecebiliryor, bu durumda unique olan locatorları oluşturup manuel olarak ekledim "Filter selector'larını yazarken AI `[aria-label*='Filter by Location']` kullandı ama bu ifade sayfadaki iki farklı wrapper'ı birden yakalar: biri "Location type", diğeri "Location". Bunu fark ettim çünkü HTML'e bakıp gerçek `aria-label` değerlerini okudum; ikisi arasındaki tek fark iki nokta (:) karakteriydi ve `[aria-label*='Filter by Location:']` yazınca sadece doğru olanı seçti. AI verdiğim HTML'i tam anlamıyla parse etmemişti, ben de bunu elle düzelttim."



### 4. Hangi durumlarda AI yerine manuel kod yazmayı tercih edersin?

Canlı tarayıcıda görmeden emin olamayacağım şeyleri hep kendim yazıyorum 
1- URL encoding, JS ile geç yüklenen elementler, üçüncü taraf widget'ların gerçek DOM yapısı. 
2- Bu projede de AI form sayfasının selector'ını üç farklı şekilde tahmin etti, üçü de çalışmadı; sayfayı açıp `<h4>Submit your application</h4>` elementini kendi gözümle görünce çözdüm. Bunun yanında assertion mantığını da kendim kontrol etmeyi tercih ettim: AI `verifyEachJobHasPositionTitle` metoduna sadece `count > 0` yazmıştı, isim doğru ama içi boştu — bunu ancak "bu test ne kanıtlamalı" diye sorarak anlarsın.
