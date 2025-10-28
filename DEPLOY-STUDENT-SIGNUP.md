# 🚀 Student Signup Edge Function Deploy Rehberi

## ✅ Deploy Etme Adımları:

### Yöntem 1: Supabase Dashboard'dan (EN KOLAY)

1. **Supabase Dashboard'a Git**: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions

2. **"Deploy a new function" butonuna bas**

3. **Dosya seç**:
   - Function name: `student-signup`
   - Seç: `supabase/functions/student-signup/index.ts` dosyasını

4. **"Deploy" butonuna bas**

5. **Environment Variables ekle**:
   - Settings → Edge Functions → Environment variables
   - İki değişken ekle:
     - `SUPABASE_URL` = `https://oubvhffqbsxsnbtinzbl.supabase.co`
     - `SUPABASE_SERVICE_ROLE_KEY` = API Settings'den al (service_role key)

### Yöntem 2: Supabase CLI ile (Terminalden)

```bash
# Önce login ol
supabase login

# Projeyi link et
supabase link --project-ref oubvhffqbsxsnbtinzbl

# Function'ı deploy et
supabase functions deploy student-signup
```

### Yöntem 3: Manuel Dosya Yükleme

1. `supabase/functions/student-signup/` klasöründeki `index.ts` dosyasını kopyala

2. Supabase Dashboard'da:
   - Edge Functions → "Deploy a new function"
   - Function name: `student-signup`
   - File: `index.ts` dosyasını seç

3. **Environment variables ekle** (ÖNEMLİ!):
   - Dashboard → Settings → Edge Functions → Environment variables
   - Şunları ekle:
     ```
     SUPABASE_URL=https://oubvhffqbsxsnbtinzbl.supabase.co
     SUPABASE_SERVICE_ROLE_KEY=<service_role_key_here>
     ```
   - Service Role Key'i nereden al: Dashboard → Settings → API → service_role key

### ✅ Deploy Kontrolü:

Deploy edildikten sonra test et:
```bash
curl -X POST https://oubvhffqbsxsnbtinzbl.functions.supabase.co/student-signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"123456"}'
```

Eğer `{"ok":false,"error":"email_not_whitelisted"}` dönerse → ✅ DEĞİL DEPLOYED!

### 🎯 Android'de Test Et:

1. Android Studio'yu aç
2. Uygulamayı çalıştır (Run)
3. Signup ekranına git
4. Admin panelinde eklediğin öğrenci email'i ile kayıt ol
5. Logcat'te şunları göreceksin:
   ```
   ApiService: Signup URL: ...
   ApiService: Signup Response Code: 200
   ```

### ❌ Hata Alırsan:

**"network_error"** alıyorsan:
- Edge Function deploy edilmemiş
- URL yanlış
- Deploy tekrar et

**"email_not_whitelisted"** alıyorsan:
- Email `students` tablosunda yok
- Admin panelden öğrenci ekle

**"auth_creation_failed"** alıyorsan:
- SUPABASE_SERVICE_ROLE_KEY yanlış
- Environment variables kontrol et

