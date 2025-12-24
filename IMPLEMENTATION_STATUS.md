# Haftalık Ders Saati Entegrasyonu - Durum Raporu

## ✅ Tamamlanan İşler

### 1. Database Migrations
- ✅ `courses.weekly_hours` kolonu eklendi
- ✅ `course_weekly_sessions` tablosu oluşturuldu
- ✅ `attendances.session_number` kolonu eklendi
- ✅ Unique constraint güncellendi

### 2. Edge Functions
- ✅ `create-qr`: `session_numbers` parametresi eklendi
- ✅ `validate-qr`: Çoklu oturum desteği eklendi
- ✅ `get-weekly-sessions`: Yeni endpoint oluşturuldu

### 3. Android Backend
- ✅ `Course.weeklyHours` eklendi
- ✅ `WeeklySession` model eklendi
- ✅ `GetWeeklySessionsResponse` model eklendi
- ✅ `CreateQRRequest.sessionNumbers` eklendi
- ✅ `ApiService.getWeeklySessions()` eklendi
- ✅ `ApiService.createQRCode()` güncellendi

## ⏳ Devam Eden İşler

### 4. Android UI (TeacherActivity)
- ⏳ Ders seçildiğinde haftalık dersleri getirme
- ⏳ Checkbox listesi ile ders seçimi UI
- ⏳ İşlenmiş dersleri disabled gösterme
- ⏳ QR kod oluştururken seçilen dersleri gönderme

### 5. Web Admin
- ⏳ Course ekleme formuna `weekly_hours` alanı ekleme

---

## Yapılması Gerekenler

### Adım 1: Database Migrations Çalıştır
Supabase SQL Editor'de şu migration'ları çalıştır:
1. `20250116_add_weekly_hours_to_courses.sql`
2. `20250116_create_course_weekly_sessions.sql`
3. `20250116_add_session_number_to_attendances.sql`

### Adım 2: Edge Functions Deploy
Supabase Dashboard'da deploy et:
1. `get-weekly-sessions` (yeni)
2. `create-qr` (güncellendi)
3. `validate-qr` (güncellendi)

### Adım 3: Android UI Güncelle
TeacherActivity'de:
- Ders ve hafta seçildiğinde `getWeeklySessions()` çağır
- Checkbox listesi göster (RecyclerView veya LinearLayout)
- Seçilen dersleri `createQRCode()` ile gönder

### Adım 4: Web Admin Güncelle
Course ekleme formuna `weekly_hours` input alanı ekle.

---

## Test Senaryosu

1. **Ders Ekle**: Admin panelinden ders ekle, `weekly_hours = 4` belirt
2. **QR Oluştur**: 
   - Öğretmen ders ve hafta seçer
   - Sistem 4 ders gösterir (1, 2, 3, 4)
   - Öğretmen 2 ders seçer (1, 2)
   - QR kod oluşturulur
3. **Yoklama**: 
   - Öğrenci QR okutur
   - 2 attendance kaydı oluşturulur (session_number: 1 ve 2)
4. **Tekrar QR**: 
   - Aynı hafta için tekrar QR oluştur
   - Sadece 3 ve 4 seçilebilir (1 ve 2 işlenmiş)

---

## Notlar

- Geriye dönük uyumluluk: `session_number` NULL ise tek oturum varsayılır
- Mevcut QR kodlar çalışmaya devam eder
- Yeni sistem ile eski sistem birlikte çalışabilir



