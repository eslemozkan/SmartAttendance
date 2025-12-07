package com.smartattendance.app

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Course(
    val id: Int, // Hash-based ID for UI (spinner index)
    val uuid: String? = null, // UUID from database (for API calls)
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

// Öğrenci yoklama durumu için modeller
@JsonClass(generateAdapter = true)
data class StudentCourseWithWeeks(
    @Json(name = "course_id") val courseId: Long,
    @Json(name = "course_name") val courseName: String,
    @Json(name = "course_code") val courseCode: String?,
    @Json(name = "weeks") val weeks: List<StudentWeekAttendance>
)

@JsonClass(generateAdapter = true)
data class StudentWeekAttendance(
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "qr_created_at") val qrCreatedAt: String?,
    @Json(name = "has_attendance") val hasAttendance: Boolean,
    @Json(name = "attendance_time") val attendanceTime: String?
)
