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
  session_numbers?: number[]; // Array of session numbers to mark as completed (e.g., [1, 2] for 2 sessions)
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
    const session_numbers = Array.isArray(input.session_numbers) 
      ? input.session_numbers.map(n => Number(n)).filter(n => Number.isFinite(n) && n > 0)
      : [];

    if (!course_id || !Number.isFinite(course_id) || course_id <= 0 || !week_number || !Number.isFinite(expire_after_minutes) || expire_after_minutes <= 0) {
      return jsonResponse(400, { error: "course_id (BIGINT), week_number and expire_after_minutes (positive numbers) required" });
    }

    // Validate session_numbers if provided
    if (session_numbers.length === 0) {
      return jsonResponse(400, { error: "session_numbers array is required and must contain at least one session number" });
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

    // Check if any of the requested sessions are already completed
    if (session_numbers.length > 0) {
      const { data: existingSessions, error: sessionsCheckError } = await supabase
        .from("course_weekly_sessions")
        .select("session_number, qr_code_id")
        .eq("course_id", course_id)
        .eq("week_number", week_number)
        .in("session_number", session_numbers)
        .not("qr_code_id", "is", null);

      if (sessionsCheckError) {
        console.error("Error checking existing sessions:", sessionsCheckError);
        return jsonResponse(500, { error: "Error checking existing sessions", details: sessionsCheckError.message });
      }

      if (existingSessions && existingSessions.length > 0) {
        const completedSessions = existingSessions.map(s => s.session_number).join(", ");
        return jsonResponse(409, { 
          error: "Bu oturumlar zaten işlenmiş", 
          completed_sessions: existingSessions.map(s => s.session_number),
          message: `Oturum ${completedSessions} için zaten QR kod oluşturulmuş`
        });
      }
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
    console.log("QR code details:", JSON.stringify(inserted, null, 2));

    // Now create/update course_weekly_sessions records for the selected sessions
    if (session_numbers.length > 0) {
      console.log(`Creating ${session_numbers.length} session record(s) for QR ${inserted.id}`);
      console.log("Session numbers:", session_numbers);
      
      const sessionRecords = session_numbers.map(session_number => ({
        course_id,
        week_number,
        session_number,
        qr_code_id: inserted.id,
      }));

      console.log("Session records to insert:", JSON.stringify(sessionRecords, null, 2));

      // Use upsert to handle existing records (in case they exist without qr_code_id)
      const { data: upsertedSessions, error: sessionsError } = await supabase
        .from("course_weekly_sessions")
        .upsert(sessionRecords, {
          onConflict: "course_id,week_number,session_number",
          ignoreDuplicates: false,
        })
        .select();

      if (sessionsError) {
        console.error("Error inserting session records:", sessionsError);
        console.error("Error details:", JSON.stringify(sessionsError, null, 2));
        // Return error instead of silently continuing
        return jsonResponse(500, { 
          error: "Failed to create session records", 
          details: sessionsError.message,
          hint: "Check if course_weekly_sessions table exists and has correct structure"
        });
      } else {
        console.log(`Successfully created ${upsertedSessions?.length || session_numbers.length} session record(s)`);
        console.log("Upserted sessions:", JSON.stringify(upsertedSessions, null, 2));
        
        // Verify the records were actually created
        const { data: verifySessions, error: verifyError } = await supabase
          .from("course_weekly_sessions")
          .select("id, session_number, qr_code_id")
          .eq("course_id", course_id)
          .eq("week_number", week_number)
          .eq("qr_code_id", inserted.id);
        
        if (verifyError) {
          console.error("Error verifying session records:", verifyError);
        } else {
          console.log(`Verified: Found ${verifySessions?.length || 0} session record(s) for QR ${inserted.id}`);
          if (verifySessions && verifySessions.length !== session_numbers.length) {
            console.warn(`Warning: Expected ${session_numbers.length} sessions but found ${verifySessions.length}`);
          }
        }
      }
    } else {
      console.warn("No session_numbers provided, skipping course_weekly_sessions creation");
    }

    const response = {
      id: inserted.id,
      qr: {
        course_id,
        week_number,
        created_at: inserted.created_at,
        expire_after: expire_after_minutes,
        teacher_latitude: inserted.teacher_latitude || null,
        teacher_longitude: inserted.teacher_longitude || null,
        session_numbers: session_numbers, // Include in response
      },
      sessions_completed: session_numbers.length,
    };

    return jsonResponse(200, response);
  } catch (error) {
    console.error("Unexpected error:", error);
    return jsonResponse(500, { error: `Unexpected error: ${error instanceof Error ? error.message : String(error)}` });
  }
});