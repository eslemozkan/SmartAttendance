# 🔧 Token Doğrulama Hatası Çözümü

## ❌ Sorun

**Hata:** `{"error":"requested path is invalid"}`

**URL:** `oubvhffqbsxsnbtinzbl.supabase.co/%20#access_token=...`

Bu, Supabase'in token doğrulama endpoint'ine yanlış yönlendirme yapıldığını gösteriyor.

---

## ✅ Çözüm 1: Site URL'i Düzeltin

### Supabase Dashboard'da:

1. **Settings** → **Authentication** → **URL Configuration**
2. **Site URL** alanına şunu yazın:
   ```
   com.smartattendance.app://
   ```
3. **Save changes** butonuna tıklayın

**ÖNEMLİ:** Site URL boş bırakılmamalı! `com.smartattendance.app://` olmalı.

---

## ✅ Çözüm 2: Redirect URL Kontrolü

### Supabase Dashboard'da:

1. **Settings** → **Authentication** → **URL Configuration**
2. **Redirect URLs** bölümünde şu olmalı:
   ```
   com.smartattendance.app://reset-password
   ```
3. **Save changes** butonuna tıklayın

---

## ✅ Çözüm 3: Android Uygulamasında Token Handling

Android uygulaması web URL'den token'ı doğru şekilde extract ediyor, ancak Supabase'in verify endpoint'ini çağırırken sorun olabilir.

**Kontrol edin:**
- ResetPasswordActivity'de `verifyTokenAndAuthenticate` fonksiyonu çalışıyor mu?
- Logcat'te hata mesajları var mı?

---

## 🧪 Test Et

### Adım 1: Site URL'i Düzeltin

1. Supabase Dashboard → **Settings** → **Authentication** → **URL Configuration**
2. **Site URL:** `com.smartattendance.app://`
3. **Save changes**

### Adım 2: Yeni Şifre Sıfırlama İsteği Gönderin

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullanın
2. Email adresinizi girin
3. "Gönder" butonuna tıklayın

### Adım 3: Email'deki Linke Tıklayın

1. Email'inizi açın (spam klasörünü kontrol edin)
2. "Reset Password" linkine tıklayın
3. **Artık Android uygulaması açılmalı ve token doğrulanmalı**

---

## 🔍 Logcat Kontrolü

Android Studio Logcat'te şunları kontrol edin:

```
ResetPasswordActivity: Deep link received: ...
ResetPasswordActivity: Reset password token received: ...
ResetPasswordActivity: Token verify response: 200
ResetPasswordActivity: Token verified successfully
```

**Eğer hata görüyorsanız:**
- Token verify response: 400 veya 500 → Token geçersiz veya süresi dolmuş
- "Invalid deep link format" → URL formatı yanlış

---

## ⚠️ Önemli Notlar

1. **Site URL boş bırakılmamalı** → `com.smartattendance.app://` olmalı
2. **Redirect URL doğru olmalı** → `com.smartattendance.app://reset-password`
3. **Yeni email isteği gönderin** → Eski email'ler çalışmayabilir
4. **Token süresi** → Genelde 1 saat, süresi dolmuşsa yeni istek gönderin

---

## 🆘 Hala Çalışmıyorsa

1. **Site URL:** `com.smartattendance.app://` (boş değil!)
2. **Redirect URL:** `com.smartattendance.app://reset-password`
3. **Yeni email isteği gönderin**
4. **Android uygulamasını yeniden derleyin**
5. **Logcat'te hataları kontrol edin**



