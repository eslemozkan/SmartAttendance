# 🔍 Email Gönderim Sorun Giderme

## ❌ Sorun: Email Gönderilmiyor

Loglardan görüldüğü üzere:
- ✅ Edge function çalışıyor (Response Code: 200)
- ✅ Reset link oluşturuldu
- ❌ `emailSent: false` - Email gönderilmedi

## 🔍 Kontrol Listesi

### 1. Supabase Dashboard'da Edge Function Loglarını Kontrol Et

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions

2. **Edge Functions → reset-password → Logs** bölümüne git

3. **Son isteği bul** ve şunları ara:
   - `"Attempting to send email via Supabase Auth API..."`
   - `"Supabase Auth API response status: ..."`
   - `"Supabase Auth API response body: ..."`
   - `"Email sent via Supabase Auth API"` VEYA hata mesajı

### 2. SMTP Ayarlarını Kontrol Et

1. **Supabase Dashboard → Settings → Auth → SMTP Settings**

2. **Kontrol et:**
   - ✅ **Enable Custom SMTP:** Açık olmalı (yeşil toggle)
   - ✅ **SMTP Host:** `smtp.gmail.com`
   - ✅ **SMTP Port:** `587`
   - ✅ **SMTP User:** Gmail adresin (örn: `eslemlestrange@gmail.com`)
   - ✅ **SMTP Password:** App Password (16 karakterlik, boşluksuz)
   - ✅ **Sender Email:** Aynı Gmail adresin
   - ✅ **Sender Name:** `SmartAttendance`

3. **"Save"** butonuna tıkla (eğer değişiklik yaptıysan)

### 3. Olası Hatalar ve Çözümleri

#### ❌ "Supabase Auth API response status: 500"
**Sebep:** SMTP ayarları yanlış veya eksik
**Çözüm:**
1. SMTP ayarlarını kontrol et
2. App Password doğru mu? (boşluklar olmadan)
3. Gmail App Password kullanıyorsan, normal şifre çalışmaz

#### ❌ "Supabase Auth API response status: 200" ama email gelmiyor
**Sebep:** SMTP ayarları yapılandırılmamış (Supabase varsayılan email sistemi çalışmıyor)
**Çözüm:**
1. SMTP ayarlarını yapılandır
2. Test email gönder (Supabase Dashboard'dan)
3. Test email gelirse, edge function'ı yeniden deploy et

#### ❌ "Error sending recovery email"
**Sebep:** SMTP authentication başarısız
**Çözüm:**
1. App Password'i yeniden oluştur
2. Boşlukları kaldırarak kopyala
3. Supabase'e yapıştır
4. Save butonuna tıkla

---

## 🧪 Test Adımları

### Adım 1: Supabase Dashboard'dan Test Email Gönder

1. **Settings → Auth → SMTP Settings**
2. **"Send test email"** butonuna tıkla (eğer varsa)
3. Email adresini gir
4. **Send** butonuna tıkla
5. Email'inizi kontrol edin

✅ **Test email geldi mi?** → SMTP ayarları doğru!

### Adım 2: Edge Function Loglarını Kontrol Et

1. **Edge Functions → reset-password → Logs**
2. Son isteği bul
3. Hata mesajlarını kontrol et

### Adım 3: Edge Function'ı Yeniden Deploy Et

SMTP ayarlarını düzelttikten sonra:

1. **Edge Functions → reset-password → Deploy**

VEYA terminalden:
```bash
supabase functions deploy reset-password
```

---

## 📝 Sonraki Adımlar

1. ✅ Supabase Dashboard → Edge Functions → reset-password → Logs
2. ✅ SMTP ayarlarını kontrol et
3. ✅ Test email gönder (Supabase Dashboard'dan)
4. ✅ Edge function'ı yeniden deploy et
5. ✅ Android'de tekrar test et

**Supabase Dashboard'daki logları paylaş, birlikte çözelim!**

Özellikle şu satırları ara:
- `"Supabase Auth API response status: ..."`
- `"Supabase Auth API response body: ..."`







