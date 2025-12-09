# 🔍 Edge Function Kontrol Listesi

## "Failed to connect" Hatası Çözümü

### 1. Edge Function Deploy Edilmiş mi?

**Supabase Dashboard'da kontrol et:**
- https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions
- `create-qr` function'ı listede var mı?
- Status: **Active** olmalı
- Eğer yoksa veya Inactive ise → **Deploy et!**

### 2. Environment Variables Kontrolü

**Supabase Dashboard → Edge Functions → create-qr → Settings → Environment Variables:**

Şu iki değişken **MUTLAKA** olmalı:
```
SUPABASE_URL=https://oubvhffqbsxsnbtinzbl.supabase.co
SUPABASE_SERVICE_ROLE_KEY=<service_role_key_buraya>
```

**Service Role Key'i nereden al:**
- Dashboard → Settings → API
- "service_role" key'ini kopyala
- Edge Function environment variables'a ekle

### 3. Edge Function'ı Yeniden Deploy Et

**Terminal'de:**
```bash
cd supabase
supabase functions deploy create-qr
```

**Ya da Dashboard'dan:**
- Edge Functions → create-qr
- "Deploy" butonuna bas

### 4. Test Et

**Tarayıcıda test:**
- URL: https://oubvhffqbsxsnbtinzbl.functions.supabase.co/create-qr
- Eğer **404** görürsen → Deploy edilmemiş!
- Eğer **405 Method Not Allowed** görürsen → Deploy edilmiş ama POST bekliyor (normal)

**Postman veya curl ile test:**
```bash
POST https://oubvhffqbsxsnbtinzbl.functions.supabase.co/create-qr
Headers:
  Content-Type: application/json
  Authorization: Bearer <anon_key>
  apikey: <anon_key>
Body:
{
  "course_id": 1,
  "week_number": 1,
  "expire_after_minutes": 15
}
```

### 5. Migration'ı Çalıştır

**Supabase Dashboard → SQL Editor:**

`supabase/migrations/20250115_fix_qr_codes_schema.sql` dosyasının içeriğini çalıştır.

### 6. Logları Kontrol Et

**Supabase Dashboard → Edge Functions → create-qr → Logs**

Hata mesajlarını burada görebilirsin.

## ✅ Başarılı Deploy Kontrolü

Eğer Edge Function çalışıyorsa:
- POST isteği gönderildiğinde JSON response döner
- Hata durumunda detaylı hata mesajı döner
- "Failed to connect" hatası almazsın

## ❌ Hala "Failed to connect" Alıyorsan

1. **İnternet bağlantını kontrol et**
2. **Firewall/VPN sorunları olabilir**
3. **Edge Function URL'ini kontrol et:** `https://oubvhffqbsxsnbtinzbl.functions.supabase.co/create-qr`
4. **Android uygulamasını yeniden başlat**
5. **Logcat'te detaylı hata mesajlarını kontrol et:**
   ```bash
   adb logcat | grep ApiService
   ```


