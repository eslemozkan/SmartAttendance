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
    // Hardcoded for testing - same as other functions
    const supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co";
    const supabaseServiceRoleKey = "YOUR_SERVICE_ROLE_KEY";
    
    const supabaseClient = createClient(
      supabaseUrl,
      supabaseServiceRoleKey,
      {
        global: {
          headers: { Authorization: req.headers.get('Authorization')! },
        },
      }
    );

    const url = new URL(req.url);
    const courseId = url.searchParams.get('course_id');
    const weekNumber = url.searchParams.get('week_number');

    if (!courseId || !weekNumber) {
      return new Response(
        JSON.stringify({ error: 'course_id and week_number parameters are required' }),
        { 
          status: 400, 
          headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        }
      );
    }

    // Get attendance records for the specified course and week
    const { data: attendance, error } = await supabaseClient
      .from('attendances')
      .select(`
        student_id,
        marked_at,
        method,
        profiles!attendances_student_id_fkey (
          full_name
        )
      `)
      .eq('course_id', parseInt(courseId))
      .eq('week_number', parseInt(weekNumber))
      .order('marked_at', { ascending: true });

    if (error) {
      console.error('Error fetching attendance:', error);
      return new Response(
        JSON.stringify({ error: 'Failed to fetch attendance' }),
        { 
          status: 500, 
          headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
        }
      );
    }

    // Transform the data to match the expected format
    const transformedAttendance = (attendance || []).map(record => ({
      student_id: record.student_id,
      student_name: record.profiles?.full_name || 'Unknown Student',
      marked_at: record.marked_at,
      method: record.method
    }));

    return new Response(
      JSON.stringify({ attendance: transformedAttendance }),
      { 
        status: 200, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    );

  } catch (error) {
    console.error('Error in get-attendance function:', error);
    return new Response(
      JSON.stringify({ error: 'Internal server error' }),
      { 
        status: 500, 
        headers: { ...corsHeaders, 'Content-Type': 'application/json' } 
      }
    );
  }
});
