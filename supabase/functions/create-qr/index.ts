// Deno Deploy/Edge Function: Create QR for a course/week
// Request: { course_id: number, week_number: number, expire_after_minutes: number }
// Response: { id, qr: { course_id, week_number, created_at, expire_after } }

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.5";

type CreateQrInput = {
  course_id?: number;
  week_number?: number;
  expire_after_minutes?: number;
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
    return jsonResponse(405, { error: "Method not allowed" });
  }

  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const supabaseServiceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
  if (!supabaseUrl || !supabaseServiceRoleKey) {
    return jsonResponse(500, { error: "Missing Supabase service envs" });
  }

  let input: CreateQrInput;
  try {
    input = await req.json();
  } catch {
    return jsonResponse(400, { error: "Invalid JSON" });
  }

  const course_id = Number(input.course_id);
  const week_number = Number(input.week_number);
  const expire_after_minutes = Number(input.expire_after_minutes ?? 15);

  if (!course_id || !week_number || !Number.isFinite(expire_after_minutes) || expire_after_minutes <= 0) {
    return jsonResponse(400, { error: "course_id, week_number, expire_after_minutes required" });
  }

  const supabase = createClient(supabaseUrl, supabaseServiceRoleKey, { auth: { persistSession: false } });

  // Ensure no active QR exists for this course/week
  const { data: existing, error: existErr } = await supabase
    .from("qr_codes")
    .select("id")
    .eq("course_id", course_id)
    .eq("week_number", week_number)
    .eq("is_active", true)
    .maybeSingle();

  if (existErr) {
    return jsonResponse(500, { error: existErr.message });
  }

  if (existing) {
    return jsonResponse(409, { error: "Bu hafta için zaten aktif bir QR var" });
  }

  // Insert new QR record
  const { data: inserted, error: insErr } = await supabase
    .from("qr_codes")
    .insert({ course_id, week_number, expire_after_minutes, is_active: true })
    .select("id, created_at")
    .single();

  if (insErr || !inserted) {
    return jsonResponse(500, { error: insErr?.message ?? "Insert failed" });
  }

  const response = {
    id: inserted.id,
    qr: {
      course_id,
      week_number,
      created_at: inserted.created_at,
      expire_after: expire_after_minutes,
    },
  };

  return jsonResponse(200, response);
});