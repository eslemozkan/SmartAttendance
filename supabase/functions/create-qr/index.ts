// Deno Deploy/Edge Function: Create QR for a course/week
// Request: { course_id: number, week_number: number, expire_after_minutes: number }
// Response: { id, qr: { course_id, week_number, created_at, expire_after } }

import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.5";

type CreateQrInput = {
  course_id?: string | number; // BIGINT number (courses.id is BIGINT)
  week_number?: number;
  expire_after_minutes?: number;
  teacher_latitude?: number;
  teacher_longitude?: number;
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

  try {
    // Use hardcoded URL (same as other functions)
    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";
    
    // Try to get service role key from environment, fallback to header if not available
    const headerAuth = req.headers.get("authorization") || req.headers.get("Authorization") || "";
    const bearer = headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : "";
    const headerApiKey = req.headers.get("apikey") || req.headers.get("x-apikey") || "";
    const serviceKeyEnv = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
    
    // Prefer service role key from env, fallback to header keys
    const supabaseServiceRoleKey = serviceKeyEnv || bearer || headerApiKey;
    
    if (!supabaseServiceRoleKey) {
      console.error("Missing Supabase service role key");
      return jsonResponse(500, { error: "Missing Supabase service role key. Please set SUPABASE_SERVICE_ROLE_KEY environment variable or provide it in Authorization header." });
    }

    let input: CreateQrInput;
    try {
      input = await req.json();
    } catch (e) {
      console.error("JSON parse error:", e);
      return jsonResponse(400, { error: "Invalid JSON" });
    }

    // course_id should be a BIGINT number (courses.id is BIGINT, not UUID)
    const course_id = Number(input.course_id);

    const week_number = Number(input.week_number);
    const expire_after_minutes = Number(input.expire_after_minutes ?? 15);
    const teacher_latitude = input.teacher_latitude ? Number(input.teacher_latitude) : null;
    const teacher_longitude = input.teacher_longitude ? Number(input.teacher_longitude) : null;

    if (!course_id || !Number.isFinite(course_id) || course_id <= 0 || !week_number || !Number.isFinite(expire_after_minutes) || expire_after_minutes <= 0) {
      return jsonResponse(400, { error: "course_id (BIGINT), week_number and expire_after_minutes (positive numbers) required" });
    }

    const supabase = createClient(supabaseUrl, supabaseServiceRoleKey, { 
      auth: { persistSession: false },
      db: { schema: 'public' }
    });

    console.log("Checking for existing QR codes...");
    console.log("Query params: course_id=" + course_id + ", week_number=" + week_number);
    
    // Ensure no active QR exists for this course/week
    const { data: existing, error: existErr } = await supabase
      .from("qr_codes")
      .select("id")
      .eq("course_id", course_id)
      .eq("week_number", week_number)
      .eq("is_active", true)
      .maybeSingle();

    if (existErr) {
      console.error("Error checking existing QR:", existErr);
      console.error("Error details:", JSON.stringify(existErr, null, 2));
      
      // Check if it's a column doesn't exist error
      const errorMsg = existErr.message || String(existErr);
      if (errorMsg.includes("column") && (errorMsg.includes("does not exist") || errorMsg.includes("undefined"))) {
        return jsonResponse(500, { 
          error: "Database schema error: Missing columns (course_id or week_number).",
          details: errorMsg,
          hint: "Please run migration: 20250115_fix_qr_codes_complete.sql in Supabase SQL Editor"
        });
      }
      
      return jsonResponse(500, { 
        error: `Database error: ${errorMsg}`,
        details: (existErr as any).details || null,
        hint: (existErr as any).hint || null
      });
    }
    
    console.log("Existing QR check completed:", existing ? "Found existing" : "No existing");

    if (existing) {
      return jsonResponse(409, { error: "Bu hafta için zaten aktif bir QR var" });
    }

    console.log("Inserting new QR code...");
    console.log("Location:", { teacher_latitude, teacher_longitude });
    
    // Insert new QR record with location if provided
    const insertData: any = { 
      course_id, 
      week_number, 
      expire_after_minutes, 
      is_active: true 
    };
    
    if (teacher_latitude != null && teacher_longitude != null) {
      insertData.teacher_latitude = teacher_latitude;
      insertData.teacher_longitude = teacher_longitude;
    }
    
    const { data: inserted, error: insErr } = await supabase
      .from("qr_codes")
      .insert(insertData)
      .select("id, created_at, teacher_latitude, teacher_longitude")
      .single();

    if (insErr) {
      console.error("Error inserting QR:", insErr);
      console.error("Error details:", JSON.stringify(insErr, null, 2));
      return jsonResponse(500, { 
        error: `Insert failed: ${insErr.message}`,
        details: insErr.details || null,
        hint: insErr.hint || null,
        code: insErr.code || null
      });
    }

    if (!inserted) {
      console.error("Insert returned no data");
      return jsonResponse(500, { error: "Insert failed: No data returned" });
    }
    
    console.log("QR code inserted successfully:", inserted.id);

    const response = {
      id: inserted.id,
      qr: {
        course_id,
        week_number,
        created_at: inserted.created_at,
        expire_after: expire_after_minutes,
        teacher_latitude: inserted.teacher_latitude || null,
        teacher_longitude: inserted.teacher_longitude || null,
      },
    };

    return jsonResponse(200, response);
  } catch (error) {
    console.error("Unexpected error:", error);
    return jsonResponse(500, { error: `Unexpected error: ${error instanceof Error ? error.message : String(error)}` });
  }
});