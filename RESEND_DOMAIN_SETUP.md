# 🔧 Resend Domain Doğrulama Rehberi

## ❌ Sorun

Resend API 403 hatası veriyor:
```
"You can only send testing emails to your own email address (eslemlestrange@gmail.com)"
```

**Sebep:** Test API key kullanıyorsunuz. Test key'ler sadece hesap sahibinin email adresine gönderebilir.

## ✅ Çözüm: Domain Doğrulama

### Adım 1: Resend Dashboard'a Git

1. https://resend.com/domains adresine git
2. Resend hesabınızla giriş yap

### Adım 2: Domain Ekle

1. **"Add Domain"** butonuna tıkla
2. Domain'inizi girin (örn: `firat.edu.tr` veya `smartattendance.com`)
3. **"Add"** butonuna tıkla

### Adım 3: DNS Kayıtlarını Ekle

Resend size DNS kayıtlarını verecek. Bunları domain sağlayıcınızda (GoDaddy, Namecheap, vs.) eklemeniz gerekiyor:

**Örnek DNS kayıtları:**
```
Type: TXT
Name: @
Value: resend._domainkey.yourdomain.com

Type: CNAME
Name: resend
Value: resend.domains.resend.com
```

**Not:** DNS kayıtlarının yayılması 24-48 saat sürebilir.

### Adım 4: Domain Doğrulamasını Bekle

1. Resend Dashboard → Domains
2. Domain'inizin durumunu kontrol et
3. **"Verified"** olana kadar bekle

### Adım 5: Edge Function'da Domain'i Kullan

Domain doğrulandıktan sonra, edge function'da gönderen adresini güncelle:

```typescript
from: "SmartAttendance <noreply@yourdomain.com>",
```

---

## 🔄 Alternatif Çözüm: Supabase Built-in Email

Eğer domain doğrulaması yapmak istemiyorsanız, Supabase'in built-in email sistemini kullanabilirsiniz:

### Adım 1: Supabase SMTP Ayarları

1. Supabase Dashboard → Settings → Auth → SMTP Settings
2. **Enable Custom SMTP** açık olmalı
3. SMTP ayarlarını yapılandır (Gmail, SendGrid, vs.)

### Adım 2: Edge Function'ı Güncelle

Edge function zaten Supabase'in built-in email sistemini fallback olarak kullanıyor. Resend başarısız olursa otomatik olarak Supabase'e geçer.

---

## 🧪 Test

Domain doğrulandıktan sonra:

1. Edge function'ı yeniden deploy et
2. Android uygulamasında "Şifremi Unuttum" özelliğini test et
3. Email'inizi kontrol edin

---

## 📝 Notlar

- **Test API key:** Sadece hesap sahibinin email adresine gönderebilir
- **Production API key:** Domain doğrulaması gerektirir
- **Domain doğrulaması:** 24-48 saat sürebilir
- **Ücretsiz plan:** Resend ücretsiz planında domain doğrulaması yapabilirsiniz

---

## 🎯 Hızlı Test (Geçici)

Eğer hemen test etmek istiyorsanız, geçici olarak test email adresine gönderebilirsiniz:

```typescript
// Geçici test için
to: ["eslemlestrange@gmail.com"], // Test email adresi
```

Ama bu production için uygun değil!




