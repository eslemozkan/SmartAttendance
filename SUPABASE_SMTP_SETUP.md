# 📧 Supabase SMTP Yapılandırması

## ✅ Supabase Built-in Email Sistemi

Edge function artık öncelikle Supabase'in built-in email sistemini kullanıyor. SMTP ayarlarını yapılandırmanız gerekiyor.

## 🔧 SMTP Ayarları

### Adım 1: Supabase Dashboard'a Git

1. https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl
2. **Settings → Auth → SMTP Settings** bölümüne git

### Adım 2: SMTP Ayarlarını Yapılandır

#### Seçenek A: Gmail SMTP (Önerilen - Ücretsiz)

1. **Gmail'de App Password oluşturun:**
   - Google Account → Security → 2-Step Verification (açık olmalı)
   - App Passwords → Select app: Mail → Select device: Other
   - 16 karakterlik şifre oluşturulur

2. **Supabase Dashboard'da ayarlayın:**
   - **Enable Custom SMTP:** Açık (yeşil toggle)
   - **SMTP Host:** `smtp.gmail.com`
   - **SMTP Port:** `587`
   - **SMTP User:** Gmail adresiniz (örn: `yourname@gmail.com`)
   - **SMTP Password:** Oluşturduğunuz App Password
   - **Sender Email:** Gmail adresiniz
   - **Sender Name:** `SmartAttendance`

3. **Save** butonuna tıklayın

#### Seçenek B: SendGrid (Ücretsiz 100 email/gün)

1. SendGrid hesabı oluşturun: https://sendgrid.com
2. API Key oluşturun
3. Supabase Dashboard'da:
   - **SMTP Host:** `smtp.sendgrid.net`
   - **SMTP Port:** `587`
   - **SMTP User:** `apikey`
   - **SMTP Password:** SendGrid API Key'iniz
   - **Sender Email:** SendGrid'de doğrulanmış email adresiniz

#### Seçenek C: Mailgun (Ücretsiz 5000 email/ay)

1. Mailgun hesabı oluşturun: https://mailgun.com
2. Domain doğrulayın
3. Supabase Dashboard'da:
   - **SMTP Host:** `smtp.mailgun.org`
   - **SMTP Port:** `587`
   - **SMTP User:** Mailgun SMTP kullanıcı adınız
   - **SMTP Password:** Mailgun SMTP şifreniz
   - **Sender Email:** Doğrulanmış domain'den (örn: `noreply@yourdomain.com`)

### Adım 3: Test Email Gönder

1. Supabase Dashboard → Settings → Auth → SMTP Settings
2. **"Send test email"** butonuna tıklayın
3. Email adresinizi girin
4. **Send** butonuna tıklayın
5. Email'inizi kontrol edin

### Adım 4: Edge Function'ı Deploy Et

SMTP ayarlarını yapılandırdıktan sonra edge function'ı yeniden deploy et:

```bash
supabase functions deploy reset-password
```

**VEYA** Supabase Dashboard'dan:
- Edge Functions → reset-password → Deploy

---

## ✅ Başarı Kontrolü

1. **Test email geldi mi?** → SMTP ayarları doğru!
2. **Android uygulamasında test et:**
   - "Şifremi Unuttum" özelliğini kullan
   - Email'inizi kontrol edin

---

## 🔍 Sorun Giderme

### ❌ "Error sending recovery email" hatası
**Sebep:** SMTP ayarları yanlış veya eksik
**Çözüm:**
1. SMTP ayarlarını kontrol et
2. Gmail App Password kullanıyorsanız, normal şifre çalışmaz
3. 2-Step Verification açık olmalı

### ❌ Email gelmiyor
**Kontrol et:**
1. ✅ SMTP ayarları doğru mu?
2. ✅ Test email geldi mi?
3. ✅ Spam klasörünü kontrol ettin mi?
4. ✅ Edge function loglarını kontrol et (Supabase Dashboard)

### ❌ "Authentication failed" hatası
**Sebep:** SMTP kullanıcı adı veya şifre yanlış
**Çözüm:**
- Gmail: App Password kullan (normal şifre çalışmaz)
- SendGrid: API Key doğru mu?
- Mailgun: SMTP bilgileri doğru mu?

---

## 📝 Notlar

- **Gmail App Password:** Normal Gmail şifresi çalışmaz, mutlaka App Password kullanın
- **2-Step Verification:** Gmail App Password için 2-Step Verification açık olmalı
- **Test Email:** SMTP ayarlarını test etmek için Supabase Dashboard'dan test email gönderebilirsiniz
- **Resend Fallback:** Test email adresi (`eslemlestrange@gmail.com`) için Resend kullanılır (sadece test için)

---

## 🎯 Özet

1. ✅ Supabase Dashboard → Settings → Auth → SMTP Settings
2. ✅ SMTP ayarlarını yapılandır (Gmail önerilir)
3. ✅ Test email gönder
4. ✅ Edge function'ı deploy et
5. ✅ Android uygulamasında test et







