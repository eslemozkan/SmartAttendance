package com.smartattendance.app

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Course(
    val id: Int, // Hash-based ID for UI (spinner index)
    val uuid: String? = null, // UUID from database (for API calls)
    val name: String,
    val code: String,
    val schedule: String,
    val weeklyHours: Int = 2 // Haftalık ders saati sayısı (default: 2)
)

data class Week(
    val id: Int,
    val name: String
)

data class StudentProfile(
    val fullName: String,
    val email: String? = null
)

data class AttendanceRecord(
    val studentId: String,
    val markedAt: String,
    val method: String,
    val profiles: StudentProfile?,
    val hasAttendance: Boolean = true // Yoklamaya katıldı mı?
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
        profiles = StudentProfile(fullName = student_name),
        hasAttendance = true
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
    @Json(name = "attendance_time") val attendanceTime: String?,
    @Json(name = "total_sessions") val totalSessions: Int = 0, // Bu hafta için toplam session sayısı
    @Json(name = "attended_sessions") val attendedSessions: List<Int> = emptyList(), // Katıldığı session numaraları
    @Json(name = "weekly_hours") val weeklyHours: Int = 0 // Dersin haftalık saati
)

@JsonClass(generateAdapter = true)
data class StudentSessionAttendance(
    @Json(name = "session_number") val sessionNumber: Int,
    @Json(name = "has_attendance") val hasAttendance: Boolean,
    @Json(name = "attendance_time") val attendanceTime: String?
)

// Haftalık ders oturumları için modeller
@JsonClass(generateAdapter = true)
data class WeeklySession(
    @Json(name = "session_number") val sessionNumber: Int,
    @Json(name = "is_completed") val isCompleted: Boolean,
    @Json(name = "session_id") val sessionId: Long? = null,
    @Json(name = "qr_code_id") val qrCodeId: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class GetWeeklySessionsResponse(
    @Json(name = "course_id") val courseId: Long,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "weekly_hours") val weeklyHours: Int,
    @Json(name = "available_sessions") val availableSessions: List<WeeklySession>,
    @Json(name = "all_sessions") val allSessions: List<WeeklySession>? = null, // Tüm session'lar (tamamlanmış + tamamlanmamış)
    @Json(name = "total_sessions") val totalSessions: Int,
    @Json(name = "completed_sessions") val completedSessions: Int
)
