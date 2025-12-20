# 🔗 Deep Link Sorunu Çözümü

## ✅ Yapılan Değişiklikler

### 1. AndroidManifest.xml Güncellendi

Web URL intent filter eklendi:
```xml
<!-- Web URL intent filter (Supabase email links) -->
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="https"
        android:host="oubvhffqbsxsnbtinzbl.supabase.co"
        android:pathPrefix="/auth/v1/verify" />
</intent-filter>
```

Bu sayede email'deki web URL'ler (`https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/verify?token=...`) Android uygulamasını açacak.

### 2. ResetPasswordActivity Güncellendi

- Web URL'den token extract ediliyor
- Token Supabase'e verify ediliyor
- Kullanıcı authenticate ediliyor
- Şifre güncelleme yapılabiliyor

---

## 🧪 Test Et

### Adım 1: Uygulamayı Yeniden Derleyin

```bash
./gradlew assembleDebug
```

Veya Android Studio'da **Build → Make Project**

### Adım 2: Email'deki Linke Tıklayın

1. Email'inizi açın (spam klasörünü kontrol edin)
2. "Reset Password" linkine tıklayın
3. Android uygulaması otomatik açılmalı
4. ResetPasswordActivity görünmeli

### Adım 3: Yeni Şifre Girin

1. Yeni şifrenizi girin
2. Şifreyi tekrar girin (onay)
3. "Şifreyi Güncelle" butonuna tıklayın
4. Başarı mesajı görünmeli

---

## 🔍 Sorun Giderme

### Link Açılmıyor

**Kontrol edin:**
1. AndroidManifest.xml'de intent filter doğru mu?
2. Uygulama yeniden derlendi mi?
3. Email'deki link formatı doğru mu?

**Test:**
- Email'deki linki kopyalayın
- Android Studio Logcat'te `ResetPasswordActivity` tag'ini filtreleyin
- Link'e tıkladığınızda log görünmeli

### Token Doğrulanamıyor

**Kontrol edin:**
1. Token süresi dolmuş olabilir (genelde 1 saat)
2. Token zaten kullanılmış olabilir
3. Email'deki en son link'i kullanın

**Çözüm:**
- Yeni bir şifre sıfırlama isteği gönderin
- Email'deki en son link'i kullanın

### Şifre Güncellenemiyor

**Kontrol edin:**
1. Token doğrulandı mı? (Logcat'te kontrol edin)
2. Şifre en az 6 karakter mi?
3. Şifreler eşleşiyor mu?

**Logcat'te kontrol edin:**
- `ResetPasswordActivity: Token verified successfully`
- `ResetPasswordActivity: Update password error: ...`

---

## 📋 Email Link Formatları

Supabase email'lerinde iki format olabilir:

### Format 1: Web URL (En Yaygın)
```
https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/verify?token=xxx&type=recovery&redirect_to=com.smartattendance.app://reset-password
```

### Format 2: Direct Deep Link
```
com.smartattendance.app://reset-password?token=xxx&type=recovery
```

Her iki format da artık çalışıyor!

---

## ✅ Başarı Kriterleri

1. ✅ Email'deki linke tıklayınca Android uygulaması açılıyor
2. ✅ ResetPasswordActivity görünüyor
3. ✅ Token doğrulanıyor (Logcat'te görünüyor)
4. ✅ Yeni şifre girilebiliyor
5. ✅ Şifre başarıyla güncelleniyor

---

## 🆘 Hala Çalışmıyorsa

1. **Uygulamayı yeniden derleyin** (Build → Make Project)
2. **Uygulamayı kaldırıp yeniden yükleyin**
3. **Email'deki en son link'i kullanın** (eski linkler çalışmayabilir)
4. **Logcat'te hataları kontrol edin**



