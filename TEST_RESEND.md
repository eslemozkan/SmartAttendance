# 🧪 Resend API Test Rehberi

## ✅ Kontrol Listesi

### 1. Secret Eklendi mi?
- ✅ Supabase Dashboard → Edge Functions → reset-password → Settings → Secrets
- ✅ `RESEND_API_KEY` var mı? Değeri doğru mu?

### 2. Edge Function Deploy Edildi mi?
- ✅ Supabase Dashboard → Edge Functions → reset-password
- ✅ Status: **Active** olmalı
- ✅ Son deploy tarihi kontrol et

### 3. Android App Güncellendi mi?
- ✅ `ApiService.kt` güncellendi (edge function kullanıyor)
- ✅ Uygulamayı yeniden build et

---

## 🧪 Test Adımları

### Adım 1: Android'de Test Et

1. **Uygulamayı aç**
2. **Login ekranına git**
3. **"Şifremi Unuttum" linkine tıkla**
4. **Email adresini gir** (kayıtlı bir email)
5. **"Gönder" butonuna tıkla**

### Adım 2: Logları Kontrol Et

**Android Studio Logcat'te şunları ara:**
```
ApiService: === Password Reset Request ===
ApiService: URL: https://oubvhffqbsxsnbtinzbl.functions.supabase.co/reset-password
ApiService: Response Code: 200
ApiService: Password reset response parsed: ok=true, emailSent=true
```

**Supabase Dashboard'da:**
1. Edge Functions → reset-password → Logs
2. Şunları ara:
   - `"Resend API key found"`
   - `"Sending email via Resend API..."`
   - `"Resend API response: 200"`
   - `"Email sent successfully via Resend"`

### Adım 3: Email Kontrolü

1. **Email'inizi kontrol edin** (spam klasörünü de!)
2. **Resend Dashboard'da kontrol edin:**
   - https://resend.com/emails
   - Email gönderim durumunu görebilirsiniz

---

## 🔍 Sorun Giderme

### ❌ "Response Code: 404"
**Sebep:** Edge function deploy edilmemiş
**Çözüm:** 
```bash
supabase functions deploy reset-password
```

### ❌ "Response Code: 500"
**Sebep:** Edge function'da hata var
**Çözüm:** 
- Supabase Dashboard → Edge Functions → reset-password → Logs
- Hata mesajını kontrol et

### ❌ "emailSent: false"
**Sebep:** Resend API key yanlış veya eksik
**Çözüm:**
1. Supabase Dashboard → Edge Functions → reset-password → Settings → Secrets
2. `RESEND_API_KEY` değerini kontrol et
3. Resend Dashboard'dan yeni API key oluştur

### ❌ Email gelmiyor
**Kontrol et:**
1. ✅ Resend API key doğru mu?
2. ✅ Email adresi kayıtlı mı?
3. ✅ Spam klasörünü kontrol ettin mi?
4. ✅ Resend Dashboard'da email durumu nedir?

---

## 📊 Başarı Kriterleri

✅ **Android loglarında:**
- `Response Code: 200`
- `ok=true`
- `emailSent=true`

✅ **Supabase loglarında:**
- `"Sending email via Resend API..."`
- `"Resend API response: 200"`
- `"Email sent successfully via Resend"`

✅ **Email geldi:**
- Email'inizde şifre sıfırlama bağlantısı var
- Bağlantıya tıklayınca şifre sıfırlama sayfası açılıyor

---

## 🎯 Test Email Adresi

Gerçek bir email adresi kullanın (kayıtlı olmalı):
- Öğrenci email'i
- Öğretmen email'i

Test email'i: `onboarding@resend.dev` kullanmayın (bu sadece gönderen adresi)







