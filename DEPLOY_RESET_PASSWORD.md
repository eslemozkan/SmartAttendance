# Reset Password Edge Function Deploy Rehberi

## Yöntem 1: Supabase CLI ile Deploy (Önerilen)

### Adım 1: Supabase CLI Kurulumu (Eğer yoksa)

```bash
# Windows (PowerShell)
irm https://github.com/supabase/cli/releases/latest/download/supabase_windows_amd64.zip -OutFile supabase.zip
Expand-Archive supabase.zip -DestinationPath .
.\supabase.exe --version

# Veya npm ile
npm install -g supabase
```

### Adım 2: Supabase'e Login Olun

```bash
supabase login
```

Browser açılacak, Supabase hesabınızla giriş yapın.

### Adım 3: Projeyi Linkleyin

```bash
cd C:\Users\elife\Documents\GitHub\SmartAttendance
supabase link --project-ref oubvhffqbsxsnbtinzbl
```

### Adım 4: Service Role Key'i Ayarlayın

```bash
# Service Role Key'i environment variable olarak ayarlayın
# Supabase Dashboard > Settings > API > Service Role Key'i kopyalayın

# Windows PowerShell
$env:SUPABASE_SERVICE_ROLE_KEY="your-service-role-key-here"

# Veya Supabase secrets'a ekleyin (önerilen)
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=your-service-role-key-here
```

### Adım 5: Edge Function'ı Deploy Edin

```bash
supabase functions deploy reset-password
```

### Adım 6: Test Edin

Deploy başarılı olduğunda şu mesajı göreceksiniz:
```
Deployed Function reset-password (https://oubvhffqbsxsnbtinzbl.functions.supabase.co/reset-password)
```

---

## Yöntem 2: Supabase Dashboard'dan Manuel Deploy

### Adım 1: Dashboard'a Gidin

1. https://supabase.com/dashboard adresine gidin
2. Projenizi seçin: `SmartAttendance`

### Adım 2: Edge Functions Bölümüne Gidin

1. Sol menüden **Edge Functions**'a tıklayın
2. **Create a new function** butonuna tıklayın

### Adım 3: Function Oluşturun

1. **Function name:** `reset-password`
2. **Template:** Boş bırakın veya "Hello World" seçin
3. **Create function** butonuna tıklayın

### Adım 4: Kodu Yapıştırın

1. Oluşturulan function'ın **Code** sekmesine gidin
2. Mevcut kodu silin
3. `supabase/functions/reset-password/index.ts` dosyasındaki tüm kodu kopyalayıp yapıştırın
4. **Deploy** butonuna tıklayın

### Adım 5: Service Role Key'i Secret Olarak Ekleyin

1. Function'ın **Settings** sekmesine gidin
2. **Secrets** bölümüne gidin
3. **Add secret** butonuna tıklayın
4. **Key:** `SUPABASE_SERVICE_ROLE_KEY`
5. **Value:** Supabase Dashboard > Settings > API > Service Role Key (kopyalayın)
6. **Save** butonuna tıklayın

### Adım 6: Test Edin

1. Function'ın **Logs** sekmesine gidin
2. Android uygulamasında "Şifremi Unuttum" özelliğini test edin
3. Logları kontrol edin

---

## Hızlı Komutlar (PowerShell)

```powershell
# Proje dizinine git
cd C:\Users\elife\Documents\GitHub\SmartAttendance

# Supabase'e login (ilk kez)
supabase login

# Projeyi linkle
supabase link --project-ref oubvhffqbsxsnbtinzbl

# Service Role Key'i secret olarak ekle (Dashboard > Settings > API'den kopyalayın)
supabase secrets set SUPABASE_SERVICE_ROLE_KEY="your-service-role-key-here"

# Edge function'ı deploy et
supabase functions deploy reset-password

# Logları izle
supabase functions logs reset-password
```

---

## Sorun Giderme

### "Function not found" hatası alıyorsanız:

1. Function adının doğru olduğundan emin olun: `reset-password`
2. `supabase/functions/reset-password/index.ts` dosyasının var olduğundan emin olun

### "Missing Supabase service role key" hatası alıyorsanız:

1. Service Role Key'i secret olarak eklediğinizden emin olun:
   ```bash
   supabase secrets set SUPABASE_SERVICE_ROLE_KEY="your-key-here"
   ```

2. Veya Dashboard'dan: Edge Functions > reset-password > Settings > Secrets

### "Permission denied" hatası alıyorsanız:

1. Supabase CLI'ye login olduğunuzdan emin: `supabase login`
2. Projeyi doğru linklediğinizden emin: `supabase link --project-ref oubvhffqbsxsnbtinzbl`

---

## Test

Deploy başarılı olduktan sonra:

1. Android uygulamasında "Şifremi Unuttum" özelliğini kullanın
2. Email adresinizi girin
3. Logları kontrol edin: `supabase functions logs reset-password`
4. Veya Dashboard'dan: Edge Functions > reset-password > Logs


