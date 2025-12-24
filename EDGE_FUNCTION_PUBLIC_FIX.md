# 🔧 Edge Function Public Fix

## ❌ Sorun

Edge Function 401 "Missing authorization header" hatası veriyor. HTML sayfası döndüren bir function public olmalı.

---

## ✅ Çözüm

### 1. Supabase Dashboard'da Function'ı Public Yapın

1. **Supabase Dashboard → Edge Functions → reset-password-page**
2. **Settings** veya **Configuration** sekmesine gidin
3. **"Public"** veya **"Require Authorization"** seçeneğini **KAPALI** yapın
4. **Save** butonuna tıklayın

**VEYA**

### 2. Function'ı Anon Key ile Çalışacak Şekilde Ayarlayın

Function'ın başında authorization kontrolünü kaldırın (zaten yapıldı).

---

## 🧪 Test Et

1. **Function'ı yeniden deploy edin** (eğer değişiklik yaptıysanız)
2. **Tarayıcıda test edin:**
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
3. **HTML sayfası görünmeli** (401 hatası değil)

---

## 📝 Notlar

- Supabase Edge Functions varsayılan olarak authorization gerektirir
- Public HTML sayfaları için authorization kapatılmalı
- URL'deki `#access_token` hash'i JavaScript tarafından okunur, function'a gönderilmez

---

## ✅ Başarı Kriterleri

1. ✅ **Function public yapıldı**
2. ✅ **401 hatası yok**
3. ✅ **HTML sayfası görünüyor**
4. ✅ **Token URL hash'inden okunuyor**






