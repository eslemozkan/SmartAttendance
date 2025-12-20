# 🚀 Şifre Sıfırlama Web Sayfası Deploy Etme

## ❌ Sorun

Email'deki link'e tıklayınca `about:blank` sayfası açılıyor. Bu, Edge Function'ın deploy edilmediği veya yanlış yapılandırıldığı anlamına geliyor.

---

## ✅ Çözüm: Edge Function'ı Deploy Et

### Yöntem 1: Supabase Dashboard'dan (En Kolay)

1. **Supabase Dashboard'a gidin:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl

2. **Edge Functions bölümüne gidin:**
   - Sol menüden **Edge Functions** seçin
   - Veya: **Project Settings → Edge Functions**

3. **Yeni Function oluşturun:**
   - **Create Function** butonuna tıklayın
   - **Function name:** `reset-password-page`
   - **Code:** `supabase/functions/reset-password-page/index.ts` dosyasının içeriğini kopyala-yapıştır
   - **Deploy** butonuna tıklayın

4. **Function URL'ini kontrol edin:**
   - Function deploy edildikten sonra URL şöyle olmalı:
   - `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`

---

### Yöntem 2: Supabase CLI ile

```bash
# Supabase CLI yüklü olmalı
npm install -g supabase

# Supabase'e login olun
supabase login

# Project'i link edin
supabase link --project-ref oubvhffqbsxsnbtinzbl

# Function'ı deploy edin
cd supabase/functions/reset-password-page
supabase functions deploy reset-password-page
```

---

## 🔧 Supabase Dashboard Ayarları

### Redirect URL Ayarla

1. **Settings → Authentication → URL Configuration**
2. **Redirect URLs** bölümüne şunu ekleyin:
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
3. **Save** butonuna tıklayın

---

## 🧪 Test Et

### 1. Function'ı Manuel Test Edin

Tarayıcıda şu URL'i açın:
```
https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
```

**Beklenen:** Şifre sıfırlama formu görünmeli

**Eğer hata görürseniz:**
- Function deploy edilmemiş olabilir
- Function adı yanlış olabilir
- Code'da syntax hatası olabilir

### 2. Token ile Test Edin

1. Android uygulamasından "Şifremi Unuttum" tıklayın
2. Email'inizi kontrol edin
3. Email'deki link'e tıklayın
4. **Web sayfası açılmalı** (about:blank değil)

---

## 🐛 Sorun Giderme

### about:blank görünüyor

**Neden:**
- Edge Function deploy edilmemiş
- Redirect URL yanlış yapılandırılmış
- Function adı yanlış

**Çözüm:**
1. Edge Function'ın deploy edildiğinden emin olun
2. Supabase Dashboard → Edge Functions → Logs kontrol edin
3. Redirect URL'i kontrol edin

### Function bulunamıyor (404)

**Neden:**
- Function adı yanlış
- Function deploy edilmemiş

**Çözüm:**
1. Supabase Dashboard → Edge Functions → Function listesini kontrol edin
2. Function adı: `reset-password-page` olmalı
3. Function'ı yeniden deploy edin

### Function hatası (500)

**Neden:**
- Code'da syntax hatası
- Supabase API key yanlış

**Çözüm:**
1. Supabase Dashboard → Edge Functions → Logs kontrol edin
2. Hata mesajını okuyun
3. Code'u düzeltin ve yeniden deploy edin

---

## ✅ Başarı Kriterleri

1. ✅ **Function deploy edildi**
2. ✅ **Manuel test başarılı** (URL'yi açınca form görünüyor)
3. ✅ **Email link'i çalışıyor** (about:blank değil, form görünüyor)
4. ✅ **Şifre güncelleme çalışıyor**

---

## 📝 Notlar

- Edge Function deploy edilene kadar web sayfası çalışmaz
- Function URL'i: `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`
- Redirect URL Supabase Dashboard'da ayarlanmalı



