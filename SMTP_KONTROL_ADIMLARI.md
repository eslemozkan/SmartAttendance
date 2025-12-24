# 🔧 SMTP Ayarları Kontrolü

## 📋 Kontrol Listesi

### 1. Supabase Dashboard → Settings → Authentication → SMTP Settings

**Şunları kontrol et:**

- ✅ **SMTP Enabled:** Açık olmalı (toggle switch)
- ✅ **SMTP Host:** `smtp.gmail.com` (veya başka bir SMTP sunucusu)
- ✅ **SMTP Port:** `465` (SSL) veya `587` (TLS)
- ✅ **SMTP User:** Gmail adresiniz (örn: `eslem@gmail.com`)
- ✅ **SMTP Password:** Gmail App Password (16 haneli şifre, normal şifre değil!)
- ✅ **Sender Email:** Gönderen email adresi (SMTP User ile aynı olmalı)
- ✅ **Sender Name:** Gönderen ismi (örn: "SmartAttendance")

**ÖNEMLİ:** 
- SMTP Password normal Gmail şifresi değil, **App Password** olmalı
- App Password oluşturmak için Gmail'de 2FA açık olmalı

---

### 2. Gmail App Password Oluşturma

Eğer SMTP Password yanlışsa veya App Password yoksa:

1. **Google Account → Security** bölümüne git
2. **2-Step Verification** açık olmalı (yoksa aç)
3. **App Passwords** bölümüne git
4. **"Select app"** → **"Mail"** seç
5. **"Select device"** → **"Other (Custom name)"** seç
6. **"Supabase"** yaz ve **Generate** butonuna tıkla
7. **Oluşturulan 16 haneli şifreyi kopyala** (örn: `abcd efgh ijkl mnop`)
8. **Supabase Dashboard → SMTP Settings → SMTP Password** alanına yapıştır
9. **Save** butonuna tıkla

---

### 3. Email Template Kontrolü

1. **Supabase Dashboard → Settings → Authentication → Email Templates**
2. **"Reset Password"** template'ini aç
3. **Template içeriğinde şu değişken olmalı:**
   ```
   {{ .ConfirmationURL }}
   ```
4. **Template aktif mi kontrol et**

---

### 4. Test Et

1. **SMTP ayarlarını düzelt**
2. **Save** butonuna tıkla
3. **60 saniye bekle** (rate limiting)
4. **Yeni bir şifre sıfırlama isteği gönder**
5. **Email'i kontrol et:**
   - Inbox
   - Spam/Junk
   - Promotions (Gmail'de)

---

## 🔍 Sorun Giderme

### Email hala gelmiyorsa:

1. **Supabase Dashboard → Logs → Auth Logs** kontrol et
2. **Email gönderme ile ilgili hata var mı bak**
3. **SMTP test email gönder:**
   - Supabase Dashboard → Settings → Authentication → SMTP Settings
   - "Send test email" butonuna tıkla
   - Test email geldi mi kontrol et

### SMTP test email gelmiyorsa:

- SMTP ayarları yanlış olabilir
- Gmail App Password yanlış olabilir
- 2FA kapalı olabilir
- Gmail hesabı kısıtlanmış olabilir

---

## ✅ Başarı Kriterleri

1. ✅ **SMTP Enabled:** Açık
2. ✅ **SMTP Password:** Gmail App Password (16 haneli)
3. ✅ **2FA:** Açık
4. ✅ **Email Template:** Aktif ve `{{ .ConfirmationURL }}` var
5. ✅ **Test email:** Geldi
6. ✅ **Şifre sıfırlama email'i:** Geldi






