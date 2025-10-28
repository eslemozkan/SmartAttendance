// Setup type definitions for built-in Supabase Runtime APIs
import "jsr:@supabase/functions-js/edge-runtime.d.ts";

interface CheckPayload {
  email: string;
}

console.info("check-student-whitelist started");

Deno.serve(async (req: Request) => {
  const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  };

  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const { email }: CheckPayload = await req.json();
    if (!email) {
      return new Response(JSON.stringify({ ok: false, error: "email_required" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL");
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    if (!supabaseUrl || !serviceKey) {
      console.error("Missing env SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY");
      return new Response(JSON.stringify({ ok: false, error: "server_misconfigured" }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const url = `${supabaseUrl}/rest/v1/students?select=id&email=eq.${encodeURIComponent(email)}`;
    const resp = await fetch(url, {
      headers: {
        apikey: serviceKey,
        Authorization: `Bearer ${serviceKey}`,
        Accept: "application/json",
      },
    });

    if (!resp.ok) {
      const txt = await resp.text();
      console.error("REST error:", resp.status, txt);
      return new Response(JSON.stringify({ ok: false, error: "db_error" }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const rows = await resp.json();
    const allowed = Array.isArray(rows) && rows.length > 0;

    return new Response(JSON.stringify({ ok: allowed }), {
      status: 200,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  } catch (err) {
    console.error("Unhandled error", err);
    return new Response(JSON.stringify({ ok: false, error: "unexpected" }), {
      status: 500,
      headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
    });
  }
});


