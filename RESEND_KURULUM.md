# Resend ile Email Gönderimi - Kurulum Rehberi

## ✅ Kolay Yöntem: Edge Function ile Resend API

Resend SMTP desteği yok, ama API var. Edge function'da Resend API kullanacağız.

## Adım 1: Resend Hesabı Oluştur

1. https://resend.com adresine gidin
2. **Sign up** butonuna tıklayın
3. Ücretsiz hesap oluşturun (3000 email/ay ücretsiz)

## Adım 2: API Key Al

1. Resend Dashboard'a giriş yapın
2. Sol menüden **API Keys**'e tıklayın
3. **Create API Key** butonuna tıklayın
4. **Name:** `Supabase Password Reset` yazın
5. **Permission:** `Sending access` seçin
6. **Create** butonuna tıklayın
7. **API Key'i kopyalayın** (sadece bir kez gösterilir! Örnek: `re_1234567890abcdef`)

## Adım 3: Edge Function'a API Key Ekle

### Seçenek A: Supabase Dashboard'dan (Kolay)

1. Supabase Dashboard → **Edge Functions** → **reset-password**
2. **Settings** sekmesine gidin
3. **Secrets** bölümüne gidin
4. **Add secret** butonuna tıklayın
5. **Key:** `RESEND_API_KEY`
6. **Value:** Resend API Key'iniz (2. adımda kopyaladığınız)
7. **Save** butonuna tıklayın

### Seçenek B: Supabase CLI ile

```bash
supabase secrets set RESEND_API_KEY=re_1234567890abcdef
```

## Adım 4: Edge Function'ı Deploy Et

Edge function zaten güncellendi, sadece deploy etmeniz gerekiyor:

```bash
supabase functions deploy reset-password
```

**VEYA** Supabase Dashboard'dan:
1. Edge Functions → **reset-password**
2. **Deploy** butonuna tıklayın

## Adım 5: Test Et

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullanın
2. Email adresinizi girin
3. Email'inizi kontrol edin (spam klasörünü de!)

---

## ✅ Başarı Kontrolü

- ✅ Email geldi mi? → Resend çalışıyor!
- ❌ Email gelmedi mi? → API Key'i kontrol edin

---

## 🔧 Sorun Giderme

### Email gelmiyor

1. **API Key kontrolü:**
   - Supabase Dashboard → Edge Functions → reset-password → Settings → Secrets
   - `RESEND_API_KEY` var mı? Doğru mu?

2. **Edge function logları:**
   - Supabase Dashboard → Edge Functions → reset-password → Logs
   - "Resend API response" loglarını kontrol edin

3. **Resend Dashboard:**
   - Resend Dashboard → Emails
   - Email gönderim durumunu kontrol edin

### "Unauthorized" hatası
- **Sebep:** API Key yanlış veya eksik
- **Çözüm:** Resend Dashboard'dan yeni API Key oluşturun ve Supabase secrets'a ekleyin

### Email spam'a düşüyor
- **Sebep:** `onboarding@resend.dev` kullanıyorsunuz
- **Çözüm:** Kendi domain'inizi doğrulayın (Resend Dashboard → Domains)

---

## 📊 Resend Limitleri (Ücretsiz Plan)

- **Aylık limit:** 3000 email
- **Günlük limit:** 100 email
- **API calls:** Sınırsız

---

## 🎯 Avantajlar

✅ SMTP ayarlarından daha kolay
✅ API Key ile çalışır (şifre karmaşası yok)
✅ Ücretsiz 3000 email/ay
✅ Detaylı analytics
✅ Kolay kurulum

---

## 📝 Özet

1. Resend hesabı oluştur → API Key al
2. Supabase SMTP Settings'e gir:
   - Host: `smtp.resend.com`
   - Port: `587`
   - Username: `resend`
   - Password: `[API Key]`
   - Sender Email: `onboarding@resend.dev`
3. Test et → Email gelirse başarılı!

