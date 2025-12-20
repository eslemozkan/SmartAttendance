# 🌐 Web Şifre Sıfırlama Sayfası Kurulumu

## ✅ Yapılan Değişiklikler

Artık şifre sıfırlama için **basit bir web sayfası** kullanılıyor:
- ✅ Android uygulaması yerine web sayfası açılıyor
- ✅ Basit ve güvenilir
- ✅ Supabase API ile direkt çalışıyor
- ✅ Database'e direkt bağlanıyor

---

## 📋 Kurulum Adımları

### 1. Edge Function'ı Deploy Et

```bash
cd supabase/functions/reset-password-page
supabase functions deploy reset-password-page
```

**VEYA Supabase Dashboard'dan:**

1. **Supabase Dashboard → Edge Functions → Create Function**
2. **Function name:** `reset-password-page`
3. **Code:** `supabase/functions/reset-password-page/index.ts` dosyasının içeriğini kopyala-yapıştır
4. **Deploy** butonuna tıkla

---

### 2. Supabase Dashboard Ayarları

#### Redirect URL Ayarla

1. **Supabase Dashboard → Settings → Authentication → URL Configuration**
2. **Redirect URLs** bölümüne şunu ekle:
   ```
   https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
   ```
3. **Site URL** kontrol et:
   - `https://oubvhffqbsxsnbtinzbl.supabase.co` olmalı (veya boş bırakılabilir)
4. **Save** butonuna tıkla

---

## 🧪 Test Et

### 1. Android Uygulamasında

1. Uygulamayı aç
2. Login ekranına git
3. "Şifremi Unuttum" linkine tıkla
4. Email adresini gir
5. "Gönder" butonuna tıkla

### 2. Email Kontrol Et

1. Email'inizi kontrol et (spam klasörünü de)
2. Email'deki "Reset Password" linkine tıklayın
3. **Web sayfası açılmalı** (Android uygulaması değil)
4. Yeni şifre gir
5. "Şifreyi Güncelle" butonuna tıkla
6. Başarı mesajı görünmeli

---

## 🔧 Nasıl Çalışıyor?

### 1. Forgot Password (LoginActivity)

```kotlin
ApiService.resetPassword(email)
```

- Supabase'in `/auth/v1/recover` endpoint'ine istek gönderilir
- `redirect_to` parametresi: `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`
- Supabase otomatik olarak email gönderir

### 2. Email Link

Email'deki link formatı:
```
https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/verify?token=xxx&type=recovery&redirect_to=https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page
```

### 3. Web Sayfası (reset-password-page)

1. **Token'ı URL'den alır:** `?token=xxx&type=recovery`
2. **Token'ı verify eder:** Supabase `/auth/v1/verify` endpoint'ine istek
3. **Session oluşturur:** Verify response'dan access token alır
4. **Şifreyi günceller:** `/auth/v1/user` endpoint'ine PUT request
5. **Başarı mesajı gösterir**

---

## 📱 Web Sayfası Özellikleri

- ✅ **Responsive tasarım:** Mobil ve desktop uyumlu
- ✅ **Form validasyonu:** Şifre uzunluğu ve eşleşme kontrolü
- ✅ **Hata yönetimi:** Kullanıcı dostu hata mesajları
- ✅ **Loading göstergesi:** İşlem sırasında spinner
- ✅ **Başarı mesajı:** Şifre güncellendiğinde bilgilendirme

---

## 🐛 Sorun Giderme

### Web sayfası açılmıyor

1. **Edge Function deploy edildi mi?** Kontrol et:
   ```bash
   supabase functions list
   ```

2. **Redirect URL doğru mu?** Supabase Dashboard'da kontrol et

3. **Email'deki link doğru mu?** Link'te `redirect_to` parametresi var mı?

### Şifre güncellenemiyor

1. **Token süresi dolmuş olabilir** (genelde 1 saat)
2. **Yeni bir reset password isteği gönder**
3. **Email'deki en son link'i kullan**

### Edge Function hatası

1. **Supabase Dashboard → Edge Functions → Logs** kontrol et
2. **Function'ın deploy edildiğinden emin ol**
3. **Code'u kontrol et:** `supabase/functions/reset-password-page/index.ts`

---

## ✅ Tamamlandı!

Artık şifre sıfırlama için basit bir web sayfası kullanılıyor. Android uygulaması yerine web sayfası açılıyor ve şifre güncelleme işlemi web üzerinden yapılıyor.

---

## 📝 Notlar

- **Web sayfası Supabase Edge Function olarak host ediliyor**
- **Android uygulamasından bağımsız çalışıyor**
- **Gmail link tracking sorunu yok** (web sayfası olduğu için)
- **Basit ve güvenilir**



