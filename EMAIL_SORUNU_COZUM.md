# 🔧 Email Gönderme Sorunu - Dün Çalışıyordu, Bugün Çalışmıyor

## 📋 Durum

- ✅ Dün email gönderiliyordu
- ❌ Bugün email gönderilmiyor
- ✅ SMTP ayarları değişmedi
- ✅ API isteği başarılı (200 OK)
- ✅ `/recover request completed` logları var

---

## 🔍 Olası Nedenler

### 1. Rate Limiting (En Olası)

Supabase rate limiting yapıyor olabilir. Çok fazla istek gönderildiğinde email göndermeyi durdurur.

**Çözüm:**
- **10-15 dakika bekle**
- Sonra tekrar dene

---

### 2. Supabase Email Servisi Geçici Sorun

Supabase'in email gönderme servisi geçici olarak çalışmıyor olabilir.

**Kontrol:**
- Supabase Dashboard → Logs → Auth Logs
- Email gönderme ile ilgili hata var mı bak
- **"Error sending recovery email"** gibi bir hata var mı?

---

### 3. Email Gönderiliyor Ama Gelmiyor

Email gönderiliyor olabilir ama:
- Farklı bir klasöre düşüyor
- Email servisi tarafından bloke ediliyor
- Gmail'in spam filtreleri çok sıkı

**Kontrol:**
- **Tüm klasörleri kontrol et:** Inbox, Spam, Promotions, Updates, Social
- **Gmail'de "All Mail" klasörünü kontrol et**
- **Email arama yap:** "Supabase" veya "password reset" ara

---

### 4. Redirect URL Değişikliği

Bugün `redirect_to` URL'ini değiştirdik:
- **Eski:** `com.smartattendance.app://reset-password` (muhtemelen)
- **Yeni:** `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`

**Kontrol:**
- Supabase Dashboard → Settings → Authentication → URL Configuration
- **Redirect URLs** listesinde **her iki URL de** var mı?
- Eğer eski URL kaldırıldıysa, Supabase email göndermeyi reddedebilir

---

## ✅ Çözüm Adımları

### Adım 1: Rate Limiting İçin Bekle

1. **10-15 dakika bekle**
2. Sonra tekrar dene

### Adım 2: Redirect URLs Kontrolü

1. Supabase Dashboard → Settings → Authentication → URL Configuration
2. **Redirect URLs** listesinde şunların **her ikisi de** olmalı:
   ```
   com.smartattendance.app://reset-password
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
3. Eğer eski URL yoksa, **ekle**
4. **Save changes**

### Adım 3: Email Klasörlerini Kontrol Et

1. Gmail'de **"All Mail"** klasörünü aç
2. **"Supabase"** veya **"password reset"** ara
3. Son 1 saat içindeki email'leri kontrol et

### Adım 4: Supabase Logs Kontrolü

1. Supabase Dashboard → Logs → Auth Logs
2. **Email gönderme ile ilgili hata var mı bak**
3. **"Error sending recovery email"** gibi bir hata var mı?

---

## 🧪 Test Et

### Adım 1: 10-15 Dakika Bekle

Rate limiting'i önlemek için bekle.

### Adım 2: Redirect URLs'i Kontrol Et ve Düzelt

1. Supabase Dashboard → Settings → Authentication → URL Configuration
2. Redirect URLs listesine **her iki URL'i de ekle**
3. Save changes

### Adım 3: Yeni İstek Gönder

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullan
2. Email adresini gir
3. "Gönder" butonuna tıkla

### Adım 4: Email'i Kontrol Et

1. **All Mail** klasörünü kontrol et
2. **"Supabase"** veya **"password reset"** ara
3. **Spam** klasörünü kontrol et

---

## ⚠️ Önemli Notlar

1. **Rate limiting:** Çok fazla istek gönderildiğinde Supabase email göndermeyi durdurur
2. **Redirect URLs:** Her iki URL de listede olmalı (eski ve yeni)
3. **Email klasörleri:** Email farklı bir klasöre düşmüş olabilir

---

## ✅ Başarı Kriterleri

1. ✅ **10-15 dakika beklendi**
2. ✅ **Redirect URLs listesinde her iki URL var**
3. ✅ **Yeni istek gönderildi**
4. ✅ **Email geldi** (All Mail veya Spam klasöründe)






