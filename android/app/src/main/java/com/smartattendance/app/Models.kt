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

data class StudentProfile(
    val fullName: String
)

data class AttendanceRecord(
    val studentId: String,
    val markedAt: String,
    val method: String,
    val profiles: StudentProfile?
) {
    // Legacy constructor for backward compatibility
    constructor(
        student_id: String,
        student_name: String,
        marked_at: String,
        method: String
    ) : this(
        studentId = student_id,
        markedAt = marked_at,
        method = method,
        profiles = StudentProfile(fullName = student_name)
    )
}
