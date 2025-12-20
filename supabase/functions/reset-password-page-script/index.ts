// JavaScript for the reset password page
// Served as an external script because CSP engelliyor (inline script yasak)

Deno.serve((_req) => {
  const js = `
// Supabase configuration
const SUPABASE_URL = 'https://oubvhffqbsxsnbtinzbl.supabase.co';
const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI';

let accessToken = null;
let token = null;
let type = null;

// Check URL hash first (Supabase redirect format)
const hash = window.location.hash.substring(1); // Remove #
if (hash) {
  const hashParams = new URLSearchParams(hash);

  // Check for errors first
  const error = hashParams.get('error');
  const errorCode = hashParams.get('error_code');
  const errorDescription = hashParams.get('error_description');

  if (error || errorCode) {
    let errorMessage = 'Şifre sıfırlama bağlantısı geçersiz veya süresi dolmuş.';
    if (errorCode === 'otp_expired') {
      errorMessage = 'Email linkinin süresi dolmuş. Lütfen yeni bir şifre sıfırlama isteği gönderin.';
    } else if (errorDescription) {
      errorMessage = decodeURIComponent(errorDescription.replace(/\\+/g, ' '));
    }
    showMessage(errorMessage, 'error');
    document.getElementById('resetForm').style.display = 'none';
  } else {
    accessToken = hashParams.get('access_token');
    token = hashParams.get('token');
    type = hashParams.get('type');
  }
}

// Fallback to query parameters
if (!token && !accessToken) {
  const urlParams = new URLSearchParams(window.location.search);
  token = urlParams.get('token');
  type = urlParams.get('type');
}

// Check if we have access token or recovery token
if (!accessToken && (!token || type !== 'recovery')) {
  showMessage('Geçersiz veya eksik şifre sıfırlama bağlantısı. Lütfen e-postanızdaki en son bağlantıyı kullanın.', 'error');
  document.getElementById('resetForm').style.display = 'none';
} else if (accessToken) {
  console.log('Access token found in URL hash, user is authenticated');
}

// Form submission
document.getElementById('resetForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const newPassword = (document.getElementById('newPassword') as HTMLInputElement).value;
  const confirmPassword = (document.getElementById('confirmPassword') as HTMLInputElement).value;

  clearErrors();

  if (newPassword.length < 6) {
    showError('newPasswordError', 'Şifre en az 6 karakter olmalı');
    return;
  }

  if (newPassword !== confirmPassword) {
    showError('confirmPasswordError', 'Şifreler eşleşmiyor');
    return;
  }

  document.getElementById('loading')!.classList.add('show');
  (document.getElementById('submitBtn') as HTMLButtonElement).disabled = true;

  try {
    let finalAccessToken = accessToken;

    if (!finalAccessToken && token) {
      const verifyUrl = SUPABASE_URL + '/auth/v1/verify?token=' + encodeURIComponent(token) + '&type=recovery';
      const verifyResponse = await fetch(verifyUrl, {
        method: 'GET',
        headers: {
          'apikey': SUPABASE_ANON_KEY,
          'Authorization': 'Bearer ' + SUPABASE_ANON_KEY,
        },
      });

      if (!verifyResponse.ok) {
        throw new Error('Token geçersiz veya süresi dolmuş. Lütfen yeni bir şifre sıfırlama isteği gönderin.');
      }

      try {
        const verifyData = await verifyResponse.json();
        finalAccessToken = verifyData.access_token;
      } catch (_e) {
        const cookies = verifyResponse.headers.get('Set-Cookie');
        if (cookies) {
          const cookieMatch = cookies.match(/sb-access-token=([^;]+)/);
          if (cookieMatch) {
            finalAccessToken = decodeURIComponent(cookieMatch[1]);
          }
        }
      }
    }

    if (!finalAccessToken) {
      throw new Error('Oturum oluşturulamadı. Lütfen tekrar deneyin.');
    }

    const updateResponse = await fetch(SUPABASE_URL + '/auth/v1/user', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'apikey': SUPABASE_ANON_KEY,
        'Authorization': 'Bearer ' + finalAccessToken,
      },
      body: JSON.stringify({ password: newPassword }),
    });

    if (!updateResponse.ok) {
      let errorMessage = 'Şifre güncellenemedi. Lütfen tekrar deneyin.';
      try {
        const errorData = await updateResponse.json();
        errorMessage = errorData.message || errorData.error_description || errorMessage;
      } catch (_e) {
        // ignore
      }
      throw new Error(errorMessage);
    }

    showMessage('Şifreniz başarıyla güncellendi! Artık yeni şifrenizle giriş yapabilirsiniz.', 'success');
    document.getElementById('resetForm').style.display = 'none';

    setTimeout(() => {
      window.close();
    }, 5000);
  } catch (error) {
    console.error('Password reset error:', error);
    const msg = (error && (error as any).message) ? (error as any).message : 'Şifre güncellenirken bir hata oluştu. Lütfen tekrar deneyin.';
    showMessage(msg, 'error');
  } finally {
    document.getElementById('loading')!.classList.remove('show');
    (document.getElementById('submitBtn') as HTMLButtonElement).disabled = false;
  }
});

function showMessage(message, type) {
  const messageEl = document.getElementById('message');
  if (!messageEl) return;
  messageEl.textContent = message;
  messageEl.className = 'message ' + type + ' show';

  if (type === 'success') {
    setTimeout(() => {
      messageEl.classList.remove('show');
    }, 5000);
  }
}

function showError(elementId, message) {
  const errorEl = document.getElementById(elementId);
  if (!errorEl) return;
  errorEl.textContent = message;
  errorEl.classList.add('show');
}

function clearErrors() {
  document.querySelectorAll('.error').forEach((el) => {
    el.classList.remove('show');
  });
}
`;

  return new Response(js, {
    headers: {
      'Content-Type': 'application/javascript; charset=utf-8',
      'Access-Control-Allow-Origin': '*',
    },
  });
});




