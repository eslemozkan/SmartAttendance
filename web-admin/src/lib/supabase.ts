import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://oubvhffqbsxsnbtinzbl.supabase.co'
const supabaseAnonKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI'

export const supabase = createClient(supabaseUrl, supabaseAnonKey)

// Types
export interface Admin {
  id: string
  email: string
  full_name: string
  role: 'super_admin' | 'admin'
  department_id?: string
  created_at: string
}

export interface Department {
  id: string
  name: string
  code: string
  created_at: string
}

export interface Class {
  id: string
  name: string
  department_id: string
  academic_year: string
  created_at: string
}

export interface Student {
  id: string
  student_number: string
  full_name: string
  email: string
  phone?: string
  class_id: string
  department_id: string
  is_active: boolean
  created_at: string
}

export interface Teacher {
  id: string
  full_name: string
  role: string
  department_id?: string
  employee_id?: string
  phone?: string
  is_active: boolean
}

export interface CourseClassAssignment {
  id: string
  course_id: string
  class_id: string
  teacher_id: string
  academic_year: string
  semester: string
  created_at: string
}

export interface AttendanceRecord {
  id: string
  student_id: string
  student_number?: string
  course_id: string
  week_number: number
  qr_code_id: string
  marked_at: string
  method: string
  class_id?: string
  profiles?: {
    full_name: string
  }
}