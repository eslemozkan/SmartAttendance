// Deno Deploy/Edge Function: Validate QR and insert attendance
// Request: { course_id: number, week_number: number, created_at: string, expire_after: number, student_id: string }
// Response: { ok: true }

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.5";

type ValidateQrInput = {
  course_id?: number;
  week_number?: number;
  created_at?: string;
  expire_after?: number;
  student_id?: string;
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

function isExpired(createdAtIso: string, expireAfterMinutes: number): boolean {
  const createdAtMs = Date.parse(createdAtIso);
  if (!Number.isFinite(createdAtMs)) return true;
  const now = Date.now();
  const expireMs = expireAfterMinutes * 60 * 1000;
  return now - createdAtMs > expireMs;
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

  let input: ValidateQrInput;
  try {
    input = await req.json();
  } catch {
    return jsonResponse(400, { error: "Invalid JSON" });
  }

  const { course_id, week_number, created_at, expire_after, student_id } = input ?? {} as ValidateQrInput;
  if (!course_id || !week_number || !created_at || !expire_after || !student_id) {
    return jsonResponse(400, { error: "course_id, week_number, created_at, expire_after, student_id required" });
  }

  if (isExpired(created_at, expire_after)) {
    return jsonResponse(410, { error: "Bu yoklamanın süresi dolmuş" });
  }

  const supabase = createClient(supabaseUrl, supabaseServiceRoleKey, { auth: { persistSession: false } });

  // check that such an active QR exists for this course/week and is recent
  const { data: qr, error: qrErr } = await supabase
    .from("qr_codes")
    .select("id, created_at, expire_after_minutes, is_active")
    .eq("course_id", course_id)
    .eq("week_number", week_number)
    .eq("is_active", true)
    .order("created_at", { ascending: false })
    .limit(1)
    .maybeSingle();

  if (qrErr) {
    return jsonResponse(500, { error: qrErr.message });
  }
  if (!qr || qr.is_active === false) {
    return jsonResponse(404, { error: "QR bulunamadı veya pasif" });
  }

  // insert attendance (unique constraint in DB prevents duplicate per student per week)
  const { error: attErr } = await supabase
    .from("attendances")
    .insert({ course_id, week_number, student_id, qr_code_id: qr.id, method: "qr" });

  if (attErr) {
    if ((attErr as any).code === "23505") {
      return jsonResponse(409, { error: "Bu öğrenci için zaten yoklama alınmış" });
    }
    return jsonResponse(500, { error: attErr.message });
  }

  return jsonResponse(200, { ok: true });
});