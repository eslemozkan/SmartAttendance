// Get available weekly sessions for a course and week
// Returns sessions that haven't been completed yet (qr_code_id IS NULL)

import { createClient } from "jsr:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed" }), {
      status: 405,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  }

  try {
    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";
    const headerAuth = req.headers.get("authorization") || req.headers.get("Authorization") || "";
    const bearer = headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : "";
    const headerApiKey = req.headers.get("apikey") || req.headers.get("x-apikey") || "";
    const serviceKeyEnv = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
    const apiKey = serviceKeyEnv || bearer || headerApiKey;

    if (!apiKey) {
      return new Response(JSON.stringify({ error: "api_key_missing" }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const { course_id, week_number } = await req.json();

    if (!course_id || !week_number) {
      return new Response(JSON.stringify({ error: "course_id and week_number required" }), {
        status: 400,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const supabase = createClient(supabaseUrl, apiKey, {
      auth: { persistSession: false },
      db: { schema: "public" },
    });

    // Get course weekly_hours
    const { data: course, error: courseError } = await supabase
      .from("courses")
      .select("id, name, code, weekly_hours")
      .eq("id", course_id)
      .single();

    if (courseError || !course) {
      return new Response(JSON.stringify({ error: "course_not_found" }), {
        status: 404,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    const weeklyHours = course.weekly_hours || 2; // Default to 2 if not set

    // Get existing sessions for this course and week
    const { data: existingSessions, error: sessionsError } = await supabase
      .from("course_weekly_sessions")
      .select("id, session_number, qr_code_id, created_at")
      .eq("course_id", course_id)
      .eq("week_number", week_number)
      .order("session_number", { ascending: true });

    if (sessionsError) {
      console.error("Error fetching sessions:", sessionsError);
      return new Response(JSON.stringify({ error: "database_error", details: sessionsError.message }), {
        status: 500,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      });
    }

    // Create array of all possible sessions (1 to weekly_hours)
    const allSessions = Array.from({ length: weeklyHours }, (_, i) => i + 1);

    // Map existing sessions
    const existingSessionsMap = new Map(
      (existingSessions || []).map((s) => [s.session_number, s])
    );

    // Build response: all sessions (both available and completed)
    const allSessionsData = allSessions.map((sessionNumber) => {
      const existing = existingSessionsMap.get(sessionNumber);
      return {
        session_number: sessionNumber,
        is_completed: existing?.qr_code_id != null,
        session_id: existing?.id || null,
        qr_code_id: existing?.qr_code_id || null,
        created_at: existing?.created_at || null,
      };
    });
    
    // Separate available and completed sessions
    const availableSessions = allSessionsData.filter((s) => !s.is_completed);
    const completedSessions = allSessionsData.filter((s) => s.is_completed);

    return new Response(
      JSON.stringify({
        course_id: Number(course_id),
        week_number: Number(week_number),
        weekly_hours: weeklyHours,
        available_sessions: availableSessions, // Not completed sessions (for selection)
        all_sessions: allSessionsData, // All sessions (for display)
        total_sessions: weeklyHours,
        completed_sessions: completedSessions.length,
      }),
      {
        status: 200,
        headers: { "Content-Type": "application/json", ...corsHeaders },
      }
    );
  } catch (error) {
    console.error("Unexpected error:", error);
    return new Response(JSON.stringify({ error: "unexpected_error", details: String(error) }), {
      status: 500,
      headers: { "Content-Type": "application/json", ...corsHeaders },
    });
  }
});



