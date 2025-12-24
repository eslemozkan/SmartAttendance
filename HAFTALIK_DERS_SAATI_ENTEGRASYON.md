# Haftalık Ders Saati Entegrasyonu - Özet

## Yapılan Değişiklikler

### 1. Database Migrations ✅
- `courses.weekly_hours` kolonu eklendi (haftalık ders saati)
- `course_weekly_sessions` tablosu oluşturuldu (haftalık ders oturum takibi)
- `attendances.session_number` kolonu eklendi (her oturum için ayrı yoklama)

### 2. Edge Functions ✅
- **create-qr**: `session_numbers` parametresi eklendi, birden fazla oturum seçilebiliyor
- **validate-qr**: Seçilen oturum sayısı kadar attendance kaydı oluşturuyor
- **get-weekly-sessions**: Yeni endpoint - işlenmemiş dersleri getiriyor

### 3. Android Models ✅
- `Course.weeklyHours` eklendi
- `WeeklySession` model eklendi
- `GetWeeklySessionsResponse` model eklendi
- `CreateQRRequest.sessionNumbers` eklendi

### 4. Android API Service ✅
- `getWeeklySessions()` fonksiyonu eklendi
- `createQRCode()` fonksiyonu `sessionNumbers` parametresi ile güncellendi

### 5. Android UI (TeacherActivity) ⏳
- Ders seçildiğinde haftalık dersleri getir
- Checkbox listesi ile ders seçimi
- İşlenmiş dersleri disabled göster
- QR kod oluştururken seçilen dersleri gönder

### 6. Web Admin ⏳
- Course ekleme formuna `weekly_hours` alanı ekle

---

## Kullanım Senaryosu

1. **Ders Ekleme**: Admin ders eklerken `weekly_hours` belirtir (örn: 4 saat)
2. **QR Kod Oluşturma**: 
   - Öğretmen ders ve hafta seçer
   - Sistem işlenmemiş dersleri gösterir (1, 2, 3, 4)
   - Öğretmen birden fazla ders seçer (örn: 2 saatlik ders için 1 ve 2)
   - QR kod oluşturulur
3. **Yoklama**: 
   - Öğrenci QR kod okutur
   - Seçilen ders sayısı kadar yoklama kaydı oluşturulur (2 kayıt)
4. **Tekrar Seçim**: 
   - İşlenmiş dersler (1, 2) artık seçilemez
   - Sadece işlenmemiş dersler (3, 4) seçilebilir

---

## Database Şeması

### courses
- `weekly_hours` INTEGER (haftalık ders saati)

### course_weekly_sessions
- `course_id` BIGINT
- `week_number` INTEGER
- `session_number` INTEGER (1, 2, 3, 4...)
- `qr_code_id` UUID (null ise işlenmemiş)

### attendances
- `session_number` INTEGER (hangi oturum için yoklama)

---

## API Endpoints

### POST /functions/v1/get-weekly-sessions
```json
{
  "course_id": 1,
  "week_number": 1
}
```

Response:
```json
{
  "course_id": 1,
  "week_number": 1,
  "weekly_hours": 4,
  "available_sessions": [
    {"session_number": 1, "is_completed": false},
    {"session_number": 2, "is_completed": false}
  ],
  "total_sessions": 4,
  "completed_sessions": 2
}
```

### POST /functions/v1/create-qr
```json
{
  "course_id": 1,
  "week_number": 1,
  "expire_after_minutes": 15,
  "session_numbers": [1, 2]
}
```

---

## Sonraki Adımlar

1. ✅ Database migrations çalıştır
2. ✅ Edge functions deploy et
3. ⏳ TeacherActivity UI güncelle
4. ⏳ Web-admin course form güncelle
5. ⏳ Test et



