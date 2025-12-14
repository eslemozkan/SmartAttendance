# 🔧 SMTP Ayarlarındaki Hatalar ve Düzeltmeler

## ❌ Tespit Edilen Hatalar

### 1. **Username Yanlış** ⚠️ KRİTİK

**Şu an:** `eslem`  
**Olması gereken:** `eslemlestrange@gmail.com` (tam email adresi)

**Neden:** Gmail SMTP, username olarak tam email adresini bekler. Sadece kullanıcı adı (`eslem`) çalışmaz.

### 2. **Username ile Sender Email Eşleşmiyor** ⚠️ KRİTİK

- **Username:** `eslem` (yanlış)
- **Sender Email:** `eslemlestrange@gmail.com` (doğru)

**Neden:** Gmail SMTP için username ve sender email aynı olmalı.

### 3. **Password Tipi** ⚠️ ÖNEMLİ

**Kontrol edin:** Password Gmail App Password mi?
- ✅ **App Password:** 16 karakterlik, boşluksuz (örn: `abcd efgh ijkl mnop` → `abcdefghijklmnop`)
- ❌ **Normal Gmail şifresi:** Çalışmaz!

### 4. **Port Numarası** ℹ️ OPSİYONEL

**Şu an:** `465`  
**Alternatif:** `587` (daha yaygın, TLS için)

Her ikisi de çalışır, ama `587` daha yaygın kullanılır.

---

## ✅ Düzeltme Adımları

### 1. Supabase Dashboard'da SMTP Ayarlarını Düzeltin

1. **Supabase Dashboard** → **Settings** → **Authentication** → **SMTP Settings**

2. **Username** alanını düzeltin:
   - ❌ Eski: `eslem`
   - ✅ Yeni: `eslemlestrange@gmail.com`

3. **Password** kontrol edin:
   - Gmail App Password kullanıyor musunuz?
   - 16 karakterlik, boşluksuz olmalı
   - Normal Gmail şifresi çalışmaz!

4. **Port** (opsiyonel):
   - `465` → `587` (daha yaygın)

5. **"Save"** butonuna tıklayın

### 2. Gmail App Password Nasıl Alınır?

Eğer App Password kullanmıyorsanız:

1. https://myaccount.google.com → **Security**
2. **2-Step Verification** açık olmalı
3. **App Passwords** → **Mail** → **Other** → "Supabase" yazın
4. **Generate** → 16 karakterlik şifre oluşur
5. Boşlukları kaldırıp Supabase'e yapıştırın

### 3. Test Email Gönderin

1. SMTP Settings sayfasında
2. **"Send test email"** butonuna tıklayın
3. Email adresinizi girin
4. **Send** butonuna tıklayın
5. Email'inizi kontrol edin

**Email geldi mi?**
- ✅ Evet → SMTP ayarları doğru, Android uygulamasında test edin
- ❌ Hayır → Yukarıdaki adımları tekrar kontrol edin

---

## 📋 Kontrol Listesi

- [ ] **Username:** `eslemlestrange@gmail.com` (tam email adresi)
- [ ] **Sender Email:** `eslemlestrange@gmail.com` (username ile aynı)
- [ ] **Password:** Gmail App Password (16 karakterlik, boşluksuz)
- [ ] **Host:** `smtp.gmail.com`
- [ ] **Port:** `587` veya `465`
- [ ] **2-Step Verification:** Açık (App Password için gerekli)
- [ ] **"Send test email"** başarılı

---

## 🔍 Supabase Dashboard'da Redirect URL Kontrolü

SMTP ayarları dışında, Redirect URL'in de ayarlanmış olması gerekiyor:

1. **Settings** → **Authentication** → **URL Configuration**
2. **Redirect URLs** bölümüne şunu ekleyin:
   ```
   com.smartattendance.app://reset-password
   ```
3. **Save** butonuna tıklayın

---

## 🧪 Test Et

1. **SMTP ayarlarını düzeltin** (yukarıdaki adımlar)
2. **"Send test email"** ile test edin
3. **Android uygulamasında** "Şifremi Unuttum" özelliğini kullanın
4. **Email'inizi kontrol edin** (spam klasörünü de)

---

## ⚠️ Önemli Notlar

1. **Username tam email adresi olmalı** → `eslemlestrange@gmail.com` (sadece `eslem` değil)
2. **Username ve Sender Email aynı olmalı** → Gmail için zorunlu
3. **App Password gerekli** → Normal Gmail şifresi çalışmaz
4. **2-Step Verification açık olmalı** → App Password almak için

---

## ✅ Düzeltme Sonrası

SMTP ayarlarını düzelttikten sonra:
- ✅ "Send test email" başarılı olmalı
- ✅ Android uygulamasında şifre sıfırlama çalışmalı
- ✅ Email gönderimi başarılı olmalı

