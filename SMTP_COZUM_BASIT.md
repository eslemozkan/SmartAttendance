# SMTP Sorunu Çözümü - Basit Adımlar

## ❌ Sorun:
Email gönderilemiyor çünkü SMTP ayarları yanlış.

## ✅ Çözüm - 3 Seçenek:

### Seçenek 1: Gmail SMTP (En Kolay - 5 dakika)

**Adım 1: Gmail App Password Oluştur**
1. https://myaccount.google.com → **Security** (Güvenlik)
2. **2-Step Verification** (2 Adımlı Doğrulama) açık olmalı (yoksa açın)
3. **App Passwords** (Uygulama Şifreleri) → **Mail** → **Other** → "Supabase" yazın
4. **Generate** → 16 karakterlik şifre oluşur (örn: `abcd efgh ijkl mnop`)
5. **Boşlukları kaldırın:** `abcdefghijklmnop`

**Adım 2: Supabase'de Ayarla**
1. Supabase Dashboard → **Settings** → **Auth** → **SMTP Settings**
2. Şunları doldurun:
   ```
   Enable Custom SMTP: AÇIK (yeşil)
   Host: smtp.gmail.com
   Port: 587
   Username: eslem@gmail.com (TAM EMAIL ADRESİNİZ!)
   Password: abcdefghijklmnop (App Password - boşluksuz)
   Sender Email: eslem@gmail.com (Gmail adresiniz)
   Sender Name: SmartAttendance
   ```
3. **Save** butonuna tıklayın

**Adım 3: Test Et**
1. **"Send test email"** butonuna tıklayın
2. Email adresinizi girin
3. Email'inizi kontrol edin

---

### Seçenek 2: Email Göndermeden Çalıştır (Geçici Çözüm)

SMTP ayarlarını şimdilik yapmak istemiyorsanız, kullanıcılar "Şifremi Unuttum" dediğinde:
- İstek alındı mesajı gösterilir
- Email gelmez (SMTP yanlış olduğu için)
- Kullanıcı yönetici ile iletişime geçmeli

**Bu geçici bir çözümdür.** Production için SMTP ayarları yapılmalı.

---

### Seçenek 3: Resend Kullan (Ücretsiz - 3000 email/ay)

1. https://resend.com → Ücretsiz hesap oluştur
2. API Key al
3. Supabase Dashboard → **Settings** → **Auth** → **SMTP Settings**
4. Şunları doldurun:
   ```
   Host: smtp.resend.com
   Port: 587
   Username: resend
   Password: [Resend API Key]
   Sender Email: onboarding@resend.dev (veya doğrulanmış email)
   Sender Name: SmartAttendance
   ```

---

## 🎯 Hangi Seçeneği Seçmeliyim?

- **Gmail kullanıyorsanız:** Seçenek 1 (En kolay)
- **Hızlı çözüm istiyorsanız:** Seçenek 3 (Resend)
- **Şimdilik çalışsın yeter:** Seçenek 2 (Geçici)

---

## ⚠️ Önemli Notlar:

1. **Gmail için:**
   - ❌ Normal şifre çalışmaz
   - ✅ App Password gerekli
   - ✅ 2-Step Verification açık olmalı

2. **Username:**
   - ❌ `eslem` (yanlış)
   - ✅ `eslem@gmail.com` (doğru - tam email)

3. **Sender Email:**
   - Gmail SMTP kullanıyorsanız → Gmail adresi olmalı
   - Hotmail SMTP kullanıyorsanız → Hotmail adresi olmalı

---

## 🧪 Test:

SMTP ayarlarını yaptıktan sonra:
1. Supabase Dashboard → **"Send test email"**
2. Email'inizi kontrol edin
3. Email gelirse → ✅ Başarılı!
4. Email gelmezse → Ayarları tekrar kontrol edin




