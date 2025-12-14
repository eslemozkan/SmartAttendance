# 🔧 QR Kod Oluşturma Timeout Sorunu Çözümü

## Sorunun Nedeni

QR kod oluşturma önceden çalışıyordu ama şimdi timeout hatası veriyor. Bunun nedeni:

1. **Tablo yapısı değişti**: Edge Function `course_id` ve `week_number` kolonlarını kullanıyor ama tabloda yok
2. **Migration çalıştırılmadı**: Yeni kolonlar eklenmedi
3. **RLS sorunları**: Row Level Security Edge Function'ları engelliyor olabilir

## ✅ Çözüm Adımları

### 1. Migration'ı Çalıştırın (ÖNEMLİ!)

**Supabase Dashboard → SQL Editor:**

`supabase/migrations/20250115_fix_qr_codes_complete.sql` dosyasının içeriğini çalıştırın.

Bu migration:
- `course_id` ve `week_number` kolonlarını ekler
- Foreign key constraint'i düzeltir
- RLS'yi disable eder
- Index'leri oluşturur

### 2. Edge Function Environment Variables Kontrolü

**Supabase Dashboard → Edge Functions → create-qr → Settings → Environment Variables:**

Şu iki değişken olmalı:
```
SUPABASE_URL=https://oubvhffqbsxsnbtinzbl.supabase.co
SUPABASE_SERVICE_ROLE_KEY=<service_role_key>
```

### 3. Edge Function'ı Deploy Edin

**Supabase Dashboard → Edge Functions → create-qr → Deploy**

Ya da terminal'de:
```bash
cd supabase
supabase functions deploy create-qr
```

### 4. Test Edin

Android uygulamasını yeniden başlatın ve QR kod oluşturmayı deneyin.

## 🔍 Sorun Devam Ederse

### Logları Kontrol Edin

**Supabase Dashboard → Edge Functions → create-qr → Logs**

Hata mesajlarını burada görebilirsiniz.

### Veritabanı Yapısını Kontrol Edin

**Supabase Dashboard → Table Editor → qr_codes**

Şu kolonlar olmalı:
- `id` (uuid)
- `assignment_id` (bigint) - eski kolon, kalabilir
- `course_id` (bigint) - YENİ, olmalı
- `week_number` (integer) - YENİ, olmalı
- `created_at` (timestamptz)
- `expire_after_minutes` (integer)
- `is_active` (boolean)

### Manuel Kontrol

SQL Editor'de şunu çalıştırın:
```sql
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'qr_codes' 
ORDER BY ordinal_position;
```

`course_id` ve `week_number` kolonlarını görmelisiniz.

## ⚠️ Önemli Notlar

- Migration'ı çalıştırmadan Edge Function çalışmaz
- RLS disable edilmeli (development için)
- Environment variables mutlaka ayarlanmalı






