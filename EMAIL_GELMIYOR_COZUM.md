# 📧 Email Gelmiyor - Çözüm Rehberi

## ✅ Durum

- **Response Code:** 200 (başarılı)
- **Response Body:** `{}` (boş)
- **Supabase:** "Email gönderildi" diyor
- **Gerçek:** Email gelmiyor

## 🔍 Olası Nedenler ve Çözümler

### 1. Spam Klasörünü Kontrol Edin ⚠️ EN YAYGIN

**Gmail'de:**
1. Gmail'i açın
2. Sol menüden **"Spam"** klasörüne tıklayın
3. Email'i orada arayın

**Diğer Email Sağlayıcıları:**
- Outlook/Hotmail: **"Junk Email"** klasörü
- Yahoo: **"Spam"** klasörü

**Neden spam'a düşer?**
- Gmail, yeni gönderenlerden gelen email'leri spam olarak işaretleyebilir
- "fırat üni" gibi genel sender name'ler spam olarak algılanabilir

---

### 2. Supabase Dashboard'da Email Loglarını Kontrol Edin

1. **Supabase Dashboard** → **Logs** → **Auth Logs**
2. Son şifre sıfırlama isteğini bulun
3. Email gönderim durumunu kontrol edin

**Veya:**
1. **Settings** → **Authentication** → **SMTP Settings**
2. **"Send test email"** butonuna tıklayın
3. Email adresinizi girin
4. **Send** butonuna tıklayın
5. Email geldi mi kontrol edin

**Test email geldi mi?**
- ✅ Evet → SMTP çalışıyor, spam klasörünü kontrol edin
- ❌ Hayır → SMTP ayarlarında sorun var

---

### 3. Email Adresini Kontrol Edin

**Kontrol edin:**
- Email adresi doğru mu?
- Email adresi sistemde kayıtlı mı?
- Farklı bir email adresi ile deneyin

**Test için:**
1. Farklı bir email adresi ile "Şifremi Unuttum" deneyin
2. O email'e geliyor mu kontrol edin

---

### 4. Gmail Spam Filtrelemesi

Gmail, bazı email'leri otomatik olarak spam olarak işaretler. Çözüm:

**Gmail'de:**
1. Spam klasöründe email'i bulun
2. Email'i açın
3. **"Not spam"** butonuna tıklayın
4. Gelecekte bu gönderenin email'leri spam'a düşmez

**Veya:**
1. Gmail → **Settings** → **Filters and Blocked Addresses**
2. Yeni filter oluşturun
3. From: `eslemlestrange@gmail.com` veya `fırat üni`
4. **"Never send it to Spam"** seçeneğini işaretleyin

---

### 5. Supabase Email Template Kontrolü

1. **Authentication** → **Email Templates** → **Reset Password**
2. Template'in aktif olduğundan emin olun
3. Template içeriğini kontrol edin:
   - `{{ .ConfirmationURL }}` değişkeni olmalı
   - Link doğru formatta olmalı

---

### 6. Redirect URL Kontrolü

1. **Settings** → **Authentication** → **URL Configuration**
2. **Redirect URLs** bölümünde şu olmalı:
   ```
   com.smartattendance.app://reset-password
   ```
3. **Site URL** kontrol edin (opsiyonel)

---

### 7. Gmail Güvenlik Ayarları

Bazen Gmail, güvenlik nedeniyle email'leri engelleyebilir:

1. https://myaccount.google.com → **Security**
2. **"Less secure app access"** kontrol edin (artık yok)
3. **"2-Step Verification"** açık olmalı (App Password için)
4. **"Recent security activity"** kontrol edin

---

## 🧪 Test Adımları

### Adım 1: Test Email Gönderin

1. Supabase Dashboard → **SMTP Settings**
2. **"Send test email"** butonuna tıklayın
3. Email adresinizi girin
4. **Send** butonuna tıklayın
5. Email geldi mi kontrol edin (spam klasörünü de)

**Sonuç:**
- ✅ Test email geldi → SMTP çalışıyor, spam klasörünü kontrol edin
- ❌ Test email gelmedi → SMTP ayarlarında sorun var

### Adım 2: Android Uygulamasında Test Edin

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullanın
2. Email adresinizi girin
3. "Gönder" butonuna tıklayın
4. Email'inizi kontrol edin (spam klasörünü de)

### Adım 3: Farklı Email Adresi ile Test Edin

1. Farklı bir email adresi ile deneyin
2. O email'e geliyor mu kontrol edin
3. Eğer geliyorsa, ilk email adresinde sorun var

---

## 🔍 Supabase Dashboard'da Kontrol

### Auth Logs Kontrolü

1. **Supabase Dashboard** → **Logs** → **Auth Logs**
2. Son şifre sıfırlama isteğini bulun
3. Şunları kontrol edin:
   - Email gönderim durumu
   - Hata mesajları
   - Email adresi

### SMTP Test

1. **Settings** → **Authentication** → **SMTP Settings**
2. **"Send test email"** butonuna tıklayın
3. Email adresinizi girin
4. **Send** butonuna tıklayın
5. Sonucu kontrol edin

---

## ⚠️ Önemli Notlar

1. **Response Code 200** → Supabase isteği kabul etti, ama email gönderip göndermediğini garanti etmez
2. **Spam klasörü** → En yaygın neden
3. **Gmail spam filtrelemesi** → Yeni gönderenlerden gelen email'ler spam'a düşebilir
4. **Test email** → SMTP çalışıyor mu kontrol etmek için kullanın

---

## ✅ Çözüm Özeti

1. ✅ **Spam klasörünü kontrol edin** (en yaygın neden)
2. ✅ **Supabase Dashboard'da "Send test email" ile test edin**
3. ✅ **Gmail spam filtrelemesini kontrol edin**
4. ✅ **Farklı email adresi ile test edin**
5. ✅ **Supabase Auth Logs'u kontrol edin**

---

## 🆘 Hala Çalışmıyorsa

Eğer yukarıdaki adımlar işe yaramadıysa:

1. **Supabase Support** ile iletişime geçin
2. **Gmail Support** ile iletişime geçin
3. **Alternatif email sağlayıcısı** deneyin (SendGrid, Mailgun, vb.)

