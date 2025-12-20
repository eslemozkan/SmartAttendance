// Public reset-password page (HTML + CSS, JS is external to avoid CSP)
Deno.serve(async (req) => {
  // Basic CORS (not strictly needed for GET of HTML, but harmless)
  const headers = new Headers({
    "content-type": "text/html; charset=utf-8",
    "cache-control": "no-store",
    "access-control-allow-origin": "*",
    "access-control-allow-methods": "GET, OPTIONS",
    "access-control-allow-headers": "Content-Type, Authorization",
  });

  if (req.method === "OPTIONS") {
    return new Response(null, { headers });
  }

  const html = `<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Şifre Sıfırlama - SmartAttendance</title>
  <style>
    :root {
      color-scheme: light;
      font-family: "Inter", system-ui, -apple-system, "Segoe UI", sans-serif;
      background: #0f172a;
      color: #e2e8f0;
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: radial-gradient(circle at top, rgba(59,130,246,0.15), transparent 40%),
                  radial-gradient(circle at 20% 20%, rgba(16,185,129,0.12), transparent 30%),
                  #0f172a;
      padding: 24px;
    }
    .card {
      width: 100%;
      max-width: 420px;
      background: rgba(15,23,42,0.85);
      border: 1px solid rgba(226,232,240,0.08);
      border-radius: 16px;
      padding: 24px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.35);
      backdrop-filter: blur(8px);
    }
    h1 {
      margin: 0 0 8px;
      font-size: 22px;
      font-weight: 700;
      color: #e2e8f0;
    }
    p {
      margin: 0 0 16px;
      color: #94a3b8;
      font-size: 14px;
      line-height: 1.5;
    }
    label {
      display: block;
      margin-bottom: 6px;
      font-size: 13px;
      color: #cbd5e1;
    }
    input {
      width: 100%;
      padding: 12px;
      border-radius: 10px;
      border: 1px solid rgba(226,232,240,0.15);
      background: rgba(15,23,42,0.6);
      color: #e2e8f0;
      font-size: 15px;
      transition: border-color .2s, box-shadow .2s;
      outline: none;
    }
    input:focus {
      border-color: rgba(59,130,246,0.7);
      box-shadow: 0 0 0 3px rgba(59,130,246,0.15);
    }
    .error {
      color: #f87171;
      font-size: 13px;
      margin-top: 6px;
      display: none;
    }
    .error.show { display: block; }
    .btn {
      width: 100%;
      padding: 13px;
      margin-top: 16px;
      border: none;
      border-radius: 10px;
      font-weight: 700;
      font-size: 15px;
      color: #0b1020;
      background: linear-gradient(120deg, #60a5fa, #34d399);
      cursor: pointer;
      transition: transform .1s ease, box-shadow .2s;
    }
    .btn:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none;
      box-shadow: none;
    }
    .btn:not(:disabled):active { transform: translateY(1px); }
    .message {
      margin-top: 16px;
      padding: 12px;
      border-radius: 10px;
      font-size: 14px;
      display: none;
    }
    .message.success {
      display: block;
      background: rgba(34,197,94,0.12);
      color: #4ade80;
      border: 1px solid rgba(74,222,128,0.3);
    }
    .message.error {
      display: block;
      background: rgba(248,113,113,0.12);
      color: #fca5a5;
      border: 1px solid rgba(248,113,113,0.25);
    }
    .loading {
      margin-top: 10px;
      display: none;
      color: #94a3b8;
      font-size: 13px;
    }
    .loading.show { display: inline-flex; gap: 8px; align-items: center; }
    .dot {
      width: 7px; height: 7px;
      border-radius: 50%;
      background: #60a5fa;
      animation: pulse 1.2s infinite ease-in-out;
    }
    .dot:nth-child(2) { animation-delay: 0.15s; }
    .dot:nth-child(3) { animation-delay: 0.3s; }
    @keyframes pulse {
      0%, 80%, 100% { opacity: .25; transform: scale(0.9); }
      40% { opacity: 1; transform: scale(1.08); }
    }
  </style>
</head>
<body>
  <div class="card">
    <h1>Şifreyi Sıfırla</h1>
    <p>E-posta ile gelen bağlantı doğrulandıysa yeni şifreni belirle.</p>

    <form id="resetForm">
      <label for="newPassword">Yeni Şifre</label>
      <input id="newPassword" name="newPassword" type="password" placeholder="Yeni şifreniz" />
      <div id="newPasswordError" class="error"></div>

      <label for="confirmPassword" style="margin-top:12px;">Şifreyi Doğrula</label>
      <input id="confirmPassword" name="confirmPassword" type="password" placeholder="Tekrar şifre" />
      <div id="confirmPasswordError" class="error"></div>

      <button id="submitBtn" class="btn" type="submit">Şifreyi Güncelle</button>
    </form>

    <div id="message" class="message"></div>
    <div id="loading" class="loading">
      <span class="dot"></span><span class="dot"></span><span class="dot"></span>
      <span>İşleniyor...</span>
    </div>
  </div>

  <!-- JS ayrı endpoint'ten gelir; inline script kullanmıyoruz -->
  <script src="https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/reset-password-page-script" defer></script>
</body>
</html>`;

  return new Response(html, { headers });
});
