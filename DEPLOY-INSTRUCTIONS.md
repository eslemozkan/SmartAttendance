# 🚀 Edge Function Deploy Talimatları

## ❗ ÖNEMLİ: `student-signup` Edge Function Deploy Et

`student-signup` function'ı deploy edilmediği için network_error alıyorsun!

### 📝 Deploy Adımları:

1. **Supabase Dashboard'a Git**: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl

2. **Edge Functions Bölümüne Git**: Sol menüden "Edge Functions" → "student-signup"

3. **Deploy Et**:
   - Eğer deploy butonu varsa bas
   - Yoksa, terminalde şunu çalıştır:
   ```bash
   cd supabase
   supabase functions deploy student-signup
   ```

4. **Environment Variables'ı Ayarla**:
   - Dashboard'da "Project Settings" → "Edge Functions" → "Environment variables"
   - Şunları ekle:
     - `SUPABASE_URL`: https://oubvhffqbsxsnbtinzbl.supabase.co
     - `SUPABASE_SERVICE_ROLE_KEY`: (Supabase Dashboard'dan al, API Settings → service_role key)

5. **Test Et**: 
   - Android'de signup ekranından tekrar dene
   - Logcat'te hata mesajlarını kontrol et:
     ```
     adb logcat | grep ApiService
     ```

### 🔍 Alternatif: Manuel Deploy

Eğer yukarıdaki yöntem çalışmazsa:

1. **Supabase CLI ile deploy et**:
   ```bash
   cd supabase/functions
   supabase functions deploy student-signup
   ```

2. **Ya da Supabase Dashboard'dan direkt deploy et**

### ✅ Deploy Edildikten Sonra:

- Android uygulamasını yeniden başlat
- Signup ekranından bir öğrenci email'i ile kayıt ol
- Logcat'te şunları göreceksin:
  - "Signup URL: ..."
  - "Signup Response Code: ..."
  - "Signup Response Body: ..."

### 🎯 Başarılı Deploy Kontrolü:

Supabase Dashboard → Edge Functions → student-signup → Invoke URL'ini kopyala
Ardından tarayıcıda test et: https://oubvhffqbsxsnbtinzbl.functions.supabase.co/student-signup

Eğer "Method not allowed" hatası alırsan → DEĞİL deployed!
Eğer JSON response dönerse → ✅ Deployed!

