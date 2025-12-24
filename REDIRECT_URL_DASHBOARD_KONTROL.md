# 🔧 Redirect URL Dashboard Kontrolü

## ❌ Sorun

Dün email gönderiliyordu, bugün göndermemeye başladı. Muhtemelen Supabase Dashboard'da Redirect URL ayarı eksik.

---

## ✅ Çözüm: Supabase Dashboard'da Redirect URL Ekle

### Adım 1: Supabase Dashboard'a Git

1. https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl
2. **Settings** → **Authentication** → **URL Configuration**

### Adım 2: Redirect URLs Listesini Kontrol Et

**Redirect URLs** bölümünde şu URL **mutlaka** olmalı:

```
https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
```

### Adım 3: Eğer Yoksa Ekle

1. **Redirect URLs** alanına şu URL'i ekle:
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```

2. **Save changes** butonuna tıkla

### Adım 4: Site URL Kontrolü

**Site URL** bölümünde şunlardan biri olmalı:
- Boş bırakılabilir VEYA
- `https://oubvhffqbsxsnbtinzbl.supabase.co` VEYA
- `com.smartattendance.app://`

**ÖNEMLİ:** `http://localhost:3000` varsa kaldır!

---

## 🔍 Neden Bu Gerekli?

Supabase, güvenlik nedeniyle sadece **Redirect URLs** listesinde kayıtlı URL'lere email gönderir. Eğer `redirect_to` parametresinde gönderdiğin URL listede yoksa, Supabase email göndermez veya sessizce başarısız olur.

---

## 🧪 Test Et

### Adım 1: Redirect URL'i Ekle

1. Supabase Dashboard → Settings → Authentication → URL Configuration
2. Redirect URLs listesine şunu ekle:
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
3. **Save changes**

### Adım 2: Yeni İstek Gönder

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullan
2. Email adresini gir
3. "Gönder" butonuna tıkla

### Adım 3: Email'i Kontrol Et

1. **Inbox** klasörünü kontrol et
2. **Spam** klasörünü kontrol et

---

## ✅ Başarı Kriterleri

1. ✅ **Redirect URLs listesinde** `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page` var
2. ✅ **Save changes** butonuna tıklandı
3. ✅ **Yeni istek gönderildi**
4. ✅ **Email geldi**






