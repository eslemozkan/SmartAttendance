# 🔧 Redirect URL Düzeltmesi

## ❌ Sorun

`reset-password` Edge Function'ında `redirect_to` parametresi yanlış ayarlanmış:
- **Yanlış:** `redirect_to: ${supabaseUrl}/auth/v1/verify?token=#token_hash&type=recovery`
- **Doğru:** `redirect_to: https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`

Bu yüzden email'deki link yanlış yere yönlendiriyor ve `about:blank` açılıyor.

---

## ✅ Çözüm

`supabase/functions/reset-password/index.ts` dosyasındaki `redirect_to` parametresi düzeltildi.

---

## 🚀 Deploy Et

### Supabase Dashboard'dan:

1. **Edge Functions → reset-password** function'ını açın
2. **Code'u güncelleyin** (107. satırdaki `redirect_to` değerini değiştirin)
3. **Deploy** butonuna tıklayın

### Veya Supabase CLI ile:

```bash
cd supabase/functions/reset-password
supabase functions deploy reset-password
```

---

## 🧪 Test Et

1. **Yeni bir şifre sıfırlama isteği gönderin** (Android uygulamasından)
2. **Email'inizi kontrol edin**
3. **Email'deki link'e tıklayın**
4. **Web sayfası açılmalı** (about:blank değil)

---

## ✅ Başarı Kriterleri

1. ✅ **Edge Function güncellendi**
2. ✅ **Yeni email isteği gönderildi**
3. ✅ **Email link'i web sayfasına yönlendiriyor**
4. ✅ **about:blank görünmüyor**



