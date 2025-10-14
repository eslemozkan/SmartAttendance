// deno-lint-ignore-file no-explicit-any
import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.4";

type ValidateQrInput = {
  assignment_id: number;
  created_at: string; // ISO
  expire_after: number; // minutes
  student_id: string;
};

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" },
  });
}

function isExpired(createdAtIso: string, expireAfterMinutes: number): boolean {
  const createdAtMs = Date.parse(createdAtIso);
  if (Number.isNaN(createdAtMs)) return true;
  const now = Date.now();
  const expireMs = expireAfterMinutes * 60 * 1000;
  return now - createdAtMs > expireMs;
}

serve(async (req) => {
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

  const { assignment_id, created_at, expire_after, student_id } = input ?? {} as ValidateQrInput;
  if (!assignment_id || !created_at || !expire_after || !student_id) {
    return jsonResponse(400, { error: "assignment_id, created_at, expire_after, student_id required" });
  }

  if (isExpired(created_at, expire_after)) {
    return jsonResponse(410, { error: "Bu yoklamanın süresi dolmuş" });
  }

  const supabase = createClient(supabaseUrl, supabaseServiceRoleKey, {
    auth: { persistSession: false },
  });

  // Optional: check that such a QR was issued (by matching assignment + created_at window)
  const { data: issued } = await supabase
    .from("qr_codes")
    .select("id, created_at, expire_after_minutes, is_active")
    .eq("assignment_id", assignment_id)
    .gte("created_at", new Date(Date.parse(created_at) - 5 * 60 * 1000).toISOString())
    .order("created_at", { ascending: false })
    .limit(1)
    .maybeSingle();

  if (!issued || issued.is_active === false) {
    return jsonResponse(404, { error: "QR bulunamadı veya pasif" });
  }

  // Insert attendance record; you should adapt table/columns to your schema
  const { error: attErr } = await supabase
    .from("attendances")
    .insert({
      assignment_id,
      student_id,
      marked_at: new Date().toISOString(),
      method: "qr",
    });

  if (attErr) {
    // If duplicate attendance constraint exists, surface a friendly message
    const msg = attErr.message?.toLowerCase?.() ?? "";
    if (msg.includes("duplicate") || msg.includes("unique")) {
      return jsonResponse(409, { error: "Bu öğrenci için zaten yoklama alınmış" });
    }
    return jsonResponse(500, { error: "Attendance insert failed", details: attErr.message });
  }

  return jsonResponse(200, { ok: true });
});


