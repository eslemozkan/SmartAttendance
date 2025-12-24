# 🚀 Edge Function Deploy - Acil!

## ❌ Sorun: Eski Kod Çalışıyor!

Loglardan görüldüğü üzere:
- ❌ Edge function eski kodla çalışıyor
- ❌ Önce Resend'e istek gönderiyor (403 hatası)
- ❌ Supabase'in built-in email sistemine geçmiyor

## ✅ Çözüm: Edge Function'ı Yeniden Deploy Et

### Yöntem 1: Supabase Dashboard'dan (Önerilen)

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions

2. **Edge Functions → reset-password** bölümüne git

3. **"Code"** sekmesine tıkla

4. **Mevcut kodu sil** ve yeni kodu yapıştır:
   - `supabase/functions/reset-password/index.ts` dosyasındaki kodu kopyala
   - Dashboard'daki code editor'a yapıştır

5. **"Deploy"** butonuna tıkla

### Yöntem 2: Terminal'den

```bash
cd C:\Users\elife\Documents\GitHub\SmartAttendance
supabase functions deploy reset-password
```

---

## ✅ Deploy Kontrolü

Deploy başarılı olduğunda:

1. **Edge Functions → reset-password → Logs**
2. Yeni bir istek gönder (Android'den)
3. Loglarda şunları ara:
   - ✅ `"Attempting to send email via Supabase Auth API..."`
   - ✅ `"Supabase Auth API response status: 200"`
   - ✅ `"Email sent via Supabase Auth API"`

**Eski loglar:**
- ❌ `"Sending email via Resend API..."` (artık görünmemeli)

---

## 🧪 Test Et

1. **Edge function'ı deploy et**
2. **Android uygulamasında "Şifremi Unuttum" özelliğini kullan**
3. **Email'inizi kontrol et**

---

## 📝 Not

Eski kod önce Resend'e istek gönderiyordu. Yeni kod önce Supabase'in built-in email sistemini kullanıyor, Resend sadece test email için fallback olarak kullanılıyor.

**Edge function'ı deploy ettikten sonra tekrar test et!**







