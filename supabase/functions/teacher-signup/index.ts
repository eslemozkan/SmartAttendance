// Setup type definitions for built-in Supabase Runtime APIs
import "jsr:@supabase/functions-js/edge-runtime.d.ts";

interface TeacherSignupPayload {
  email: string;
  password: string;
}

console.info("[teacher-signup] function cold start");

Deno.serve(async (req: Request) => {
  const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
  } as const;

  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    console.log("[teacher-signup] incoming:", req.method, new URL(req.url).pathname);
    const { email, password }: TeacherSignupPayload = await req.json();
    console.log("[teacher-signup] parsed email:", email);

    if (!email || !password) {
      return new Response(JSON.stringify({ ok: false, error: "email_and_password_required" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    if (password.length < 6) {
      return new Response(JSON.stringify({ ok: false, error: "password_too_short" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // Supabase values
    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "YOUR_SERVICE_ROLE_KEY_HERE";
    // Use anon key only for whitelist SELECT (RLS-friendly); keep serviceKey for Auth + profile update
    const anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI";
    console.log("[teacher-signup] supabaseUrl:", supabaseUrl);
    console.log("[teacher-signup] serviceKey length:", serviceKey ? serviceKey.length : 0);

    // 1) Whitelist check in profiles for teachers
    const whitelistUrl = `${supabaseUrl}/rest/v1/profiles?select=id,full_name,email,auth_user_id,department_id&email=eq.${encodeURIComponent(email)}&role=eq.teacher`;
    console.log("[teacher-signup] whitelist URL:", whitelistUrl);
    const whitelistResp = await fetch(whitelistUrl, {
      headers: {
        apikey: anonKey,
        Authorization: `Bearer ${anonKey}`,
        Accept: "application/json",
      },
    });
    console.log("[teacher-signup] whitelist status:", whitelistResp.status);
    const whitelistData = await whitelistResp.json();
    console.log("[teacher-signup] whitelist data:", JSON.stringify(whitelistData));

    if (!whitelistResp.ok) {
      return new Response(JSON.stringify({ ok: false, error: "whitelist_check_failed", details: whitelistData }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    if (!Array.isArray(whitelistData) || whitelistData.length === 0) {
      return new Response(JSON.stringify({ ok: false, error: "email_not_whitelisted" }), {
        status: 403,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const teacherRow = whitelistData[0];

    if (teacherRow.auth_user_id) {
      return new Response(JSON.stringify({ ok: false, error: "user_already_registered" }), {
        status: 409,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // 2) Create Auth user
    const authUrl = `${supabaseUrl}/auth/v1/admin/users`;
    const authPayload = {
      email,
      password,
      email_confirm: true,
      user_metadata: {
        full_name: teacherRow.full_name,
        role: "teacher",
      },
    };

    console.log("[teacher-signup] creating auth user at:", authUrl);
    const authResp = await fetch(authUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${serviceKey}`,
        apikey: serviceKey,
      },
      body: JSON.stringify(authPayload),
    });
    console.log("[teacher-signup] auth create status:", authResp.status);
    if (!authResp.ok) {
      const errText = await authResp.text();
      console.log("[teacher-signup] auth create error:", errText);
      if (authResp.status === 422) {
        return new Response(JSON.stringify({ ok: false, error: "user_already_exists" }), {
          status: 409,
          headers: { "Content-Type": "application/json", ...corsHeaders },
        });
      }
      return new Response(JSON.stringify({ ok: false, error: "auth_creation_failed", details: errText }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const authUser = await authResp.json();
    const userId = authUser.id as string;

    // 3) Link profiles row to auth user (update auth_user_id)
    const profileUpdateUrl = `${supabaseUrl}/rest/v1/profiles?id=eq.${teacherRow.id}`;
    const profileUpdatePayload = {
      auth_user_id: userId,
      email: email,
      role: "teacher",
    };

    console.log("[teacher-signup] updating profile at:", profileUpdateUrl);
    const profileUpdateResp = await fetch(profileUpdateUrl, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${serviceKey}`,
        apikey: serviceKey,
        Prefer: "return=minimal",
      },
      body: JSON.stringify(profileUpdatePayload),
    });
    console.log("[teacher-signup] profile update status:", profileUpdateResp.status);
    if (!profileUpdateResp.ok) {
      // rollback auth user
      await fetch(`${supabaseUrl}/auth/v1/admin/users/${userId}`, {
        method: "DELETE",
        headers: {
          Authorization: `Bearer ${serviceKey}`,
          apikey: serviceKey,
        },
      });
      const errText = await profileUpdateResp.text();
      console.log("[teacher-signup] profile update error:", errText);
      return new Response(JSON.stringify({ ok: false, error: "profile_update_failed", details: errText }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    return new Response(JSON.stringify({ ok: true, message: "Teacher registered successfully", user_id: userId }), {
      status: 200,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  } catch (err) {
    console.error("[teacher-signup] unhandled error:", err);
    return new Response(JSON.stringify({ ok: false, error: "unexpected_error" }), {
      status: 500,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  }
});


