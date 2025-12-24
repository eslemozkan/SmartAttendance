# 🔧 Function Deploy Kontrolü

## ❌ Sorun

Content-Type hala `text/plain` dönüyor. Function doğru deploy edilmemiş.

---

## ✅ Adım Adım Kontrol

### 1. Supabase Dashboard'da Function'ı Aç

1. **Supabase Dashboard → Edge Functions → reset-password-page**
2. **Code** sekmesine git

### 2. Kodun Başlangıcını Kontrol Et

**Editördeki kodun İLK SATIRI şu olmalı:**

```typescript
Deno.serve(async (req) => {
```

**Eğer farklıysa (örneğin `// Serve the password reset HTML page` gibi bir şey görüyorsan):**
- ❌ Kod yanlış yapıştırılmış
- ✅ Tüm kodu sil ve doğru kodu yapıştır

### 3. Content-Type Header'ını Kontrol Et

**Editörde şu satırı ara (Ctrl+F):**

```
Content-Type
```

**Bulduğun satır şu olmalı:**

```typescript
'Content-Type': 'text/html; charset=utf-8',
```

**Eğer `text/plain` görüyorsan:**
- ❌ Kod yanlış
- ✅ Tüm kodu sil ve doğru kodu yapıştır

### 4. Deploy Butonunu Kontrol Et

1. **Sayfanın sağ alt köşesinde "Deploy updates" butonu var mı?**
2. **Buton yeşil mi? (deploy edilmemiş değişiklikler varsa yeşil olur)**
3. **Eğer yeşilse, tıkla ve deploy'un tamamlanmasını bekle**

### 5. Deploy Sonrası Kontrol

Deploy ettikten sonra PowerShell'de şu komutu çalıştır:

```powershell
$r = Invoke-WebRequest -Uri "https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page" -Method GET
Write-Host "Content-Type: $($r.Headers['Content-Type'])"
```

**Beklenen:** `text/html; charset=utf-8`

---

## 🔍 Olası Sorunlar

### Sorun 1: Kod Yanlış Yapıştırılmış

**Çözüm:**
- Tüm kodu sil
- Doğru kodu yapıştır (RESET_PASSWORD_PAGE_FIX.md dosyasındaki kod)
- Deploy et

### Sorun 2: Deploy Butonuna Tıklanmamış

**Çözüm:**
- "Deploy updates" butonuna tıkla
- Deploy'un tamamlanmasını bekle (birkaç saniye)

### Sorun 3: Function Cache Sorunu

**Çözüm:**
- Birkaç dakika bekle
- Tekrar test et
- Hard refresh yap (Ctrl+F5)

---

## ✅ Doğru Kod (Kopyala-Yapıştır)

```typescript
Deno.serve(async (req) => {
  const headers = new Headers({
    'Content-Type': 'text/html; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization',
  });

  if (req.method === 'OPTIONS') {
    return new Response(null, { headers });
  }

  const html = `<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Şifre Sıfırlama - SmartAttendance</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .container {
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
            padding: 40px;
            max-width: 400px;
            width: 100%;
        }

        .logo {
            text-align: center;
            margin-bottom: 30px;
        }

        .logo h1 {
            color: #1976D2;
            font-size: 28px;
            margin-bottom: 10px;
        }

        .logo p {
            color: #666;
            font-size: 14px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
            font-size: 14px;
        }

        .form-group input {
            width: 100%;
            padding: 12px;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 16px;
            transition: border-color 0.3s;
        }

        .form-group input:focus {
            outline: none;
            border-color: #1976D2;
        }

        .form-group .error {
            color: #F44336;
            font-size: 12px;
            margin-top: 5px;
            display: none;
        }

        .form-group .error.show {
            display: block;
        }

        .btn {
            width: 100%;
            padding: 14px;
            background: #1976D2;
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: background 0.3s;
            margin-top: 10px;
        }

        .btn:hover {
            background: #1565C0;
        }

        .btn:disabled {
            background: #ccc;
            cursor: not-allowed;
        }

        .message {
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: none;
        }

        .message.show {
            display: block;
        }

        .message.success {
            background: #E8F5E9;
            color: #2E7D32;
            border: 1px solid #4CAF50;
        }

        .message.error {
            background: #FFEBEE;
            color: #C62828;
            border: 1px solid #F44336;
        }

        .loading {
            display: none;
            text-align: center;
            margin-top: 20px;
        }

        .loading.show {
            display: block;
        }

        .spinner {
            border: 3px solid #f3f3f3;
            border-top: 3px solid #1976D2;
            border-radius: 50%;
            width: 30px;
            height: 30px;
            animation: spin 1s linear infinite;
            margin: 0 auto;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        .back-link {
            text-align: center;
            margin-top: 20px;
        }

        .back-link a {
            color: #1976D2;
            text-decoration: none;
            font-size: 14px;
        }

        .back-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">
            <h1>🔐 Şifre Sıfırlama</h1>
            <p>Yeni şifrenizi belirleyin</p>
        </div>

        <div id="message" class="message"></div>

        <form id="resetForm">
            <div class="form-group">
                <label for="newPassword">Yeni Şifre</label>
                <input 
                    type="password" 
                    id="newPassword" 
                    name="newPassword" 
                    placeholder="En az 6 karakter"
                    required
                    minlength="6"
                >
                <div class="error" id="newPasswordError"></div>
            </div>

            <div class="form-group">
                <label for="confirmPassword">Şifre Tekrar</label>
                <input 
                    type="password" 
                    id="confirmPassword" 
                    name="confirmPassword" 
                    placeholder="Şifrenizi tekrar girin"
                    required
                    minlength="6"
                >
                <div class="error" id="confirmPasswordError"></div>
            </div>

            <button type="submit" class="btn" id="submitBtn">Şifreyi Güncelle</button>
        </form>

        <div class="loading" id="loading">
            <div class="spinner"></div>
            <p style="margin-top: 10px; color: #666;">Güncelleniyor...</p>
        </div>

        <div class="back-link">
            <a href="#" onclick="window.close(); return false;">Kapat</a>
        </div>
    </div>

    <script src="https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page-script"></script>
</body>
</html>`;

  return new Response(html, { headers });
});
```

---

## 📋 Kontrol Listesi

- [ ] **Function açıldı** (Supabase Dashboard → Edge Functions → reset-password-page → Code)
- [ ] **İlk satır:** `Deno.serve(async (req) => {`
- [ ] **Content-Type:** `'Content-Type': 'text/html; charset=utf-8'`
- [ ] **Tüm kod silindi ve doğru kod yapıştırıldı**
- [ ] **Deploy updates butonuna tıklandı**
- [ ] **Deploy tamamlandı** (birkaç saniye beklendi)
- [ ] **PowerShell'de test edildi:** Content-Type `text/html; charset=utf-8` dönüyor






