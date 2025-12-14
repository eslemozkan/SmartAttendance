// Deno Deploy/Edge Function: Reset Password
// Request: { email: string }
// Response: { ok: boolean, message?: string, error?: string }

import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.5";

type ResetPasswordInput = {
  email?: string;
};

function jsonResponse(status: number, body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "*",
      "Access-Control-Allow-Methods": "POST, OPTIONS"
    },
  });
}

Deno.serve(async (req) => {
  // CORS preflight
  if (req.method === "OPTIONS") {
    return new Response(null, {
      status: 204,
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "*",
        "Access-Control-Allow-Methods": "POST, OPTIONS",
      },
    });
  }
  
  if (req.method !== "POST") {
    return jsonResponse(405, { ok: false, error: "Method not allowed" });
  }

  try {
    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";
    
    // Try to get service role key from environment (secret), fallback to header if not available
    const headerAuth = req.headers.get("authorization") || req.headers.get("Authorization") || "";
    const bearer = headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : "";
    const headerApiKey = req.headers.get("apikey") || req.headers.get("x-apikey") || "";
    
    // Get service role key from Supabase secrets (secret name, not the value!)
    const serviceKeyEnv = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
    
    // Prefer service role key from env (secret), fallback to header keys
    const supabaseServiceRoleKey = serviceKeyEnv || bearer || headerApiKey;
    
    if (!supabaseServiceRoleKey) {
      console.error("Missing Supabase service role key");
      return jsonResponse(500, { ok: false, error: "Missing Supabase service role key" });
    }

    let input: ResetPasswordInput;
    try {
      input = await req.json();
    } catch (e) {
      console.error("JSON parse error:", e);
      return jsonResponse(400, { ok: false, error: "Invalid JSON" });
    }

    const email = input.email?.trim();
    if (!email) {
      return jsonResponse(400, { ok: false, error: "Email is required" });
    }

    console.log("Resetting password for email:", email);
    console.log("Service role key available:", supabaseServiceRoleKey ? "Yes" : "No");
    console.log("Service key from env:", serviceKeyEnv ? "Yes" : "No");

    const supabase = createClient(supabaseUrl, supabaseServiceRoleKey, { 
      auth: { persistSession: false },
      db: { schema: 'public' }
    });

    // Generate password reset link using admin API
    // Supabase will handle the case where email doesn't exist (security feature)
    const { data: linkData, error: linkError } = await supabase.auth.admin.generateLink({
      type: 'recovery',
      email: email,
    });

    if (linkError || !linkData) {
      console.error("Error generating reset link:", linkError);
      return jsonResponse(500, { 
        ok: false, 
        error: linkError?.message || "Şifre sıfırlama bağlantısı oluşturulamadı" 
      });
    }

    const resetLink = linkData.properties?.action_link;
    console.log("Reset link generated successfully for:", email);

    // Try Supabase's built-in email system first (recommended)
    // This uses SMTP settings configured in Supabase Dashboard
    try {
      console.log("Attempting to send email via Supabase Auth API...");
      const recoverUrl = `${supabaseUrl}/auth/v1/recover`;
      const recoverPayload = JSON.stringify({ 
        email: email,
        redirect_to: `${supabaseUrl}/auth/v1/verify?token=#token_hash&type=recovery`
      });
      
      const recoverResponse = await fetch(recoverUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'apikey': supabaseServiceRoleKey,
          'Authorization': `Bearer ${supabaseServiceRoleKey}`
        },
        body: recoverPayload
      });

      const recoverBody = await recoverResponse.text();
      console.log("Supabase Auth API response status:", recoverResponse.status);
      console.log("Supabase Auth API response body:", recoverBody);

      if (recoverResponse.ok) {
        console.log("Email sent via Supabase Auth API");
        return jsonResponse(200, { 
          ok: true, 
          message: "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi. Lütfen e-postanızı (ve spam klasörünü) kontrol edin.",
          resetLink: resetLink,
          email: email,
          emailSent: true
        });
      } else if (recoverResponse.status === 429) {
        // Rate limit hatası - kullanıcıya açıklayıcı mesaj
        let recoverBodyJson;
        try {
          recoverBodyJson = JSON.parse(recoverBody);
        } catch {
          recoverBodyJson = { msg: "Çok sık istek gönderildi" };
        }
        console.warn("Supabase Auth API rate limit:", recoverBody);
        return jsonResponse(429, { 
          ok: false, 
          message: "Güvenlik nedeniyle, şifre sıfırlama isteği çok sık gönderilemez. Lütfen 60 saniye bekleyip tekrar deneyin.",
          resetLink: resetLink,
          email: email,
          emailSent: false,
          error: recoverBodyJson.msg || "Rate limit exceeded"
        });
      } else {
        console.warn("Supabase Auth API email sending failed:", recoverResponse.status, recoverBody);
      }
    } catch (e) {
      console.error("Supabase Auth API email sending exception:", e);
    }

    // Fallback: Try Resend API only for test email (if configured)
    const resendApiKey = Deno.env.get("RESEND_API_KEY");
    const testEmail = "eslemlestrange@gmail.com"; // Test email address
    
    if (resendApiKey && resendApiKey !== "" && email.toLowerCase() === testEmail.toLowerCase()) {
      console.log("Sending email via Resend API (test email only)...");
      
      try {
        const emailHtml = `
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background-color: #1976D2; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
        .content { padding: 20px; background-color: #f9f9f9; }
        .button { display: inline-block; padding: 12px 24px; background-color: #1976D2; color: white; text-decoration: none; border-radius: 4px; margin: 20px 0; }
        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>SmartAttendance</h1>
        </div>
        <div class="content">
            <h2>Şifre Sıfırlama</h2>
            <p>Merhaba,</p>
            <p>Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın:</p>
            <p style="text-align: center;">
                <a href="${resetLink}" class="button">Şifremi Sıfırla</a>
            </p>
            <p>Veya aşağıdaki bağlantıyı tarayıcınıza kopyalayın:</p>
            <p style="word-break: break-all; color: #1976D2; padding: 10px; background-color: #e3f2fd; border-radius: 4px;">${resetLink}</p>
            <p><strong>Bu bağlantı 1 saat süreyle geçerlidir.</strong></p>
            <p>Eğer bu isteği siz yapmadıysanız, bu e-postayı görmezden gelebilirsiniz.</p>
        </div>
        <div class="footer">
            <p>© 2024 SmartAttendance - Akademik Yoklama Sistemi</p>
        </div>
    </div>
</body>
</html>`;

        const resendResponse = await fetch("https://api.resend.com/emails", {
          method: "POST",
          headers: {
            "Authorization": `Bearer ${resendApiKey}`,
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            from: "SmartAttendance <onboarding@resend.dev>",
            to: [testEmail],
            subject: "Şifre Sıfırlama - SmartAttendance",
            html: emailHtml,
          }),
        });

        const resendBody = await resendResponse.json();
        console.log("Resend API response status:", resendResponse.status);

        if (resendResponse.ok) {
          console.log("Email sent successfully via Resend (test email)");
          return jsonResponse(200, { 
            ok: true, 
            message: "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi. Lütfen e-postanızı (ve spam klasörünü) kontrol edin.",
            resetLink: resetLink,
            email: email,
            emailSent: true
          });
        } else {
          console.error("Resend API error:", resendResponse.status, JSON.stringify(resendBody));
        }
      } catch (resendError) {
        console.error("Resend API exception:", resendError);
      }
    }

    // If all email sending fails, return the link directly
    return jsonResponse(200, { 
      ok: true, 
      message: "Şifre sıfırlama bağlantısı oluşturuldu. Aşağıdaki bağlantıyı kullanarak şifrenizi sıfırlayabilirsiniz.",
      resetLink: resetLink,
      email: email,
      emailSent: false
    });
  } catch (error) {
    console.error("Unexpected error:", error);
    return jsonResponse(500, { 
      ok: false, 
      error: `Unexpected error: ${error instanceof Error ? error.message : String(error)}` 
    });
  }
});

