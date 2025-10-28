// Setup type definitions for built-in Supabase Runtime APIs
import "jsr:@supabase/functions-js/edge-runtime.d.ts";

interface StudentSignupPayload {
  email: string;
  password: string;
}

console.info("student-signup started");

Deno.serve(async (req: Request) => {
  const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  };

  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    console.log("student-signup received request");
    const { email, password }: StudentSignupPayload = await req.json();
    
    console.log("Parsed email:", email);
    
    if (!email || !password) {
      return new Response(JSON.stringify({ 
        ok: false, 
        error: "email_and_password_required" 
      }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    if (password.length < 6) {
      return new Response(JSON.stringify({ 
        ok: false, 
        error: "password_too_short" 
      }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // Supabase values
    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";
    
    // Service Role Key - DEĞİŞTİRMELİSİN!
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "YOUR_SERVICE_ROLE_KEY_HERE";
    
    console.log("Service key length:", serviceKey?.length || 0);
    
    console.log("Using Supabase URL:", supabaseUrl);
    console.log("Using Service Key:", serviceKey ? "Present" : "Missing");

    // 1. Whitelist kontrolü - students tablosunda email var mı?
    const whitelistUrl = `${supabaseUrl}/rest/v1/students?select=id,full_name,class_id,department_id&email=eq.${encodeURIComponent(email)}`;
    
    console.log("Whitelist URL:", whitelistUrl);
    console.log("Checking email:", email);
    
    const whitelistResp = await fetch(whitelistUrl, {
      headers: {
        apikey: serviceKey,
        Authorization: `Bearer ${serviceKey}`,
        Accept: "application/json",
      },
    });

    console.log("Whitelist response status:", whitelistResp.status);
    const whitelistData = await whitelistResp.json();
    console.log("Whitelist response data:", JSON.stringify(whitelistData));

    if (!whitelistResp.ok) {
      console.error("Whitelist check failed:", whitelistResp.status);
      return new Response(JSON.stringify({ 
        ok: false, 
        error: "whitelist_check_failed",
        details: JSON.stringify(whitelistData)
      }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    if (!Array.isArray(whitelistData) || whitelistData.length === 0) {
      console.error("Email not found in whitelist:", email);
      return new Response(JSON.stringify({ 
        ok: false, 
        error: "email_not_whitelisted" 
      }), {
        status: 403,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const studentData = whitelistData[0];

    // 2. Supabase Auth'da kullanıcı oluştur
    const authUrl = `${supabaseUrl}/auth/v1/admin/users`;
    const authPayload = {
      email: email,
      password: password,
      email_confirm: true, // Otomatik email confirmation
      user_metadata: {
        full_name: studentData.full_name,
        role: "student"
      }
    };

    const authResp = await fetch(authUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${serviceKey}`,
        "apikey": serviceKey,
      },
      body: JSON.stringify(authPayload),
    });

    if (!authResp.ok) {
      const authError = await authResp.text();
      console.error("Auth user creation failed:", authResp.status, authError);
      
      // Eğer kullanıcı zaten varsa, farklı hata döndür
      if (authResp.status === 422) {
        return new Response(JSON.stringify({ 
          ok: false, 
          error: "user_already_exists" 
        }), {
          status: 409,
          headers: { "Content-Type": "application/json", ...corsHeaders },
        });
      }
      
      return new Response(JSON.stringify({ 
        ok: false, 
        error: "auth_creation_failed" 
      }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const authUser = await authResp.json();
    const userId = authUser.id;

    // 3. profiles tablosuna öğrenci kaydı ekle
    const profileUrl = `${supabaseUrl}/rest/v1/profiles`;
    const profilePayload = {
      id: userId,
      full_name: studentData.full_name,
      role: "student"
    };

    const profileResp = await fetch(profileUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${serviceKey}`,
        "apikey": serviceKey,
        "Prefer": "return=minimal"
      },
      body: JSON.stringify(profilePayload),
    });

    if (!profileResp.ok) {
      const profileError = await profileResp.text();
      console.error("Profile creation failed:", profileResp.status, profileError);
      
      // Auth kullanıcısını sil (rollback)
      await fetch(`${supabaseUrl}/auth/v1/admin/users/${userId}`, {
        method: "DELETE",
        headers: {
          "Authorization": `Bearer ${serviceKey}`,
          "apikey": serviceKey,
        },
      });
      
      return new Response(JSON.stringify({ 
        ok: false, 
        error: "profile_creation_failed" 
      }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // 4. students tablosunda auth_user_id güncelle (opsiyonel)
    const updateStudentUrl = `${supabaseUrl}/rest/v1/students?id=eq.${studentData.id}`;
    const updateStudentPayload = {
      auth_user_id: userId
    };

    const updateStudentResp = await fetch(updateStudentUrl, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${serviceKey}`,
        "apikey": serviceKey,
        "Prefer": "return=minimal"
      },
      body: JSON.stringify(updateStudentPayload),
    });

    if (!updateStudentResp.ok) {
      console.warn("Student table update failed, but user created successfully");
    }

    return new Response(JSON.stringify({ 
      ok: true, 
      message: "Student registered successfully",
      user_id: userId
    }), {
      status: 200,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });

  } catch (err) {
    console.error("Unhandled error in student-signup:", err);
    return new Response(JSON.stringify({ 
      ok: false, 
      error: "unexpected_error" 
    }), {
      status: 500,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  }
});
