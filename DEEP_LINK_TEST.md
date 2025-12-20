# 🔧 Deep Link Test ve Sorun Giderme

## ❌ Sorun

Yeni email linkine tıklayınca hala blank sayfa görünüyor. Android uygulaması açılmıyor.

**Olası nedenler:**
1. Android uygulaması yüklü değil
2. Deep link intent filter çalışmıyor
3. Email'deki link formatı yanlış
4. AndroidManifest'te intent filter eksik

---

## ✅ Çözüm Adımları

### Adım 1: Android Uygulamasını Kontrol Edin

1. **Android cihazınızda/emülatörde uygulamayı açın**
2. **Uygulama çalışıyor mu kontrol edin**
3. **Uygulama yüklü mü kontrol edin**

### Adım 2: Uygulamayı Yeniden Derleyin ve Yükleyin

1. **Android Studio'da:**
   - **Build → Clean Project**
   - **Build → Rebuild Project**
   - **Run → Run 'app'**

2. **Veya terminal'de:**
   ```bash
   cd android
   ./gradlew clean
   ./gradlew assembleDebug
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Adım 3: Deep Link'i Manuel Test Edin

**Android Studio'da veya terminal'de:**

```bash
adb shell am start -a android.intent.action.VIEW -d "com.smartattendance.app://reset-password?token=test&type=recovery"
```

**Bu komut Android uygulamasını açmalı!**

Eğer açılmıyorsa:
- AndroidManifest'te intent filter yanlış
- Uygulama yüklü değil
- Package name yanlış

### Adım 4: Email'deki Link Formatını Kontrol Edin

**Email'deki link şu formatta olmalı:**

```
https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/verify?token=xxx&type=recovery&redirect_to=com.smartattendance.app://reset-password
```

**VEYA:**

```
com.smartattendance.app://reset-password?token=xxx&type=recovery
```

**Kontrol edin:**
- Link'te `redirect_to=com.smartattendance.app://reset-password` var mı?
- Link'te `token=` ve `type=recovery` var mı?

---

## 🔍 Detaylı Kontrol

### 1. AndroidManifest.xml Kontrolü

**Kontrol edin:**
- `ResetPasswordActivity` tanımlı mı?
- Intent filter'lar doğru mu?
- `android:exported="true"` mi?

**Doğru format:**
```xml
<activity 
    android:name=".ResetPasswordActivity"
    android:exported="true">
    <!-- Deep link intent filter -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="com.smartattendance.app"
            android:host="reset-password" />
    </intent-filter>
    <!-- Web URL intent filter -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data
            android:scheme="https"
            android:host="oubvhffqbsxsnbtinzbl.supabase.co"
            android:pathPrefix="/auth/v1/verify" />
    </intent-filter>
</activity>
```

### 2. Package Name Kontrolü

**AndroidManifest.xml'de:**
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.smartattendance.app">
```

**SupabaseClient.kt'de:**
```kotlin
const val REDIRECT_URL = "com.smartattendance.app://reset-password"
```

**Her ikisi de aynı olmalı!**

### 3. Logcat Kontrolü

**Android Studio Logcat'te:**
- `ResetPasswordActivity` tag'ini filtreleyin
- Link'e tıkladığınızda log görünmeli:
  ```
  ResetPasswordActivity: Deep link received: ...
  ```

**Eğer log görünmüyorsa:**
- Deep link çalışmıyor
- Intent filter yanlış
- Uygulama yüklü değil

---

## 🧪 Test Senaryoları

### Senaryo 1: Manuel Deep Link Testi

```bash
adb shell am start -a android.intent.action.VIEW -d "com.smartattendance.app://reset-password?token=test&type=recovery"
```

**Beklenen:** Android uygulaması açılmalı

### Senaryo 2: Web URL Testi

```bash
adb shell am start -a android.intent.action.VIEW -d "https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/verify?token=test&type=recovery&redirect_to=com.smartattendance.app://reset-password"
```

**Beklenen:** Android uygulaması açılmalı

### Senaryo 3: Email Link Testi

1. Email'deki link'i kopyalayın
2. Terminal'de:
   ```bash
   adb shell am start -a android.intent.action.VIEW -d "LINK_BURAYA"
   ```

**Beklenen:** Android uygulaması açılmalı

---

## 🆘 Hala Çalışmıyorsa

### 1. Uygulamayı Kaldırıp Yeniden Yükleyin

```bash
adb uninstall com.smartattendance.app.debug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. AndroidManifest'i Kontrol Edin

- Intent filter'lar doğru mu?
- `android:exported="true"` mi?
- Package name doğru mu?

### 3. Supabase Dashboard'u Kontrol Edin

- **Site URL:** `com.smartattendance.app://`
- **Redirect URLs:** `com.smartattendance.app://reset-password`
- **Yeni email isteği gönderildi mi?**

### 4. Email Link Formatını Kontrol Edin

- Link'te `redirect_to=com.smartattendance.app://reset-password` var mı?
- Link'te `token=` ve `type=recovery` var mı?

---

## ✅ Başarı Kriterleri

1. ✅ **Manuel deep link testi çalışıyor** (`adb shell am start ...`)
2. ✅ **Email link'e tıklayınca Android uygulaması açılıyor**
3. ✅ **ResetPasswordActivity görünüyor**
4. ✅ **Logcat'te deep link logları görünüyor**



