# 🧪 Şifre Sıfırlama Test Rehberi

## ✅ Deploy Edildi!

Edge function deploy edildi. Şimdi test edelim.

## 🧪 Test Adımları

### 1. Android Uygulamasında Test

1. **Uygulamayı aç**
2. **Login ekranına git**
3. **"Şifremi Unuttum" linkine tıkla** (öğrenci veya öğretmen için)
4. **Email adresini gir** (kayıtlı bir email)
5. **"Gönder" butonuna tıkla**

### 2. Beklenen Sonuç

✅ **Başarılı durum:**
- "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi" mesajı görünmeli
- Email'inizi kontrol edin (spam klasörünü de!)

❌ **Hata durumu:**
- Hata mesajı görünürse, Android Logcat'te kontrol edin

### 3. Logları Kontrol Et

**Android Studio → Logcat:**
```
ApiService: === Password Reset Request ===
ApiService: URL: https://oubvhffqbsxsnbtinzbl.functions.supabase.co/reset-password
ApiService: Response Code: 200
ApiService: Password reset response parsed: ok=true, emailSent=true
```

**Supabase Dashboard → Edge Functions → reset-password → Logs:**
```
Resetting password for email: ...
Service role key available: Yes
Reset link generated successfully for: ...
Sending email via Resend API...
Resend API response: 200
Email sent successfully via Resend
```

### 4. Email Kontrolü

1. **Email'inizi kontrol edin** (spam klasörünü de!)
2. **Resend Dashboard'da kontrol edin:**
   - https://resend.com/emails
   - Email gönderim durumunu görebilirsiniz

---

## 🔍 Sorun Giderme

### ❌ "Kullanıcı kontrolü yapılamadı" hatası
**Sebep:** Edge function eski kodla deploy edilmiş olabilir
**Çözüm:** Edge function'ı yeniden deploy et

### ❌ "Missing Supabase service role key" hatası
**Sebep:** Secret eksik (ama zaten var)
**Çözüm:** Edge function'ı yeniden deploy et

### ❌ Email gelmiyor
**Kontrol et:**
1. ✅ Resend API key doğru mu? (Supabase Dashboard → Edge Functions → Secrets)
2. ✅ Email adresi kayıtlı mı?
3. ✅ Spam klasörünü kontrol ettin mi?
4. ✅ Resend Dashboard'da email durumu nedir?

### ❌ Response Code: 404
**Sebep:** Edge function deploy edilmemiş
**Çözüm:** Edge function'ı deploy et

### ❌ Response Code: 500
**Sebep:** Edge function'da hata var
**Çözüm:** Supabase Dashboard → Edge Functions → reset-password → Logs
- Hata mesajlarını kontrol et

---

## ✅ Başarı Kriterleri

✅ **Android loglarında:**
- `Response Code: 200`
- `ok=true`
- `emailSent=true`

✅ **Supabase loglarında:**
- `"Reset link generated successfully"`
- `"Sending email via Resend API..."`
- `"Email sent successfully via Resend"`

✅ **Email geldi:**
- Email'inizde şifre sıfırlama bağlantısı var
- Bağlantıya tıklayınca şifre sıfırlama sayfası açılıyor

---

## 📝 Test Email Adresi

Gerçek bir email adresi kullanın (kayıtlı olmalı):
- Öğrenci email'i
- Öğretmen email'i

Test email'i: `onboarding@resend.dev` kullanmayın (bu sadece gönderen adresi)


