# 🔐 Supabase Password Reset Setup

## ✅ Yapılan Değişiklikler

Artık **Supabase'in built-in password reset mekanizması** kullanılıyor:
- ❌ Edge Function yok
- ❌ SMTP manuel yapılandırma yok
- ❌ Email manuel gönderme yok
- ✅ Supabase'in kendi sistemi kullanılıyor

## 📋 Supabase Dashboard Ayarları

### 1. Redirect URL Ayarla

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl

2. **Settings → Authentication → URL Configuration** bölümüne git

3. **Redirect URLs** bölümüne şunu ekle:
   ```
   com.smartattendance.app://reset-password
   ```

4. **Site URL** kontrol et (opsiyonel):
   - `com.smartattendance.app://` olmalı (veya boş bırakılabilir)

5. **Save** butonuna tıkla

### 2. Email Template Kontrol Et

1. **Authentication → Email Templates** bölümüne git

2. **"Reset Password"** template'inin aktif olduğundan emin ol

3. Template içeriğini kontrol et:
   - `{{ .ConfirmationURL }}` değişkeni olmalı
   - Bu değişken otomatik olarak reset link'ini içerir

---

## 🧪 Test Et

### 1. Android Uygulamasında

1. Uygulamayı aç
2. Login ekranına git
3. "Şifremi Unuttum" linkine tıkla
4. Email adresini gir
5. "Gönder" butonuna tıkla

### 2. Email Kontrol Et

1. Email'inizi kontrol et (spam klasörünü de)
2. Email'deki "Reset Password" linkine tıkla
3. Android uygulaması otomatik açılmalı
4. Yeni şifre gir
5. "Şifreyi Güncelle" butonuna tıkla

---

## 🔧 Nasıl Çalışıyor?

### 1. Forgot Password (LoginActivity)

```kotlin
SupabaseClient.client.auth.resetPasswordForEmail(
    email = email,
    redirectTo = "com.smartattendance.app://reset-password"
)
```

- Supabase otomatik olarak email gönderir
- Email'deki link `com.smartattendance.app://reset-password` deep link'ini içerir
- Android uygulaması bu deep link'i yakalar

### 2. Reset Password (ResetPasswordActivity)

```kotlin
SupabaseClient.client.auth.updateUser {
    password = newPassword
}
```

- Kullanıcı email'deki linke tıkladığında zaten authenticate olmuş durumda
- Sadece yeni şifreyi girer ve günceller
- Tekrar login yapmasına gerek yok

---

## ⚠️ Önemli Notlar

1. **Redirect URL mutlaka ayarlanmalı:**
   - Supabase Dashboard → Settings → Authentication → Redirect URLs
   - `com.smartattendance.app://reset-password` eklenmeli

2. **Email gönderimi:**
   - Supabase varsayılan olarak email göndermez (SMTP yapılandırması gerekir)
   - Ancak test için Supabase Dashboard'dan "Send test email" kullanılabilir
   - Production için SMTP yapılandırması önerilir (ama zorunlu değil)

3. **Deep Link:**
   - AndroidManifest.xml'de intent filter tanımlı
   - `com.smartattendance.app://reset-password` scheme'i kullanılıyor

---

## 🐛 Sorun Giderme

### Email gelmiyor

1. **Supabase Dashboard → Logs** kontrol et
2. **Authentication → Email Templates** kontrol et
3. **Settings → Authentication → SMTP Settings** kontrol et (opsiyonel)

### Deep link çalışmıyor

1. **AndroidManifest.xml** kontrol et
2. **Redirect URL** Supabase Dashboard'da doğru mu kontrol et
3. **Email'deki link** doğru mu kontrol et

### Şifre güncellenemiyor

1. **Token süresi dolmuş olabilir** (genelde 1 saat)
2. **Yeni bir reset password isteği gönder**
3. **Email'deki en son link'i kullan**

---

## ✅ Tamamlandı!

Artık Supabase'in built-in password reset mekanizması kullanılıyor. Edge function'a veya manuel email gönderimine gerek yok!

