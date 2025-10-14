// deno-lint-ignore-file no-explicit-any
import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.4";

type CreateQrInput = {
  assignment_id: number;
  expire_after_minutes: number; // 5,10,15,30
};

type QrPayload = {
  assignment_id: number;
  created_at: string; // ISO
  expire_after: number; // minutes
};

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" },
  });
}

serve(async (req) => {
  if (req.method !== "POST") {
    return jsonResponse(405, { error: "Method not allowed" });
  }

  const url = new URL(req.url);
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

  const { assignment_id, expire_after_minutes } = input ?? {} as CreateQrInput;
  if (!assignment_id || !expire_after_minutes || expire_after_minutes <= 0) {
    return jsonResponse(400, { error: "assignment_id and positive expire_after_minutes required" });
  }

  const supabase = createClient(supabaseUrl, supabaseServiceRoleKey, {
    auth: { persistSession: false },
  });

  const createdAt = new Date().toISOString();

  // Insert a record to allow later auditing/validation on the server
  const { data, error } = await supabase
    .from("qr_codes")
    .insert({
      assignment_id,
      expire_after_minutes,
      created_at: createdAt,
      is_active: true,
    })
    .select("id, assignment_id, created_at, expire_after_minutes")
    .single();

  if (error || !data) {
    return jsonResponse(500, { error: "Failed to create QR", details: error?.message });
  }

  const payload: QrPayload = {
    assignment_id: data.assignment_id,
    created_at: data.created_at,
    expire_after: data.expire_after_minutes,
  };

  // The frontend can encode this payload into a QR code
  return jsonResponse(200, { qr: payload, id: data.id });
});


