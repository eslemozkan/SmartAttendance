import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from 'jsr:@supabase/supabase-js@2';

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
};

Deno.serve(async (req: Request) => {
  // Handle CORS preflight requests
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders });
  }

  try {
    // Hardcoded URL (same as other functions)
    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";

    // Prefer service role key from env, fallback to Authorization/apikey headers
    const headerAuth = req.headers.get("authorization") || req.headers.get("Authorization") || "";
    const bearer = headerAuth.startsWith("Bearer ") ? headerAuth.substring(7) : "";
    const headerApiKey = req.headers.get("apikey") || req.headers.get("x-apikey") || "";
    const serviceKeyEnv = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") || "";
    const supabaseServiceRoleKey = serviceKeyEnv || bearer || headerApiKey;

    if (!supabaseServiceRoleKey) {
      console.error("Missing Supabase service role key for get-weeks");
      return new Response(
        JSON.stringify({ error: "Missing Supabase service role key" }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const supabaseClient = createClient(
      supabaseUrl,
      supabaseServiceRoleKey,
      {
        global: {
          headers: { Authorization: req.headers.get("Authorization") ?? "" },
        },
      }
    );

    const url = new URL(req.url);
    const courseId = url.searchParams.get('course_id');
    const courseIdNum = Number(courseId);

    if (!courseId || Number.isNaN(courseIdNum)) {
      return new Response(
        JSON.stringify({ error: 'course_id parameter is required and must be a number' }),
        { 
          status: 400, 
          headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        }
      );
    }

    // Get weeks with QR codes for the specified course
    const { data: qrCodes, error } = await supabaseClient
      .from('qr_codes')
      .select('course_id, week_number, created_at, is_active')
      .eq('course_id', courseIdNum)
      .eq('is_active', true)
      .order('week_number', { ascending: true });

    if (error) {
      console.error('Error fetching QR codes:', error);
      return new Response(
        JSON.stringify({ error: 'Failed to fetch weeks' }),
        { 
          status: 500, 
          headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        }
      );
    }

    console.log(`get-weeks course_id=${courseIdNum} rows=${qrCodes?.length ?? 0}`);

    return new Response(
      JSON.stringify({ weeks: qrCodes || [] }),
      { 
        status: 200, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    );

  } catch (error) {
    console.error('Error in get-weeks function:', error);
    return new Response(
      JSON.stringify({ error: 'Internal server error' }),
      { 
        status: 500, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    );
  }
});
