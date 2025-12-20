# 🔧 Redirect URL Sorunu Çözümü

## ❌ Sorun

Email'deki link `localhost:3000` adresine yönlendiriyor ve "This site can't be reached" hatası veriyor.

**Neden:** Supabase Dashboard'da Redirect URL yanlış ayarlanmış.

---

## ✅ Çözüm: Supabase Dashboard'da Redirect URL'i Düzeltin

### Adım 1: Supabase Dashboard'a Gidin

1. https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl
2. **Settings** → **Authentication** → **URL Configuration**

### Adım 2: Redirect URLs'i Kontrol Edin

**Redirect URLs** bölümünde şu olmalı:
```
com.smartattendance.app://reset-password
```

**Eğer `http://localhost:3000` varsa:**
- ❌ Kaldırın veya
- ✅ `com.smartattendance.app://reset-password` ekleyin

### Adım 3: Site URL'i Kontrol Edin (Opsiyonel)

**Site URL** bölümünde:
- Boş bırakılabilir VEYA
- `com.smartattendance.app://` olabilir

### Adım 4: Save Butonuna Tıklayın

Değişiklikleri kaydedin.

---

## 🔍 Kontrol Listesi

- [ ] **Redirect URLs:** `com.smartattendance.app://reset-password` var mı?
- [ ] **Site URL:** Boş veya `com.smartattendance.app://` mi?
- [ ] **Save** butonuna tıklandı mı?

---

## 🧪 Test Et

### Adım 1: Yeni Şifre Sıfırlama İsteği Gönderin

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullanın
2. Email adresinizi girin
3. "Gönder" butonuna tıklayın

### Adım 2: Email'deki Linke Tıklayın

1. Email'inizi açın (spam klasörünü kontrol edin)
2. "Reset Password" linkine tıklayın
3. **Artık `localhost:3000` yerine Android uygulaması açılmalı**

### Adım 3: Şifre Güncelleyin

1. Android uygulamasında ResetPasswordActivity görünmeli
2. Yeni şifrenizi girin
3. "Şifreyi Güncelle" butonuna tıklayın

---

## ⚠️ Önemli Notlar

1. **Redirect URL değişikliği** → Yeni email'lerde geçerli olur
2. **Eski email'ler** → Hala `localhost:3000`'e yönlendirebilir
3. **Yeni istek gönderin** → Redirect URL düzeltildikten sonra yeni email isteği gönderin

---

## 🔄 Alternatif: Web URL'den Token Extract Etme

Eğer Supabase Dashboard'da Redirect URL'i değiştiremiyorsanız, web URL'den token'ı extract edip kullanabiliriz. Ancak bu daha karmaşık ve önerilmez.

**Önerilen:** Supabase Dashboard'da Redirect URL'i düzeltin.

---

## ✅ Başarı Kriterleri

1. ✅ Supabase Dashboard'da Redirect URL: `com.smartattendance.app://reset-password`
2. ✅ Yeni email isteği gönderildi
3. ✅ Email'deki linke tıklayınca Android uygulaması açılıyor
4. ✅ `localhost:3000` hatası yok

---

## 🆘 Hala Çalışmıyorsa

1. **Supabase Dashboard'da Redirect URL'i kontrol edin** (yukarıdaki adımlar)
2. **Yeni email isteği gönderin** (eski email'ler çalışmayabilir)
3. **Android uygulamasını yeniden derleyin**
4. **Email template'ini kontrol edin** (Authentication → Email Templates → Reset Password)



