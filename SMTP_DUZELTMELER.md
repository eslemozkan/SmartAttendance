# SMTP Ayarları Düzeltmeleri

## ❌ Bulunan Hatalar:

### 1. Username Yanlış
- **Şu an:** `eslem`
- **Olması gereken:** `eslem@gmail.com` (tam email adresi)

### 2. Sender Email Uyumsuz
- **Şu an:** `elifeslemozkan@hotmail.com` (Hotmail)
- **Sorun:** Gmail SMTP kullanıyorsunuz ama Hotmail email gönderiyorsunuz
- **Çözüm:** Sender email Gmail olmalı: `eslem@gmail.com` (veya kullandığınız Gmail adresi)

### 3. Port (Opsiyonel)
- **Şu an:** `465` (SSL)
- **Önerilen:** `587` (TLS - daha yaygın)

### 4. Password
- Gmail App Password kullanıyor musunuz?
- Normal Gmail şifresi çalışmaz!

---

## ✅ Doğru Ayarlar:

```
Host: smtp.gmail.com
Port: 587 (veya 465 - ikisi de çalışır)
Username: eslem@gmail.com (TAM EMAIL ADRESİ!)
Password: [Gmail App Password - 16 karakter]
Sender Email: eslem@gmail.com (Gmail adresiniz)
Sender Name: Fırat Üniversitesi (veya istediğiniz isim)
```

---

## 🔧 Düzeltme Adımları:

### Adım 1: Gmail App Password Oluşturun (Eğer yoksa)

1. https://myaccount.google.com adresine gidin
2. **Security** (Güvenlik) sekmesine gidin
3. **2-Step Verification** (2 Adımlı Doğrulama) açık olmalı
4. **App Passwords** (Uygulama Şifreleri) bölümüne gidin
5. **Select app:** Mail
6. **Select device:** Other (Custom name) → "Supabase" yazın
7. **Generate** (Oluştur) butonuna tıklayın
8. 16 karakterlik şifre oluşur (örn: `abcd efgh ijkl mnop`)
9. Boşlukları kaldırın: `abcdefghijklmnop`

### Adım 2: Supabase'de Ayarları Düzeltin

1. Supabase Dashboard > Settings > Auth > SMTP Settings
2. Şu değişiklikleri yapın:

   **Username:**
   - ❌ `eslem`
   - ✅ `eslem@gmail.com` (tam email adresi)

   **Password:**
   - ❌ Eski şifre (normal Gmail şifresi)
   - ✅ Gmail App Password (16 karakter, boşluksuz)

   **Sender Email:**
   - ❌ `elifeslemozkan@hotmail.com`
   - ✅ `eslem@gmail.com` (Gmail adresiniz)

   **Port (Opsiyonel):**
   - ❌ `465`
   - ✅ `587` (daha yaygın, önerilir)

3. **Save** butonuna tıklayın

### Adım 3: Test Edin

1. **"Send test email"** butonuna tıklayın
2. Email adresinizi girin
3. **Send** butonuna tıklayın
4. Email'inizi kontrol edin

---

## ⚠️ Önemli Notlar:

### Gmail SMTP Kullanıyorsanız:
- ✅ Sender Email **mutlaka Gmail** olmalı
- ✅ Username **tam email adresi** olmalı
- ✅ Password **App Password** olmalı (normal şifre değil!)

### Hotmail Email Göndermek İsterseniz:
- Gmail SMTP kullanamazsınız
- Hotmail SMTP kullanmalısınız:
  - Host: `smtp-mail.outlook.com`
  - Port: `587`
  - Username: `elifeslemozkan@hotmail.com`
  - Password: Hotmail şifreniz (veya App Password)

---

## 🎯 Özet:

**En önemli 3 düzeltme:**
1. ✅ Username: `eslem` → `eslem@gmail.com`
2. ✅ Sender Email: `elifeslemozkan@hotmail.com` → `eslem@gmail.com`
3. ✅ Password: Normal şifre → Gmail App Password

Bu 3 değişikliği yaptıktan sonra "Send test email" ile test edin!







