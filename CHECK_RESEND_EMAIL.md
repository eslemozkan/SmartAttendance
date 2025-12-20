# 🔍 Resend Email Sorun Giderme

## ❌ Sorun: Email Gönderilmiyor

Loglardan görüldüğü üzere:
- ✅ Edge function çalışıyor (Response Code: 200)
- ✅ Reset link oluşturuldu
- ❌ `emailSent: false` - Email gönderilmedi

## 🔍 Kontrol Listesi

### 1. Supabase Dashboard'da Logları Kontrol Et

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions

2. **Edge Functions → reset-password → Logs** bölümüne git

3. **Son isteği bul** ve şunları ara:
   - `"Resend API key check: ..."`
   - `"Resend API response status: ..."`
   - `"Resend API error: ..."`

### 2. Resend API Key Kontrolü

**Supabase Dashboard → Edge Functions → Secrets:**
- `RESEND_API_KEY` var mı?
- Değeri doğru mu? (Resend Dashboard'dan kontrol et)

**Resend Dashboard'da kontrol et:**
- https://resend.com/api-keys
- API key aktif mi?
- API key'in "Sending access" yetkisi var mı?

### 3. Resend Dashboard'da Email Durumu

1. **Resend Dashboard'a git:**
   - https://resend.com/emails

2. **Son gönderilen email'leri kontrol et:**
   - Email gönderildi mi?
   - Status nedir? (Delivered, Bounced, Failed, etc.)
   - Hata mesajı var mı?

### 4. Olası Hatalar ve Çözümleri

#### ❌ "Unauthorized" veya "Invalid API key"
**Sebep:** Resend API key yanlış veya eksik
**Çözüm:**
1. Resend Dashboard'dan yeni API key oluştur
2. Supabase Dashboard → Edge Functions → Secrets
3. `RESEND_API_KEY` secret'ını güncelle
4. Edge function'ı yeniden deploy et

#### ❌ "Domain not verified"
**Sebep:** `onboarding@resend.dev` kullanıyorsunuz ama domain doğrulanmamış
**Çözüm:**
- `onboarding@resend.dev` test için kullanılabilir
- Eğer çalışmıyorsa, Resend Dashboard → Domains'den domain doğrula

#### ❌ "Rate limit exceeded"
**Sebep:** Resend ücretsiz planında günlük limit aşıldı
**Çözüm:**
- Resend Dashboard'da limit kontrolü yap
- Ertesi gün tekrar dene

#### ❌ Email spam'a düşüyor
**Sebep:** Domain doğrulanmamış
**Çözüm:**
- Resend Dashboard → Domains'den domain doğrula
- SPF, DKIM, DMARC kayıtlarını ekle

---

## 🧪 Test Adımları

### 1. Edge Function Loglarını Kontrol Et

Supabase Dashboard → Edge Functions → reset-password → Logs

Şunları ara:
```
Resend API key check: Found (length: ...)
Resend API response status: 200
Email sent successfully via Resend
```

VEYA hata varsa:
```
Resend API error - Status: 401
Resend API error - Body: {"message":"Invalid API key"}
```

### 2. Resend Dashboard'da Test Et

1. Resend Dashboard → Emails
2. Son gönderilen email'leri kontrol et
3. Email durumunu kontrol et

### 3. Edge Function'ı Yeniden Deploy Et

Logları güncelledik, yeniden deploy et:

```bash
supabase functions deploy reset-password
```

VEYA Supabase Dashboard'dan:
- Edge Functions → reset-password → Deploy

---

## 📝 Sonraki Adımlar

1. ✅ Supabase Dashboard → Edge Functions → reset-password → Logs
2. ✅ Resend Dashboard → Emails
3. ✅ Edge function'ı yeniden deploy et
4. ✅ Tekrar test et

Logları paylaş, birlikte çözelim!




