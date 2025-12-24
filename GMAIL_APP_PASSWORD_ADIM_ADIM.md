# 📧 Gmail App Password - Adım Adım Rehber

## ✅ Adım 1: 2-Step Verification Açık (Tamamlandı!)

Görüyorum ki 2-Step Verification zaten açık. Şimdi App Password oluşturalım.

---

## 🔑 Adım 2: App Password Oluştur

### 2.1. App Passwords Sayfasına Git

1. **Aynı sayfada (Güvenlik sayfasında) aşağı kaydır**
2. **"2 Adımlı Doğrulama"** bölümünü bul
3. **"2 Adımlı Doğrulama"** yazısına **tıkla** (yeşil tik işaretinin yanındaki metne)

**VEYA** direkt şu linke git:
- https://myaccount.google.com/apppasswords

### 2.2. App Password Oluştur

1. **"Uygulama seçin"** (Select app) dropdown'ına tıkla
2. **"Mail"** seçeneğini seç
3. **"Cihaz seçin"** (Select device) dropdown'ına tıkla
4. **"Diğer (Özel ad)"** (Other (Custom name)) seçeneğini seç
5. **"SmartAttendance"** yaz (veya istediğin bir isim)
6. **"Oluştur"** (Create) butonuna tıkla

### 2.3. App Password'i Kopyala

1. **16 karakterlik şifre** gösterilecek (örn: `abcd efgh ijkl mnop`)
2. **"Kopyala"** butonuna tıkla
3. **ÖNEMLİ:** Bu şifreyi bir yere kaydet! Sadece bir kez gösterilir!

---

## ⚙️ Adım 3: Supabase SMTP Ayarları

### 3.1. Supabase Dashboard'a Git

1. **Yeni bir sekme aç**
2. Şu adrese git: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl
3. **Settings → Auth → SMTP Settings** bölümüne git

### 3.2. SMTP Ayarlarını Yapılandır

1. **"Enable Custom SMTP"** toggle'ını **AÇIK** yap (yeşil olmalı)

2. Şu bilgileri doldur:
   - **SMTP Host:** `smtp.gmail.com`
   - **SMTP Port:** `587`
   - **SMTP User:** Gmail adresin (örn: `eslemlestrange@gmail.com`)
   - **SMTP Password:** Az önce kopyaladığın **App Password** (16 karakterlik)
   - **Sender Email:** Aynı Gmail adresin
   - **Sender Name:** `SmartAttendance`

3. **"Save"** butonuna tıkla

---

## 🧪 Adım 4: Test Email Gönder

1. **Aynı sayfada (SMTP Settings)**
2. **"Send test email"** butonuna tıkla
3. Email adresini gir (kendi email'ini)
4. **"Send"** butonuna tıkla
5. **Email'inizi kontrol edin** (spam klasörünü de!)

---

## ✅ Adım 5: Edge Function'ı Deploy Et

SMTP ayarları yapılandırıldıktan sonra:

1. **Supabase Dashboard → Edge Functions → reset-password**
2. **"Deploy"** butonuna tıkla

**VEYA** terminalden:
```bash
supabase functions deploy reset-password
```

---

## 🎯 Adım 6: Android'de Test Et

1. **Android uygulamasını aç**
2. **Login ekranına git**
3. **"Şifremi Unuttum"** linkine tıkla
4. **Email adresini gir**
5. **"Gönder"** butonuna tıkla
6. **Email'inizi kontrol edin!**

---

## ❌ Sorun Giderme

### "App Passwords" seçeneği görünmüyor
**Sebep:** 2-Step Verification yeni açılmış olabilir
**Çözüm:** Birkaç dakika bekle ve tekrar dene

### "Authentication failed" hatası
**Sebep:** App Password yanlış kopyalanmış
**Çözüm:** 
1. Yeni bir App Password oluştur
2. Boşlukları kaldırarak kopyala (örn: `abcdefghijklmnop`)
3. Supabase'e yapıştır

### Email gelmiyor
**Kontrol et:**
1. ✅ Test email geldi mi? (Supabase Dashboard'dan)
2. ✅ Spam klasörünü kontrol ettin mi?
3. ✅ SMTP ayarları doğru mu?
4. ✅ Edge function deploy edildi mi?

---

## 📝 Özet

1. ✅ 2-Step Verification açık (tamamlandı)
2. ⏳ App Password oluştur
3. ⏳ Supabase SMTP ayarlarını yapılandır
4. ⏳ Test email gönder
5. ⏳ Edge function'ı deploy et
6. ⏳ Android'de test et

Hangi adımdasın? Yardıma ihtiyacın olursa söyle!







