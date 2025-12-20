# 📧 Email Gönderme Sorunu Çözümü

## ❌ Sorun

Response 200 dönüyor ama email gelmiyor:
```
Reset password response: 200 - {}
```

---

## ✅ Çözüm Adımları

### 1. Supabase Dashboard'da SMTP Ayarlarını Kontrol Et

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl

2. **Settings → Authentication → SMTP Settings** bölümüne git

3. **Şunları kontrol et:**
   - ✅ **SMTP Enabled:** Açık olmalı
   - ✅ **SMTP Host:** `smtp.gmail.com` (veya başka bir SMTP sunucusu)
   - ✅ **SMTP Port:** `465` veya `587`
   - ✅ **SMTP User:** Gmail adresiniz (örn: `eslem@gmail.com`)
   - ✅ **SMTP Password:** Gmail App Password (normal şifre değil!)
   - ✅ **Sender Email:** Gönderen email adresi
   - ✅ **Sender Name:** Gönderen ismi

4. **Eğer SMTP ayarları yoksa veya yanlışsa:**
   - Gmail App Password oluştur (2FA açık olmalı)
   - SMTP ayarlarını düzelt
   - **Save** butonuna tıkla

---

### 2. Rate Limiting Kontrolü

Supabase rate limiting yapıyor olabilir. **60 saniye bekle** ve tekrar dene.

---

### 3. Email Klasörlerini Kontrol Et

1. **Inbox** klasörünü kontrol et
2. **Spam/Junk** klasörünü kontrol et
3. **Promotions** klasörünü kontrol et (Gmail'de)

---

### 4. Email Adresini Kontrol Et

1. **Supabase Dashboard → Authentication → Users** bölümüne git
2. **Email adresinin kayıtlı olup olmadığını kontrol et**
3. Eğer kayıtlı değilse, önce kayıt ol veya test için kayıtlı bir email kullan

---

### 5. Supabase Dashboard'da Email Logs Kontrol Et

1. **Supabase Dashboard → Logs → Edge Function Logs** bölümüne git
2. **Son şifre sıfırlama isteğini kontrol et**
3. **Hata mesajları var mı bak**

---

## 🧪 Test Et

### Adım 1: SMTP Ayarlarını Düzelt

1. Supabase Dashboard → Settings → Authentication → SMTP Settings
2. SMTP ayarlarını kontrol et ve düzelt
3. **Save** butonuna tıkla

### Adım 2: 60 Saniye Bekle

Rate limiting'i önlemek için 60 saniye bekle.

### Adım 3: Yeni İstek Gönder

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullan
2. Email adresini gir
3. "Gönder" butonuna tıkla

### Adım 4: Email'i Kontrol Et

1. **Inbox** klasörünü kontrol et
2. **Spam** klasörünü kontrol et
3. **Promotions** klasörünü kontrol et (Gmail'de)

---

## 🔍 Gmail App Password Oluşturma

Eğer SMTP ayarlarında Gmail kullanıyorsan:

1. **Google Account → Security** bölümüne git
2. **2-Step Verification** açık olmalı
3. **App Passwords** bölümüne git
4. **"Select app"** → **"Mail"** seç
5. **"Select device"** → **"Other (Custom name)"** seç
6. **"Supabase"** yaz ve **Generate** butonuna tıkla
7. **Oluşturulan 16 haneli şifreyi kopyala**
8. **Supabase Dashboard → SMTP Settings → SMTP Password** alanına yapıştır

---

## ⚠️ Önemli Notlar

1. **Gmail App Password** kullanmalısın (normal şifre değil!)
2. **2FA açık olmalı** (Gmail App Password için gerekli)
3. **Rate limiting:** 60 saniye bekle ve tekrar dene
4. **Email spam klasörüne düşebilir**

---

## ✅ Başarı Kriterleri

1. ✅ **SMTP ayarları doğru**
2. ✅ **60 saniye beklendi**
3. ✅ **Yeni istek gönderildi**
4. ✅ **Email geldi** (inbox veya spam klasöründe)



