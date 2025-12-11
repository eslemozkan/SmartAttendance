package com.smartattendance.app

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SupabaseResponse<T>(
    val data: List<T>? = null,
    val error: SupabaseError? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseError(
    val message: String,
    val details: String? = null,
    val hint: String? = null,
    val code: String? = null
)

@JsonClass(generateAdapter = true)
data class QRCodeRecord(
    val id: String,
    @Json(name = "course_id") val courseId: Long,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after_minutes") val expireAfterMinutes: Int,
    @Json(name = "is_active") val isActive: Boolean
)


class SupabaseService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // Supabase REST API base URL
    private val supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co"
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
    
    suspend fun getWeeksWithQR(courseId: Long): List<WeekWithQR>? {
        return try {
            val httpRequest = Request.Builder()
                .url("$supabaseUrl/rest/v1/qr_codes?course_id=eq.$courseId&is_active=eq.true&select=course_id,week_number,created_at,is_active&order=week_number.asc")
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val responseBody = response.body?.string()
            android.util.Log.d("SupabaseService", "GetWeeks Response Code: ${response.code}")
            android.util.Log.d("SupabaseService", "GetWeeks Response Body: $responseBody")
            
            if (response.isSuccessful) {
                val qrCodes = moshi.adapter(List::class.java).fromJson(responseBody)
                qrCodes?.map { qrCodeMap ->
                    val map = qrCodeMap as Map<String, Any>
                    WeekWithQR(
                        course_id = (map["course_id"] as Number).toLong(),
                        week_number = (map["week_number"] as Number).toInt(),
                        created_at = map["created_at"] as String,
                        is_active = map["is_active"] as Boolean
                    )
                }
            } else {
                android.util.Log.e("SupabaseService", "GetWeeks HTTP Error: ${response.code} - $responseBody")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Exception in getWeeksWithQR: ${e.message}", e)
            null
        }
    }
    
    suspend fun getAttendanceForWeek(courseId: String, weekNumber: Int): List<AttendanceRecord>? {
        return try {
            val httpRequest = Request.Builder()
                .url("$supabaseUrl/rest/v1/attendances?course_id=eq.$courseId&week_number=eq.$weekNumber&select=student_id,marked_at,method,profiles!attendances_student_id_fkey(full_name,email)&order=marked_at.asc")
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val responseBody = response.body?.string()
            android.util.Log.d("SupabaseService", "GetAttendance Response Code: ${response.code}")
            android.util.Log.d("SupabaseService", "GetAttendance Response Body: $responseBody")
            
            if (response.isSuccessful) {
                val attendances = moshi.adapter(List::class.java).fromJson(responseBody)
                val result = attendances?.mapNotNull { attendanceMap ->
                    try {
                        val map = attendanceMap as Map<String, Any>
                        val studentIdRaw = map["student_id"]
                        val studentId = when (studentIdRaw) {
                            is String -> studentIdRaw
                            is Number -> studentIdRaw.toString()
                            else -> studentIdRaw?.toString() ?: ""
                        }
                        val profilesMap = map["profiles"] as? Map<String, Any>
                        val studentProfile = profilesMap?.let { 
                            StudentProfile(
                                fullName = it["full_name"] as? String ?: "",
                                email = it["email"] as? String
                            )
                        }
                        android.util.Log.d("SupabaseService", "Parsed attendance: $studentId - ${studentProfile?.fullName}")
                        AttendanceRecord(
                            studentId = studentId,
                            markedAt = map["marked_at"] as? String ?: "",
                            method = map["method"] as? String ?: "",
                            profiles = studentProfile,
                            hasAttendance = true
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("SupabaseService", "Error parsing attendance: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                android.util.Log.d("SupabaseService", "Returning ${result.size} attendance records")
                return result
            } else {
                android.util.Log.e("SupabaseService", "GetAttendance HTTP Error: ${response.code} - $responseBody")
                return null
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Exception in getAttendanceForWeek: ${e.message}", e)
            null
        }
    }
    
    // Derse atanmış sınıflardaki tüm öğrencileri getir
    suspend fun getAllStudentsForCourse(courseId: String): List<StudentRecord>? {
        return try {
            // 1. Önce course_class_assignments'den class_id'leri al
            val assignmentsUrl = "$supabaseUrl/rest/v1/course_class_assignments?course_id=eq.$courseId&select=class_id"
            val assignmentsRequest = Request.Builder()
                .url(assignmentsUrl)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val assignmentsResponse = withContext(Dispatchers.IO) {
                client.newCall(assignmentsRequest).execute()
            }
            val assignmentsBody = assignmentsResponse.body?.string()
            android.util.Log.d("SupabaseService", "GetAllStudents - Assignments Response Code: ${assignmentsResponse.code}")
            android.util.Log.d("SupabaseService", "GetAllStudents - Assignments Response Body: $assignmentsBody")
            
            if (!assignmentsResponse.isSuccessful || assignmentsBody.isNullOrEmpty() || assignmentsBody == "[]") {
                android.util.Log.w("SupabaseService", "No class assignments found for course $courseId")
                return emptyList()
            }
            
            val assignments = moshi.adapter(List::class.java).fromJson(assignmentsBody) as? List<Map<String, Any>>
            val classIds = assignments?.mapNotNull { item ->
                val v = item["class_id"]
                when (v) {
                    is String -> v
                    is Number -> v.toString()
                    else -> v?.toString()
                }
            }?.filter { it.isNotBlank() }?.distinct() ?: emptyList()
            
            if (classIds.isEmpty()) {
                android.util.Log.w("SupabaseService", "No class IDs found for course $courseId")
                return emptyList()
            }
            
            android.util.Log.d("SupabaseService", "Found ${classIds.size} classes for course $courseId: $classIds")
            
            // 2. O sınıflardaki tüm öğrencileri al
            // Supabase REST API'de IN operatörü için virgülle ayrılmış değerler kullanıyoruz
            val classIdsParam = classIds.joinToString(",")
            val studentsUrl = "$supabaseUrl/rest/v1/students?class_id=in.($classIdsParam)&select=id,email,full_name&order=full_name.asc"
            
            val studentsRequest = Request.Builder()
                .url(studentsUrl)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val studentsResponse = withContext(Dispatchers.IO) {
                client.newCall(studentsRequest).execute()
            }
            val studentsBody = studentsResponse.body?.string()
            android.util.Log.d("SupabaseService", "GetAllStudents - Students Response Code: ${studentsResponse.code}")
            android.util.Log.d("SupabaseService", "GetAllStudents - Students Response Body: $studentsBody")
            
            if (studentsResponse.isSuccessful && !studentsBody.isNullOrEmpty() && studentsBody != "[]") {
                val students = moshi.adapter(List::class.java).fromJson(studentsBody) as? List<Map<String, Any>>
                val result = students?.mapNotNull { studentMap ->
                    try {
                        val id = studentMap["id"]
                        val studentId = when (id) {
                            is String -> id
                            is Number -> id.toString()
                            else -> null
                        }
                        if (studentId == null) {
                            android.util.Log.w("SupabaseService", "Invalid student ID: $id")
                            return@mapNotNull null
                        }
                        val fullName = studentMap["full_name"] as? String ?: ""
                        val email = studentMap["email"] as? String ?: ""
                        android.util.Log.d("SupabaseService", "Parsed student: $studentId - $fullName")
                        StudentRecord(
                            studentId = studentId,
                            email = email,
                            profiles = StudentProfile(fullName = fullName, email = email),
                            fullName = fullName
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("SupabaseService", "Error parsing student: ${e.message}", e)
                        null
                    }
                } ?: emptyList()
                android.util.Log.d("SupabaseService", "Returning ${result.size} students")
                return result
            } else {
                android.util.Log.e("SupabaseService", "GetAllStudents - Students HTTP Error: ${studentsResponse.code} - $studentsBody")
                return emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Exception in getAllStudentsForCourse: ${e.message}", e)
            null
        }
    }
}

// Derse kayıtlı öğrenci kaydı
data class StudentRecord(
    val studentId: String,
    val email: String,
    val profiles: StudentProfile?,
    val fullName: String = profiles?.fullName ?: "",
    val hasAttendance: Boolean = false,
    val attendanceTime: String? = null,
    val method: String? = null
)
