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
    @Json(name = "course_id") val courseId: Int,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after_minutes") val expireAfterMinutes: Int,
    @Json(name = "is_active") val isActive: Boolean
)


@JsonClass(generateAdapter = true)
data class StudentProfile(
    @Json(name = "full_name") val fullName: String
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
    
    suspend fun getWeeksWithQR(courseId: Int): List<WeekWithQR>? {
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
                        course_id = (map["course_id"] as Double).toInt(),
                        week_number = (map["week_number"] as Double).toInt(),
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
    
    suspend fun getAttendanceForWeek(courseId: Int, weekNumber: Int): List<AttendanceRecord>? {
        return try {
            val httpRequest = Request.Builder()
                .url("$supabaseUrl/rest/v1/attendances?course_id=eq.$courseId&week_number=eq.$weekNumber&select=student_id,marked_at,method,profiles!attendances_student_id_fkey(full_name)&order=marked_at.asc")
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
                attendances?.map { attendanceMap ->
                    val map = attendanceMap as Map<String, Any>
                    val profilesMap = map["profiles"] as? Map<String, Any>
                    val studentProfile = profilesMap?.let { 
                        StudentProfile(fullName = it["full_name"] as String)
                    }
                    AttendanceRecord(
                        studentId = map["student_id"] as String,
                        markedAt = map["marked_at"] as String,
                        method = map["method"] as String,
                        profiles = studentProfile
                    )
                }
            } else {
                android.util.Log.e("SupabaseService", "GetAttendance HTTP Error: ${response.code} - $responseBody")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("SupabaseService", "Exception in getAttendanceForWeek: ${e.message}", e)
            null
        }
    }
}
