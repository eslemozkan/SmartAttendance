# 🔧 Site URL Düzeltmesi

## ❌ Sorun

**Site URL** alanında yanlış/eksik URL var:
```
https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-pas
```

**Doğru URL şu olmalı:**
```
https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
```

---

## ✅ Çözüm

### Adım 1: Supabase Dashboard'a Git

1. **Settings → Authentication → URL Configuration**

### Adım 2: Site URL'i Düzelt

1. **Site URL** alanındaki mevcut değeri sil
2. Şu URL'i yaz:
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
3. **Save changes** butonuna tıkla

---

## 🔍 Neden Bu Önemli?

**Site URL**, Supabase'in email template'lerinde kullanılıyor ve email gönderme mekanizması için kritik. Eğer yanlışsa:
- Supabase email göndermeyi reddedebilir
- Email'deki link yanlış oluşturulabilir
- Email gönderilse bile link çalışmayabilir

---

## ✅ Kontrol Listesi

- [ ] **Site URL:** `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`
- [ ] **Redirect URLs:** Her iki URL de listede (`com.smartattendance.app://reset-password` ve `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`)
- [ ] **Save changes** butonuna tıklandı

---

## 🧪 Test Et

1. **Site URL'i düzelt**
2. **Save changes**
3. **10-15 dakika bekle** (rate limiting)
4. **Yeni bir şifre sıfırlama isteği gönder**
5. **Email'i kontrol et**

---

## ✅ Başarı Kriterleri

1. ✅ **Site URL düzeltildi**
2. ✅ **Save changes yapıldı**
3. ✅ **Yeni istek gönderildi**
4. ✅ **Email geldi**
