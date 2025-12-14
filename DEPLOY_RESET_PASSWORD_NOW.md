# 🚀 Reset Password Edge Function Deploy

## ✅ Secret'lar Hazır!

Secret'lar mevcut:
- ✅ `SUPABASE_SERVICE_ROLE_KEY` - var
- ✅ `RESEND_API_KEY` - var

## 🔄 Şimdi Yapılacak: Edge Function'ı Deploy Et

### Yöntem 1: Supabase Dashboard'dan (EN KOLAY)

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions

2. **`reset-password` function'ını bul:**
   - Sol menüden **Edge Functions** → **reset-password**

3. **Code sekmesine git:**
   - `supabase/functions/reset-password/index.ts` dosyasındaki kodu kopyala
   - Dashboard'daki code editor'a yapıştır

4. **Deploy et:**
   - **Deploy** butonuna tıkla
   - Veya **Save** butonuna tıkla (otomatik deploy eder)

### Yöntem 2: Supabase CLI ile

```bash
cd C:\Users\elife\Documents\GitHub\SmartAttendance
supabase functions deploy reset-password
```

**Not:** Eğer CLI yüklü değilse, Yöntem 1'i kullan.

---

## ✅ Deploy Kontrolü

Deploy başarılı olduğunda:

1. **Dashboard'da:**
   - Edge Functions → reset-password
   - Status: **Active** olmalı
   - Son deploy tarihi güncel olmalı

2. **Test et:**
   - Android uygulamasında "Şifremi Unuttum" özelliğini kullan
   - Email adresini gir
   - Artık hata gelmemeli!

---

## 🔍 Sorun Giderme

### ❌ Hala "Kullanıcı kontrolü yapılamadı" hatası
- Edge function'ı yeniden deploy et
- Supabase Dashboard → Edge Functions → reset-password → Logs
- Hata mesajlarını kontrol et

### ❌ "Missing Supabase service role key" hatası
- Secret'ları kontrol et (zaten var)
- Edge function'ı yeniden deploy et

### ❌ Email gelmiyor
- Resend API key doğru mu kontrol et
- Resend Dashboard'da email durumunu kontrol et: https://resend.com/emails


