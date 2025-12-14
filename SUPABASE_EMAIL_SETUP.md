# Supabase Email Yapılandırması

## Adım 1: Supabase Dashboard'a Giriş

1. https://supabase.com/dashboard adresine gidin
2. Projenizi seçin: `oubvhffqbsxsnbtinzbl`

## Adım 2: SMTP Ayarlarını Yapılandırın

### Seçenek A: Gmail SMTP (Önerilen - Ücretsiz)

1. **Gmail'de App Password oluşturun:**
   - Google Account > Security > 2-Step Verification (açık olmalı)
   - App Passwords > Select app: Mail > Select device: Other
   - 16 karakterlik şifre oluşturulur

2. **Supabase Dashboard'da ayarlayın:**
   - Settings > Auth > SMTP Settings
   - Enable Custom SMTP: **Açık**
   - SMTP Host: `smtp.gmail.com`
   - SMTP Port: `587`
   - SMTP User: Gmail adresiniz (örn: `yourname@gmail.com`)
   - SMTP Password: Oluşturduğunuz App Password
   - Sender Email: Gmail adresiniz
   - Sender Name: `SmartAttendance`

### Seçenek B: SendGrid (Ücretsiz 100 email/gün)

1. SendGrid hesabı oluşturun: https://sendgrid.com
2. API Key oluşturun
3. Supabase Dashboard'da:
   - SMTP Host: `smtp.sendgrid.net`
   - SMTP Port: `587`
   - SMTP User: `apikey`
   - SMTP Password: SendGrid API Key'iniz
   - Sender Email: SendGrid'de doğrulanmış email adresiniz

### Seçenek C: Mailgun (Ücretsiz 5000 email/ay)

1. Mailgun hesabı oluşturun: https://mailgun.com
2. Domain doğrulayın
3. Supabase Dashboard'da:
   - SMTP Host: `smtp.mailgun.org`
   - SMTP Port: `587`
   - SMTP User: Mailgun SMTP kullanıcı adınız
   - SMTP Password: Mailgun SMTP şifreniz
   - Sender Email: Doğrulanmış domain'den (örn: `noreply@yourdomain.com`)

## Adım 3: Email Template'lerini Kontrol Edin

1. **Authentication > Email Templates** bölümüne gidin
2. **"Reset Password"** template'ini kontrol edin
3. Template'in aktif olduğundan emin olun
4. İsterseniz template'i özelleştirebilirsiniz:

```html
<h2>Şifre Sıfırlama</h2>
<p>Merhaba,</p>
<p>Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:</p>
<p><a href="{{ .ConfirmationURL }}">Şifremi Sıfırla</a></p>
<p>Bu bağlantı 1 saat süreyle geçerlidir.</p>
```

## Adım 4: Test Edin

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullanın
2. Email adresinizi girin
3. Email'inizi kontrol edin (spam klasörünü de kontrol edin)
4. Email'deki bağlantıya tıklayarak şifrenizi sıfırlayın

## Sorun Giderme

### Email gelmiyor?

1. **SMTP ayarlarını kontrol edin:**
   - Settings > Auth > SMTP Settings
   - Tüm bilgilerin doğru olduğundan emin olun
   - "Test Email" butonunu kullanarak test edin

2. **Email template'lerini kontrol edin:**
   - Authentication > Email Templates
   - "Reset Password" template'inin aktif olduğundan emin olun

3. **Spam klasörünü kontrol edin:**
   - Email'ler spam klasörüne düşebilir

4. **Gmail App Password kullanıyorsanız:**
   - Normal Gmail şifresi çalışmaz, mutlaka App Password kullanın
   - 2-Step Verification açık olmalı

5. **Logları kontrol edin:**
   - Supabase Dashboard > Logs > Auth Logs
   - Email gönderim hatalarını buradan görebilirsiniz

## Notlar

- Supabase'in `/auth/v1/recover` endpoint'i SMTP yapılandırılmışsa otomatik email gönderir
- Edge function zaten bu endpoint'i kullanıyor
- SMTP yapılandırması yapıldıktan sonra email gönderimi otomatik çalışır
- Email gelmezse, edge function yine de reset link'ini döndürür (backup olarak)


