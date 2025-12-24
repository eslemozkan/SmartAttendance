# 🔍 Supabase SMTP Settings - Buton Bulma Rehberi

## 📍 SMTP Settings Sayfasına Git

### Adım 1: Supabase Dashboard'a Git

1. **Yeni bir sekme aç**
2. Şu adrese git: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl
3. **Giriş yap** (eğer giriş yapmadıysan)

### Adım 2: Settings'e Git

1. **Sol menüden** (en altta) **"Settings"** (⚙️ Ayarlar ikonu) seçeneğine tıkla
2. Veya direkt şu linke git: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/settings/auth

### Adım 3: Auth → SMTP Settings

1. **Settings** sayfasında, **sol menüden** **"Auth"** seçeneğine tıkla
2. **"SMTP Settings"** sekmesine tıkla
3. Veya direkt şu linke git: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/auth/providers

### Adım 4: SMTP Settings Sayfası

Bu sayfada göreceksin:

1. **"Enable Custom SMTP"** toggle (açık olmalı - yeşil)
2. **SMTP ayarları formu:**
   - SMTP Host
   - SMTP Port
   - SMTP User
   - SMTP Password
   - Sender Email
   - Sender Name
3. **"Save"** butonu (sağ üstte veya formun altında)

### Adım 5: "Send test email" Butonunu Bul

**"Send test email"** butonu genellikle:
- **"Save"** butonunun yanında
- Veya formun **altında**
- Veya **sağ üstte** bir buton olarak

**Eğer göremiyorsan:**
1. Sayfayı **aşağı kaydır**
2. **"Save"** butonuna tıkladıktan sonra görünebilir
3. Veya **sayfayı yenile** (F5)

---

## 🔄 Alternatif: Direkt Link

Eğer hala bulamıyorsan, direkt şu linke git:

https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/auth/providers

---

## 📸 Sayfa Görünümü

SMTP Settings sayfası şöyle görünmeli:

```
┌─────────────────────────────────────┐
│  Settings > Auth > SMTP Settings   │
├─────────────────────────────────────┤
│                                     │
│  ☑ Enable Custom SMTP              │
│                                     │
│  SMTP Host: [smtp.gmail.com]       │
│  SMTP Port: [587]                  │
│  SMTP User: [your-email@gmail.com] │
│  SMTP Password: [••••••••]         │
│  Sender Email: [your-email@gmail] │
│  Sender Name: [SmartAttendance]   │
│                                     │
│  [Save]  [Send test email]         │
│                                     │
└─────────────────────────────────────┘
```

---

## ❓ Hala Bulamıyorsan

1. **Sayfayı yenile** (F5)
2. **Tarayıcı cache'ini temizle**
3. **Farklı bir tarayıcı dene**
4. **Supabase Dashboard'un güncel versiyonunu kullandığından emin ol**

---

## 🎯 Hızlı Test (Buton Yoksa)

Eğer "Send test email" butonu yoksa, direkt Android uygulamasında test edebilirsin:

1. **Edge function'ı deploy et**
2. **Android uygulamasında "Şifremi Unuttum" özelliğini kullan**
3. **Email'inizi kontrol et**

Hangi adımdasın? Sayfayı görebiliyor musun?







