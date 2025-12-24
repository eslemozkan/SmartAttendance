# 🔧 Final Password Reset Fix

## ❌ Sorun

Email'deki link'e tıklayınca hala `about:blank` görünüyor. Supabase'in verify endpoint'i redirect_to'yu doğru kullanmıyor olabilir.

---

## ✅ Çözüm: Site URL Ayarlama

Supabase Dashboard'da **Site URL** ayarını web sayfası URL'ine ayarlayın:

### Adımlar:

1. **Supabase Dashboard → Settings → Authentication → URL Configuration**
2. **Site URL** bölümüne şunu yazın:
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
3. **Redirect URLs** bölümüne de aynı URL'i ekleyin:
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
4. **Save** butonuna tıklayın

---

## 🧪 Test Et

1. **Yeni bir şifre sıfırlama isteği gönderin** (Android uygulamasından)
2. **Email'inizi kontrol edin**
3. **Email'deki link'e tıklayın**
4. **Web sayfası açılmalı** (about:blank değil)

---

## 📝 Notlar

- **Site URL** Supabase'in email'lerinde kullanılan base URL'dir
- Email'deki link formatı: `{Site URL}/auth/v1/verify?token=xxx&type=recovery`
- Verify endpoint'i token'ı verify edip Site URL'e yönlendirir
- Bu yüzden Site URL'i web sayfası URL'ine ayarlamak önemli

---

## ✅ Başarı Kriterleri

1. ✅ **Site URL ayarlandı**
2. ✅ **Redirect URL ayarlandı**
3. ✅ **Yeni email isteği gönderildi**
4. ✅ **Email link'i web sayfasına yönlendiriyor**
5. ✅ **about:blank görünmüyor**






