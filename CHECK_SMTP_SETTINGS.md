# SMTP Ayarlarını Kontrol Etme Rehberi

## Adım 1: Supabase Dashboard'a Gidin

1. https://supabase.com/dashboard
2. Projenizi seçin: `SmartAttendance`

## Adım 2: SMTP Ayarlarını Kontrol Edin

1. Sol menüden **Settings**'e tıklayın
2. **Auth** sekmesine gidin
3. **SMTP Settings** bölümüne scroll edin

## Kontrol Listesi:

### ✅ SMTP Etkin mi?
- **"Enable Custom SMTP"** toggle'ı **AÇIK** (yeşil) olmalı

### ✅ Host Doğru mu?
- Gmail için: `smtp.gmail.com`
- SendGrid için: `smtp.sendgrid.net`
- Mailgun için: `smtp.mailgun.org`

### ✅ Port Doğru mu?
- `587` (veya `465`)

### ✅ Username Dolu mu?
- Gmail için: Gmail adresiniz (örn: `yourname@gmail.com`)
- SendGrid için: `apikey`
- Mailgun için: SMTP kullanıcı adınız

### ✅ Password Dolu mu?
- Gmail için: **App Password** (16 karakter, boşluksuz)
- SendGrid için: API Key
- Mailgun için: SMTP şifreniz

### ✅ Sender Email Dolu mu?
- Gönderen email adresi yazılmış olmalı

### ✅ Sender Name Dolu mu?
- Gönderen ismi yazılmış olmalı (örn: `SmartAttendance`)

## Adım 3: Test Email Gönderin

1. **SMTP Settings** sayfasında **"Send test email"** butonuna tıklayın
2. Email adresinizi girin
3. **Send** butonuna tıklayın
4. Email'inizi kontrol edin (spam klasörünü de kontrol edin)

**Eğer test email gelirse:** ✅ SMTP ayarları doğru!
**Eğer test email gelmezse:** ❌ Ayarları tekrar kontrol edin

## Yaygın Hatalar:

### Gmail Kullanıyorsanız:
- ❌ Normal Gmail şifresi çalışmaz
- ✅ **App Password** kullanmalısınız
- ✅ 2-Step Verification açık olmalı

### SendGrid Kullanıyorsanız:
- ✅ Username: `apikey` (tam olarak bu kelime)
- ✅ Password: SendGrid API Key'iniz

### Mailgun Kullanıyorsanız:
- ✅ Domain doğrulanmış olmalı
- ✅ SMTP credentials'ları kullanın

## Adım 4: Email Template'lerini Kontrol Edin

1. Sol menüden **Authentication**'a gidin
2. **Email Templates** sekmesine gidin
3. **"Reset Password"** template'inin aktif olduğundan emin olun

## Adım 5: Android Uygulamasında Test Edin

1. Uygulamayı açın
2. "Şifremi Unuttum"a tıklayın
3. **Sistemde kayıtlı bir email** adresi girin
4. Email'inizi kontrol edin

---

## Hızlı Kontrol Komutu (Supabase Dashboard'dan):

SMTP ayarlarını kontrol etmek için Dashboard'da:
- Settings > Auth > SMTP Settings
- Tüm alanların dolu olduğundan emin olun
- "Send test email" butonunu kullanın







