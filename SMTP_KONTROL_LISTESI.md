# SMTP Ayarları Kontrol Listesi

## ✅ Hızlı Kontrol (5 dakika)

### 1. Supabase Dashboard'a Gidin
- https://supabase.com/dashboard
- Projenizi seçin

### 2. SMTP Ayarlarını Açın
- **Settings** > **Auth** > **SMTP Settings**

### 3. Kontrol Edin:

```
☐ Enable Custom SMTP: AÇIK (yeşil toggle)
☐ Host: smtp.gmail.com (veya kullandığınız servis)
☐ Port: 587
☐ Username: Dolu mu?
☐ Password: Dolu mu? (Gmail için App Password!)
☐ Sender Email: Dolu mu?
☐ Sender Name: Dolu mu?
```

### 4. Test Email Gönderin
- **"Send test email"** butonuna tıklayın
- Email adresinizi girin
- **Send** butonuna tıklayın
- Email'inizi kontrol edin

**✅ Email geldi mi?** → SMTP ayarları doğru!
**❌ Email gelmedi mi?** → Aşağıdaki sorunları kontrol edin

---

## 🔍 Detaylı Kontrol

### Gmail Kullanıyorsanız:

1. **App Password oluşturdunuz mu?**
   - Google Account > Security > 2-Step Verification (AÇIK olmalı)
   - App Passwords > Mail > Other > "Supabase" yazın
   - 16 karakterlik şifre oluşturulur
   - Bu şifreyi Supabase'deki Password alanına yapıştırın

2. **Normal Gmail şifresi çalışmaz!**
   - ❌ Gmail şifreniz: `mypassword123`
   - ✅ App Password: `abcdefghijklmnop` (16 karakter, boşluksuz)

3. **Supabase'deki ayarlar:**
   ```
   Host: smtp.gmail.com
   Port: 587
   Username: yourname@gmail.com
   Password: [App Password - 16 karakter]
   Sender Email: yourname@gmail.com
   Sender Name: SmartAttendance
   ```

### SendGrid Kullanıyorsanız:

1. **API Key oluşturdunuz mu?**
   - SendGrid Dashboard > Settings > API Keys
   - "Full Access" veya "Mail Send" yetkisi olan key oluşturun

2. **Supabase'deki ayarlar:**
   ```
   Host: smtp.sendgrid.net
   Port: 587
   Username: apikey (tam olarak bu kelime!)
   Password: [SendGrid API Key]
   Sender Email: [SendGrid'de doğrulanmış email]
   Sender Name: SmartAttendance
   ```

### Mailgun Kullanıyorsanız:

1. **Domain doğruladınız mı?**
   - Mailgun Dashboard > Sending > Domains
   - Domain doğrulanmış olmalı

2. **Supabase'deki ayarlar:**
   ```
   Host: smtp.mailgun.org
   Port: 587
   Username: [Mailgun SMTP username]
   Password: [Mailgun SMTP password]
   Sender Email: noreply@yourdomain.com
   Sender Name: SmartAttendance
   ```

---

## 🧪 Test Adımları

### Adım 1: Supabase Dashboard'dan Test
1. Settings > Auth > SMTP Settings
2. "Send test email" butonuna tıklayın
3. Email adresinizi girin
4. Email'inizi kontrol edin

### Adım 2: Android Uygulamasından Test
1. Uygulamayı açın
2. "Şifremi Unuttum"a tıklayın
3. **Sistemde kayıtlı bir email** girin
4. Email'inizi kontrol edin (spam klasörünü de!)

### Adım 3: Logları Kontrol Edin
1. Supabase Dashboard > Logs > Auth Logs
2. Email gönderim hatalarını buradan görebilirsiniz

---

## ❌ Yaygın Hatalar ve Çözümleri

### "Authentication failed"
- **Sebep:** Username veya Password yanlış
- **Çözüm:** Gmail için App Password kullandığınızdan emin olun

### "Connection timeout"
- **Sebep:** Port yanlış veya firewall engelliyor
- **Çözüm:** Port 587 kullanın (465 değil)

### "Sender address rejected"
- **Sebep:** Sender Email doğrulanmamış
- **Çözüm:** Gmail için kendi email'inizi, SendGrid/Mailgun için doğrulanmış email kullanın

### Email gelmiyor ama hata yok
- **Sebep:** SMTP ayarları yanlış ama Supabase hata göstermiyor
- **Çözüm:** "Send test email" ile test edin, logları kontrol edin

---

## ✅ Başarı Kriterleri

SMTP ayarları doğru yapılandırılmışsa:
- ✅ "Send test email" ile email gelir
- ✅ Android uygulamasından "Şifremi Unuttum" çalışır
- ✅ Email spam klasörüne düşmez (genellikle)
- ✅ Email'deki link çalışır

---

## 📞 Yardım

Hala çalışmıyorsa:
1. Supabase Dashboard > Logs > Auth Logs'u kontrol edin
2. "Send test email" sonucunu kontrol edin
3. Gmail/SendGrid/Mailgun dashboard'larından gönderim loglarını kontrol edin


