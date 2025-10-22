package com.smartattendance.app

data class Course(
    val id: Int,
    val name: String,
    val code: String,
    val schedule: String
)

data class Week(
    val id: Int,
    val name: String
)

data class AttendanceRecord(
    val student_id: String,
    val student_name: String,
    val marked_at: String,
    val method: String
) {
    // Constructor for SupabaseService compatibility
    constructor(
        studentId: String,
        markedAt: String,
        method: String,
        profiles: com.smartattendance.app.StudentProfile?
    ) : this(
        student_id = studentId,
        student_name = profiles?.fullName ?: "Unknown Student",
        marked_at = markedAt,
        method = method
    )
}
