# Konum Kontrolü Test Rehberi

## Yöntem 1: Mock Location (Sahte Konum) - ÖNERİLEN ✅

### Adımlar:

1. **Developer Options'ı Aç:**
   - Ayarlar → Telefon Hakkında → Yapı Numarası'na 7 kez tıkla
   - "Geliştirici oldunuz" mesajı görünecek

2. **Mock Location App İndir:**
   - Google Play'den "Mock Location" veya "Fake GPS Location" uygulaması indir
   - Örnek: "Fake GPS Location" (Lexa)

3. **Mock Location'ı Aktif Et:**
   - Ayarlar → Geliştirici Seçenekleri → Mock Location App
   - İndirdiğin uygulamayı seç

4. **Test Senaryoları:**

   **Senaryo A: Yakın Konum (Başarılı Test)**
   - Öğretmen olarak QR kod oluştur (gerçek konumun kaydedilir)
   - Mock Location uygulamasını aç
   - Aynı konumu seç (veya çok yakın, 10-20 metre)
   - Öğrenci olarak QR kod okut
   - ✅ **Beklenen:** Yoklama alınmalı

   **Senaryo B: Uzak Konum (Başarısız Test)**
   - Öğretmen olarak QR kod oluştur
   - Mock Location uygulamasını aç
   - Farklı bir şehir/konum seç (örn: 100+ metre uzak)
   - Öğrenci olarak QR kod okut
   - ❌ **Beklenen:** "Konum uygun değil" hatası

   **Senaryo C: Sınırda Konum (30 metre)**
   - Öğretmen konumu: 39.123456, 35.123456
   - Mock Location: 39.123456, 35.123456 + 0.00027 (yaklaşık 30 metre)
   - Test et: Başarılı olmalı
   - Mock Location: 39.123456, 35.123456 + 0.0003 (yaklaşık 33 metre)
   - Test et: Başarısız olmalı

### Konum Koordinatları Hesaplama:
- 1 derece enlem ≈ 111 km
- 30 metre ≈ 0.00027 derece
- Test için: Öğretmen konumuna ±0.00027 ekle/çıkar

---

## Yöntem 2: Log'lara Bakarak Test

### Android Studio Logcat ile:

1. **Logcat'i Aç:**
   - Android Studio → View → Tool Windows → Logcat
   - Filter: `ApiService` veya `LocationHelper`

2. **Test Et:**
   - QR kod okut
   - Log'larda şunları ara:
     ```
     Student Location: lat=..., lon=...
     Distance from teacher: ...m
     Location check passed: ...m <= 30.0m
     ```
   - veya
     ```
     Location check failed: student is ...m away (max: 30m)
     ```

3. **Supabase Logs:**
   - Supabase Dashboard → Edge Functions → validate-qr → Logs
   - Mesafe hesaplamalarını görebilirsin

---

## Yöntem 3: Test Modu Ekleme (Geliştirme)

Eğer mock location çalışmazsa, uygulamaya test modu ekleyebiliriz:
- Debug build'de manuel konum girme
- Farklı mesafeleri test etme
- Log'larda detaylı bilgi gösterme

---

## Yöntem 4: İki Cihaz (Gerçek Test)

Eğer iki telefon varsa:
1. Telefon 1: Öğretmen hesabı → QR kod oluştur
2. Telefon 2: Öğrenci hesabı → QR kod okut
3. Farklı konumlarda test et (yakın/uzak)

---

## Hızlı Test Senaryosu

1. **Öğretmen olarak:**
   - QR kod oluştur
   - Logcat'te "Location: lat=..., lon=..." notunu al

2. **Mock Location ile:**
   - Aynı koordinatları gir (veya çok yakın)
   - Öğrenci olarak QR okut
   - ✅ Başarılı olmalı

3. **Mock Location ile:**
   - Koordinatları değiştir (100+ metre uzak)
   - Öğrenci olarak QR okut
   - ❌ "Konum uygun değil" hatası

---

## Sorun Giderme

**Mock Location çalışmıyor:**
- Developer Options'da "Mock Location App" seçili mi?
- Mock Location uygulaması açık mı?
- Konum servisleri açık mı?

**Konum alınamıyor:**
- Konum izni verildi mi?
- GPS açık mı?
- Logcat'te hata var mı?

**Test sonuçları tutarsız:**
- Log'lara bak: Mesafe hesaplaması doğru mu?
- Server-side kontrol çalışıyor mu? (Supabase logs)
- Client-side kontrol çalışıyor mu? (Android logs)



