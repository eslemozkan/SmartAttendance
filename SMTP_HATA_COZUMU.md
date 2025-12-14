# 🔧 SMTP Hatası Çözümü

## ❌ Hata Mesajı

```
Response Code: 500
Response Body: {"code":500,"error_code":"unexpected_failure","msg":"Error sending recovery email"}
```

Bu hata, Supabase'in email göndermeye çalıştığı ama başarısız olduğu anlamına gelir.

## ✅ Çözüm: SMTP Ayarlarını Yapılandırın

### 1. Supabase Dashboard'a Gidin

1. https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl
2. **Settings** → **Authentication** → **SMTP Settings**

### 2. SMTP Ayarlarını Yapılandırın

**Enable Custom SMTP:** AÇIK (yeşil toggle)

**SMTP Provider Settings:**
- **Host:** `smtp.gmail.com` (Gmail kullanıyorsanız)
- **Port:** `587` (veya `465`)
- **Username:** Gmail adresiniz (örn: `eslem@gmail.com`)
- **Password:** Gmail App Password (16 karakterlik, boşluksuz)
- **Sender Email:** Gmail adresiniz (aynı)
- **Sender Name:** İstediğiniz isim (örn: "SmartAttendance")

### 3. Gmail App Password Nasıl Alınır?

1. https://myaccount.google.com → **Security**
2. **2-Step Verification** açık olmalı
3. **App Passwords** → **Mail** → **Other** → "Supabase" yazın
4. **Generate** → 16 karakterlik şifre oluşur
5. Boşlukları kaldırıp Supabase'e yapıştırın

### 4. Test Email Gönderin

1. SMTP Settings sayfasında
2. **"Send test email"** butonuna tıklayın
3. Email adresinizi girin
4. **Send** butonuna tıklayın
5. Email'inizi kontrol edin

**Email geldi mi?**
- ✅ Evet → SMTP ayarları doğru, tekrar deneyin
- ❌ Hayır → SMTP ayarlarını kontrol edin

---

## 🔍 Kontrol Listesi

- [ ] Enable Custom SMTP: AÇIK
- [ ] Host: `smtp.gmail.com` (veya kullandığınız servis)
- [ ] Port: `587` veya `465`
- [ ] Username: Tam email adresi (örn: `eslem@gmail.com`)
- [ ] Password: App Password (16 karakterlik, boşluksuz)
- [ ] Sender Email: Username ile aynı
- [ ] Sender Name: Dolu
- [ ] "Send test email" başarılı

---

## ⚠️ Önemli Notlar

1. **Normal Gmail şifresi çalışmaz** → App Password gerekli
2. **2-Step Verification açık olmalı** → App Password almak için
3. **Username tam email adresi olmalı** → `eslem@gmail.com` (sadece `eslem` değil)
4. **Sender Email ile Username aynı olmalı** → Gmail için

---

## 🧪 Test Et

SMTP ayarlarını yaptıktan sonra:

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullanın
2. Email adresinizi girin
3. "Gönder" butonuna tıklayın
4. Email'inizi kontrol edin (spam klasörünü de)

---

## 📝 Alternatif: Test İçin Email Göndermeden Devam Et

Eğer SMTP yapılandırmak istemiyorsanız, şifre sıfırlama linkini manuel olarak gösterebiliriz. Ancak bu production için önerilmez.

---

## ✅ Tamamlandı!

SMTP ayarlarını yaptıktan sonra şifre sıfırlama özelliği çalışmalı.

