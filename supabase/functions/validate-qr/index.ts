// Setup type definitions for built-in Supabase Runtime APIs
import "jsr:@supabase/functions-js/edge-runtime.d.ts";

console.info("validate-qr started");

Deno.serve(async (req) => {
  const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  };
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  try {
    const { course_id, week_number, created_at, expire_after, student_id, student_email, student_latitude, student_longitude } = await req.json();
    console.info("Input:", { course_id, week_number, created_at, expire_after, student_id, student_email, student_latitude, student_longitude });
    if (!course_id || !week_number) {
      return new Response(JSON.stringify({ ok: false, error: "missing_fields", required: ["course_id", "week_number"], got: { course_id, week_number } }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";
    const headerAuth = req.headers.get("authorization") || req.headers.get("Authorization") || "";
    const bearer = headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : "";
    const headerApiKey = req.headers.get("apikey") || req.headers.get("x-apikey") || "";
    const serviceKeyEnv = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
    // Prefer service role if available for reliability; else fall back to incoming anon key
    const apiKey = serviceKeyEnv || bearer || headerApiKey;
    if (!apiKey) {
      return new Response(JSON.stringify({ ok: false, error: "api_key_missing" }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // Resolve real student id (prefer profiles.id, fallback to students.id)
    let resolvedStudentProfileId: string | null = null;
    let resolvedStudentIdFromStudents: string | null = null;
    if (student_email) {
      const profUrl = `${supabaseUrl}/rest/v1/profiles?select=id,email&email=eq.${encodeURIComponent(student_email)}`;
      const profResp = await fetch(profUrl, {
        headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" },
      });
      const profData = await profResp.json();
      if (Array.isArray(profData) && profData.length > 0) {
        resolvedStudentProfileId = profData[0].id;
      }
      if (!resolvedStudentProfileId) {
        // try case-insensitive
        const profUrl2 = `${supabaseUrl}/rest/v1/profiles?select=id,email&email=ilike.${encodeURIComponent(student_email)}`;
        const profResp2 = await fetch(profUrl2, {
          headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" },
        });
        const profData2 = await profResp2.json();
        if (Array.isArray(profData2) && profData2.length > 0) {
          resolvedStudentProfileId = profData2[0].id;
        }
      }
      // fallback to students table by email → and ensure a matching profiles row exists
      if (!resolvedStudentProfileId) {
        const stuUrl = `${supabaseUrl}/rest/v1/students?select=id,email,full_name,department_id&email=eq.${encodeURIComponent(student_email)}`;
        const stuResp = await fetch(stuUrl, { headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" } });
        const stuData = await stuResp.json();
        if (Array.isArray(stuData) && stuData.length > 0) {
          // create or fetch profiles row for this email
          const stu = stuData[0];
          const profLookupUrl = `${supabaseUrl}/rest/v1/profiles?select=id&email=eq.${encodeURIComponent(stu.email)}`;
          const profLookupResp = await fetch(profLookupUrl, { headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" } });
          let profLookup = await profLookupResp.json();
          if (!Array.isArray(profLookup) || profLookup.length === 0) {
            const profileInsertUrl = `${supabaseUrl}/rest/v1/profiles`;
            const body = { email: stu.email, role: "student", full_name: stu.full_name ?? null, department_id: stu.department_id ?? null };
            const profInsResp = await fetch(profileInsertUrl, {
              method: "POST",
              headers: { "Content-Type": "application/json", apikey: apiKey, Authorization: `Bearer ${apiKey}`, Prefer: "return=representation" },
              body: JSON.stringify(body),
            });
            const profInsData = await profInsResp.json();
            if (profInsResp.ok && Array.isArray(profInsData) && profInsData.length > 0) {
              resolvedStudentProfileId = profInsData[0].id;
            }
          } else {
            resolvedStudentProfileId = profLookup[0].id;
          }
        }
      }
    }

    if (!resolvedStudentProfileId && !resolvedStudentIdFromStudents && student_id) {
      // fallback only if looks like UUID
      const uuidLike = typeof student_id === 'string' && /^[0-9a-fA-F-]{36}$/.test(student_id);
      if (uuidLike) {
        resolvedStudentProfileId = String(student_id);
      }
    }

    if (!resolvedStudentProfileId && !resolvedStudentIdFromStudents) {
      return new Response(JSON.stringify({ ok: false, error: "student_not_found" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // Check QR exists and is active, and verify location if available
    let teacherLatitude: number | null = null;
    let teacherLongitude: number | null = null;
    let qrCodeId: string | null = null;
    try {
      const qrUrl = `${supabaseUrl}/rest/v1/qr_codes?select=id,created_at,expire_after_minutes,teacher_latitude,teacher_longitude,is_active&course_id=eq.${course_id}&week_number=eq.${week_number}&is_active=eq.true&order=created_at.desc&limit=1`;
      const qrResp = await fetch(qrUrl, { headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" } });
      const qrData = await qrResp.json();
      if (Array.isArray(qrData) && qrData.length > 0) {
        const qr = qrData[0];
        
        // Check if QR code is expired
        const createdAt = new Date(qr.created_at);
        const expireAfterMinutes = qr.expire_after_minutes || 15;
        const expireTime = new Date(createdAt.getTime() + expireAfterMinutes * 60 * 1000);
        const now = new Date();
        
        if (now > expireTime) {
          console.info(`QR code expired: created at ${createdAt.toISOString()}, expires at ${expireTime.toISOString()}, now is ${now.toISOString()}`);
          return new Response(JSON.stringify({ ok: false, error: "qr_expired", message: "Bu yoklamanın süresi dolmuş" }), {
            status: 410,
            headers: { "Content-Type": "application/json", ...corsHeaders },
          });
        }
        
        qrCodeId = qr.id;
        teacherLatitude = qr.teacher_latitude;
        teacherLongitude = qr.teacher_longitude;
        
        // Location check: if both teacher and student locations are available, verify distance
        if (teacherLatitude != null && teacherLongitude != null && 
            student_latitude != null && student_longitude != null) {
          // Haversine formula to calculate distance in meters
          const R = 6371000; // Earth radius in meters
          const dLat = (student_latitude - teacherLatitude) * Math.PI / 180;
          const dLon = (student_longitude - teacherLongitude) * Math.PI / 180;
          const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(teacherLatitude * Math.PI / 180) * Math.cos(student_latitude * Math.PI / 180) *
                    Math.sin(dLon / 2) * Math.sin(dLon / 2);
          const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
          const distance = R * c; // Distance in meters
          
          const maxDistance = 30; // 30 meters
          if (distance > maxDistance) {
            console.info(`Location check failed: student is ${distance.toFixed(2)}m away (max: ${maxDistance}m)`);
            return new Response(JSON.stringify({ ok: false, error: "location_too_far", distance: distance.toFixed(2), maxDistance, message: "Konum uygun değil" }), {
              status: 403,
              headers: { "Content-Type": "application/json", ...corsHeaders },
            });
          }
          console.info(`Location check passed: student is ${distance.toFixed(2)}m away (within ${maxDistance}m)`);
        } else {
          console.info("Location check skipped: teacher location or student location not available");
        }
      } else {
        // No active QR found
        console.info("No active QR code found for this course and week");
        return new Response(JSON.stringify({ ok: false, error: "invalid_qr", message: "QR kod geçersiz veya aktif değil" }), {
          status: 404,
          headers: { "Content-Type": "application/json", ...corsHeaders },
        });
      }
    } catch (err) {
      console.error("Error checking QR or location:", err);
      return new Response(JSON.stringify({ ok: false, error: "database_error", message: "QR kod kontrolü sırasında hata oluştu" }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // Enrollment check: ensure student belongs to a class that has this course
    try {
      // 1) find student's auth_user_id via profiles
      let authUserId: string | null = null;
      const profDetailUrl = `${supabaseUrl}/rest/v1/profiles?select=auth_user_id&id=eq.${resolvedStudentProfileId}`;
      const profDetailResp = await fetch(profDetailUrl, { headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" } });
      const profDetailData = await profDetailResp.json();
      if (Array.isArray(profDetailData) && profDetailData.length > 0) {
        authUserId = profDetailData[0].auth_user_id || null;
      }

      // 2) get student's class_id
      let classId: string | null = null;
      if (authUserId) {
        const stuUrl = `${supabaseUrl}/rest/v1/students?select=class_id,auth_user_id&auth_user_id=eq.${authUserId}`;
        const stuResp = await fetch(stuUrl, { headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" } });
        const stuData = await stuResp.json();
        if (Array.isArray(stuData) && stuData.length > 0) {
          classId = stuData[0].class_id || null;
        }
      }

      // 3) verify course is assigned to student's class (course_class_assignments)
      if (classId) {
        const ccaUrl = `${supabaseUrl}/rest/v1/course_class_assignments?select=id&course_id=eq.${course_id}&class_id=eq.${classId}`;
        const ccaResp = await fetch(ccaUrl, { headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" } });
        const ccaData = await ccaResp.json();
        const allowed = Array.isArray(ccaData) && ccaData.length > 0;
        if (!allowed) {
          return new Response(JSON.stringify({ ok: false, error: "student_not_enrolled" }), {
            status: 403,
            headers: { "Content-Type": "application/json", ...corsHeaders },
          });
        }
      }
    } catch (_) {
      // If anything fails in enrollment check, keep permissive for dev, do not block
    }

    // Ensure we have a profiles.id for attendance; do NOT fallback to students.id to satisfy FK
    const studentIdForDuplicate = resolvedStudentProfileId;
    if (!studentIdForDuplicate) {
      return new Response(JSON.stringify({ ok: false, error: "profile_not_resolved" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }
    // Get sessions associated with this QR code
    let sessionNumbers: number[] = [];
    if (qrCodeId) {
      try {
        const sessionsUrl = `${supabaseUrl}/rest/v1/course_weekly_sessions?select=session_number&course_id=eq.${course_id}&week_number=eq.${week_number}&qr_code_id=eq.${qrCodeId}`;
        const sessionsResp = await fetch(sessionsUrl, {
          headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" },
        });
        const sessionsData = await sessionsResp.json();
        console.info(`Fetched sessions for QR ${qrCodeId}:`, sessionsData);
        if (Array.isArray(sessionsData) && sessionsData.length > 0) {
          sessionNumbers = sessionsData.map((s: any) => s.session_number);
          console.info(`Found ${sessionNumbers.length} session(s) for this QR code:`, sessionNumbers);
        } else {
          console.info(`No sessions found for QR ${qrCodeId}, will use default`);
        }
      } catch (err) {
        console.error("Error fetching sessions:", err);
        // Continue with default behavior (single attendance) if session fetch fails
      }
    } else {
      console.info(`No qrCodeId found, will use default session`);
    }

    // If no sessions found, default to single attendance (backward compatibility)
    if (sessionNumbers.length === 0) {
      sessionNumbers = [1]; // Default to session 1
      console.info(`Using default session number: 1`);
    }

    // Check for duplicate attendance for any of the sessions
    const dupUrl = `${supabaseUrl}/rest/v1/attendances?select=id,session_number&course_id=eq.${course_id}&week_number=eq.${week_number}&student_id=eq.${studentIdForDuplicate}`;
    const dupResp = await fetch(dupUrl, {
      headers: { apikey: apiKey, Authorization: `Bearer ${apiKey}`, Accept: "application/json" },
    });
    const dupData = await dupResp.json();
    console.info(`Checking duplicates: found ${Array.isArray(dupData) ? dupData.length : 0} existing attendance record(s)`);
    if (Array.isArray(dupData) && dupData.length > 0) {
      // Separate existing sessions by NULL and non-NULL
      const existingSessionsWithNumber = dupData.map((a: any) => a.session_number).filter((n: any) => n != null);
      const hasNullSession = dupData.some((a: any) => a.session_number == null);
      
      console.info(`Existing sessions with number: ${existingSessionsWithNumber.join(", ") || "none"}`);
      console.info(`Has NULL session: ${hasNullSession}`);
      console.info(`New QR session numbers: ${sessionNumbers.join(", ")}`);
      
      // If new QR has session numbers, check only against existing sessions with numbers
      // If old record has NULL session_number, it's a legacy record and shouldn't block new session-based attendance
      if (sessionNumbers.length > 0 && existingSessionsWithNumber.length > 0) {
        const allSessionsAttended = sessionNumbers.every(sn => existingSessionsWithNumber.includes(sn));
        if (allSessionsAttended) {
          console.info(`All sessions already attended: ${sessionNumbers.join(", ")}`);
          return new Response(JSON.stringify({ ok: true, message: "already_attended", sessions: existingSessionsWithNumber }), {
            status: 200,
            headers: { "Content-Type": "application/json", ...corsHeaders },
          });
        } else {
          const missingSessions = sessionNumbers.filter(sn => !existingSessionsWithNumber.includes(sn));
          console.info(`Some sessions not yet attended: ${missingSessions.join(", ")}`);
        }
      } else if (sessionNumbers.length === 0 && hasNullSession) {
        // If new QR has no session numbers (legacy) and old record has NULL, it's duplicate
        console.info("Legacy QR and legacy attendance record - duplicate");
        return new Response(JSON.stringify({ ok: true, message: "already_attended", sessions: [] }), {
          status: 200,
          headers: { "Content-Type": "application/json", ...corsHeaders },
        });
      } else if (hasNullSession && sessionNumbers.length > 0) {
        // Old record has NULL session_number (legacy), new QR has session numbers - allow it
        console.info("Legacy attendance record found (session_number=NULL), allowing new session-based attendance");
      }
    }

    // Insert attendance records for each session
    const insertUrl = `${supabaseUrl}/rest/v1/attendances`;
    const markedAt = new Date().toISOString();
    const attendanceRecords: Array<{ session_number: number; success: boolean }> = [];

    for (const sessionNumber of sessionNumbers) {
      const payload: any = {
        course_id,
        week_number,
        session_number: sessionNumber,
        student_id: studentIdForDuplicate,
        marked_at: markedAt,
        method: "qr",
      };
      
      // Add student location if available
      if (student_latitude != null && student_longitude != null) {
        payload.student_latitude = student_latitude;
        payload.student_longitude = student_longitude;
      }

      console.info(`Attempting to insert attendance for session ${sessionNumber}:`, JSON.stringify(payload, null, 2));

      const insertResp = await fetch(insertUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          apikey: apiKey,
          Authorization: `Bearer ${apiKey}`,
          Prefer: "return=representation",
        },
        body: JSON.stringify(payload),
      });
      const insertData = await insertResp.json();
      
      console.info(`Insert response for session ${sessionNumber}: status=${insertResp.status}, data=`, JSON.stringify(insertData, null, 2));
      
      if (!insertResp.ok) {
        // If duplicate error (23505), skip this session
        if ((insertData as any).code === "23505") {
          console.info(`Session ${sessionNumber} already attended (duplicate), skipping`);
          continue;
        }
        console.error(`Error inserting attendance for session ${sessionNumber}:`, JSON.stringify(insertData, null, 2));
        // Continue with other sessions even if one fails
      } else {
        console.info(`Successfully inserted attendance for session ${sessionNumber}`);
        attendanceRecords.push({ session_number: sessionNumber, success: true });
      }
    }

    if (attendanceRecords.length === 0) {
      return new Response(JSON.stringify({ ok: false, error: "no_attendance_inserted", message: "Tüm oturumlar için yoklama zaten alınmış olabilir" }), {
        status: 409,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    return new Response(JSON.stringify({ 
      ok: true, 
      sessions_attended: attendanceRecords.length,
      total_sessions: sessionNumbers.length,
      sessions: attendanceRecords.map(r => r.session_number)
    }), {
      status: 200,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  } catch (err) {
    return new Response(JSON.stringify({ ok: false, error: "unexpected_error" }), {
      status: 500,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  }
});

// Deno Deploy/Edge Function: Validate QR and insert attendance
// Request: { course_id: number, week_number: number, created_at: string, expire_after: number, student_id: string }
// Response: { ok: true }

import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.5";

type ValidateQrInput = {
  course_id?: string | number; // BIGINT number (courses.id is BIGINT, but can come as string from JSON)
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

