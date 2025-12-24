# 🧪 SMTP Test Rehberi

## ✅ SMTP Ayarları Yapılandırıldı!

Şimdi test edelim.

## 🔄 Adım 1: Edge Function'ı Deploy Et

### Yöntem A: Supabase Dashboard'dan (Kolay)

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions

2. **Edge Functions → reset-password** bölümüne git

3. **"Deploy"** butonuna tıkla

### Yöntem B: Terminal'den

```bash
supabase functions deploy reset-password
```

---

## 🧪 Adım 2: Test Email Gönder (Supabase Dashboard'dan)

1. **Supabase Dashboard → Settings → Auth → SMTP Settings**

2. **"Send test email"** butonuna tıkla

3. **Email adresini gir** (kendi email'ini)

4. **"Send"** butonuna tıkla

5. **Email'inizi kontrol edin** (spam klasörünü de!)

✅ **Test email geldi mi?** → SMTP ayarları doğru!

---

## 📱 Adım 3: Android Uygulamasında Test Et

1. **Android uygulamasını aç**

2. **Login ekranına git**

3. **"Şifremi Unuttum"** linkine tıkla

4. **Email adresini gir** (kayıtlı bir email)

5. **"Gönder"** butonuna tıkla

6. **Email'inizi kontrol edin!**

---

## 🔍 Kontrol Listesi

### Supabase Dashboard'da Logları Kontrol Et

1. **Edge Functions → reset-password → Logs**

2. **Son isteği bul** ve şunları ara:
   - `"Attempting to send email via Supabase Auth API..."`
   - `"Supabase Auth API response status: 200"`
   - `"Email sent via Supabase Auth API"`

### Android Logcat'te Kontrol Et

```
ApiService: Response Code: 200
ApiService: Password reset response parsed: ok=true, emailSent=true
```

---

## ❌ Sorun Giderme

### ❌ Test email gelmedi
**Kontrol et:**
1. ✅ SMTP ayarları doğru mu?
2. ✅ App Password doğru kopyalandı mı? (boşluklar olmadan)
3. ✅ Spam klasörünü kontrol ettin mi?

### ❌ "Authentication failed" hatası
**Sebep:** App Password yanlış
**Çözüm:**
1. Yeni bir App Password oluştur
2. Boşlukları kaldırarak kopyala
3. Supabase'e yapıştır

### ❌ Android'de email gelmedi
**Kontrol et:**
1. ✅ Test email geldi mi? (Supabase Dashboard'dan)
2. ✅ Edge function deploy edildi mi?
3. ✅ Supabase loglarında hata var mı?

---

## ✅ Başarı Kriterleri

✅ **Test email geldi** → SMTP çalışıyor!
✅ **Android'de email geldi** → Her şey hazır!

Test sonuçlarını paylaş, birlikte kontrol edelim!







