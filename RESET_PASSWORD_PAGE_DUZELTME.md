# 🔧 Reset Password Page Düzeltmesi

## ❌ Sorun

Sayfa HTML kodunu gösteriyor, render edilmiyor. Content-Type header'ı yanlış olabilir.

---

## ✅ Çözüm: Supabase Dashboard'da Function'ı Yeniden Deploy Et

### Adım 1: Supabase Dashboard'a Git

1. **Supabase Dashboard → Edge Functions → reset-password-page**

### Adım 2: Kodu Tamamen Sil ve Yeniden Yapıştır

1. **Editördeki tüm kodu sil** (Ctrl+A → Delete)
2. **Aşağıdaki kodu kopyala ve yapıştır:**

```typescript
// Serve the password reset HTML page
// This function is public and doesn't require authorization
Deno.serve(async (req) => {
  // CORS headers
  const headers = new Headers({
    'Content-Type': 'text/html; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization',
  });

  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    return new Response(null, { headers });
  }

  // This is a public endpoint - no authorization required
  // Supabase will handle authorization via URL hash (#access_token)

  // Return the HTML page (CSS inline, JS external because of CSP)
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

    <!-- JS ayrı bir endpoint'ten yüklenecek (CSP inline script engelliyor) -->
    <script src="https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page-script"></script>
</body>
</html>`;

  return new Response(html, { headers });
});
```

### Adım 3: Deploy Et

1. **Deploy** butonuna tıkla
2. **Deploy'un tamamlanmasını bekle**

### Adım 4: Tarayıcıda Test Et

1. **Tarayıcıda sayfayı aç:**
   - Email'deki linke tıkla VEYA
   - Direkt şu URL'i aç: `https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page`
2. **Hard refresh yap:** `Ctrl+F5` (veya `Ctrl+Shift+R`)
3. **Sayfanın normal görünmesi gerekiyor:**
   - Mavi arka plan
   - Ortada beyaz kart
   - Form alanları
   - "Şifreyi Güncelle" butonu

---

## 🔍 Sorun Giderme

### Hala kod görünüyorsa:

1. **Browser Console'u aç:** F12
2. **Network sekmesine git**
3. **Sayfayı yenile:** F5
4. **`reset-password-page` isteğini bul**
5. **Headers sekmesine git**
6. **Response Headers → Content-Type** kontrol et
   - `text/html; charset=utf-8` olmalı
   - Eğer `text/plain` görüyorsan, function deploy edilmemiş olabilir

### JavaScript çalışmıyorsa:

1. **Console sekmesine git**
2. **Hata mesajları var mı kontrol et**
3. **Network sekmesine git**
4. **`reset-password-page-script` isteğini bul**
5. **Status Code** kontrol et (200 olmalı)

---

## ✅ Başarı Kriterleri

1. ✅ **Function deploy edildi**
2. ✅ **Sayfa normal görünüyor** (kod değil, form)
3. ✅ **JavaScript yükleniyor** (Console'da hata yok)
4. ✅ **Form çalışıyor** (şifre güncelleme başarılı)



