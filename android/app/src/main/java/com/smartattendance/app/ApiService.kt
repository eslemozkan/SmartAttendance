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
data class CreateQRRequest(
    @Json(name = "course_id") val courseId: Int,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "expire_after_minutes") val expireAfterMinutes: Int
)

@JsonClass(generateAdapter = true)
data class CreateQRResponse(
    val id: String,
    val qr: QRData
)

@JsonClass(generateAdapter = true)
data class QRData(
    @Json(name = "course_id") val courseId: Int,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after") val expireAfter: Int
)

@JsonClass(generateAdapter = true)
data class ValidateQRRequest(
    @Json(name = "course_id") val courseId: Int,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after") val expireAfter: Int,
    @Json(name = "student_id") val studentId: String
)

@JsonClass(generateAdapter = true)
data class ValidateQRResponse(
    val ok: Boolean? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class WeekWithQR(
    val course_id: Int,
    val week_number: Int,
    val created_at: String,
    val is_active: Boolean
)


@JsonClass(generateAdapter = true)
data class GetWeeksResponse(
    val weeks: List<WeekWithQR>
)

@JsonClass(generateAdapter = true)
data class GetAttendanceResponse(
    val attendance: List<AttendanceRecord>
)

@JsonClass(generateAdapter = true)
data class StudentSignupResult(
    val ok: Boolean,
    val error: String? = null,
    val message: String? = null,
    @Json(name = "user_id") val userId: String? = null
)

class ApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // Supabase Edge Functions base URL (functions subdomain)
    private val functionsBaseUrl = "https://oubvhffqbsxsnbtinzbl.functions.supabase.co"
    // Supabase anon public key (should be moved to secure storage for production)
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
    
    suspend fun checkStudentWhitelist(email: String): Boolean {
        return try {
            val payload = """{"email":"$email"}"""
            val httpRequest = Request.Builder()
                .url("$functionsBaseUrl/check-student-whitelist")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val body = response.body?.string()
            android.util.Log.d("ApiService", "Whitelist Code: ${response.code} Body: $body")
            if (!response.isSuccessful || body.isNullOrEmpty()) return false
            // naive parse: look for ok:true
            body.contains("\"ok\":true")
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "checkStudentWhitelist error: ${e.message}", e)
            false
        }
    }
    
    suspend fun studentSignup(email: String, password: String): StudentSignupResult {
        return try {
            val payload = """{"email":"$email","password":"$password"}"""
            val url = "$functionsBaseUrl/student-signup"
            android.util.Log.d("ApiService", "Signup URL: $url")
            android.util.Log.d("ApiService", "Signup Payload: $payload")
            android.util.Log.d("ApiService", "Anon Key: ${anonKey.take(20)}...")
            
            val httpRequest = Request.Builder()
                .url("$functionsBaseUrl/student-signup")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val body = response.body?.string()
            android.util.Log.d("ApiService", "Signup Response Code: ${response.code}")
            android.util.Log.d("ApiService", "Signup Response Body: $body")
            
            if (response.isSuccessful) {
                val result = moshi.adapter(StudentSignupResult::class.java).fromJson(body)
                result ?: StudentSignupResult(false, "parse_error")
            } else {
                val errorResult = moshi.adapter(StudentSignupResult::class.java).fromJson(body)
                errorResult ?: StudentSignupResult(false, "http_error_${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "studentSignup error: ${e.javaClass.simpleName} - ${e.message}", e)
            android.util.Log.e("ApiService", "Stack trace: ${e.stackTraceToString()}")
            StudentSignupResult(false, "network_error")
        }
    }

    suspend fun teacherSignup(email: String, password: String): StudentSignupResult {
        return try {
            val payload = """{"email":"$email","password":"$password"}"""
            val url = "$functionsBaseUrl/teacher-signup"
            android.util.Log.d("ApiService", "Teacher Signup URL: $url")
            android.util.Log.d("ApiService", "Signup Payload: $payload")
            android.util.Log.d("ApiService", "Anon Key: ${anonKey.take(20)}...")

            val httpRequest = Request.Builder()
                .url(url)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val body = response.body?.string()
            android.util.Log.d("ApiService", "Teacher Signup Response Code: ${response.code}")
            android.util.Log.d("ApiService", "Teacher Signup Response Body: $body")

            if (response.isSuccessful) {
                val result = moshi.adapter(StudentSignupResult::class.java).fromJson(body)
                result ?: StudentSignupResult(false, "parse_error")
            } else {
                val errorResult = moshi.adapter(StudentSignupResult::class.java).fromJson(body)
                errorResult ?: StudentSignupResult(false, "http_error_${response.code}")
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "teacherSignup error: ${e.javaClass.simpleName} - ${e.message}", e)
            android.util.Log.e("ApiService", "Stack trace: ${e.stackTraceToString()}")
            StudentSignupResult(false, "network_error")
        }
    }
    
    suspend fun studentLogin(email: String, password: String): Boolean {
        return try {
            val url = "https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/token?grant_type=password"
            val payload = """{"email":"$email","password":"$password"}"""
            
            android.util.Log.d("ApiService", "Login URL: $url")
            
            val httpRequest = Request.Builder()
                .url(url)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", anonKey)
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            
            val body = response.body?.string()
            android.util.Log.d("ApiService", "Login Response Code: ${response.code}")
            android.util.Log.d("ApiService", "Login Response Body: ${body?.take(100)}...")
            
            response.isSuccessful && body != null && body.contains("access_token")
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "studentLogin error: ${e.message}", e)
            false
        }
    }
    
    suspend fun createQRCode(courseId: Int, weekNumber: Int, expireAfterMinutes: Int): CreateQRResponse? {
        val request = CreateQRRequest(courseId, weekNumber, expireAfterMinutes)
        val json = moshi.adapter(CreateQRRequest::class.java).toJson(request)
        
        val httpRequest = Request.Builder()
            .url("$functionsBaseUrl/create-qr")
            .post(json.toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $anonKey")
            .addHeader("apikey", anonKey)
            .build()
        
        return try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val body = response.body?.string()
            if (response.isSuccessful) {
                moshi.adapter(CreateQRResponse::class.java).fromJson(body)
            } else {
                throw RuntimeException("create-qr failed ${response.code}: ${body ?: "<empty>"}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    suspend fun validateQRCode(qrDataString: String, studentId: String): Boolean? {
        return try {
            android.util.Log.d("ApiService", "QR Data String: $qrDataString")
            android.util.Log.d("ApiService", "Student ID: $studentId")
            
            // Parse QR data string (assuming it's JSON)
            val qrData = moshi.adapter(QRData::class.java).fromJson(qrDataString)
            android.util.Log.d("ApiService", "Parsed QR Data: $qrData")
            
            if (qrData != null) {
                val request = ValidateQRRequest(
                    courseId = qrData.courseId,
                    weekNumber = qrData.weekNumber,
                    createdAt = qrData.createdAt,
                    expireAfter = qrData.expireAfter,
                    studentId = studentId
                )
                
                val json = moshi.adapter(ValidateQRRequest::class.java).toJson(request)
                android.util.Log.d("ApiService", "Request JSON: $json")
                
                val httpRequest = Request.Builder()
                    .url("$functionsBaseUrl/validate-qr")
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("apikey", anonKey)
                    .build()
                
                val response = withContext(Dispatchers.IO) {
                    client.newCall(httpRequest).execute()
                }
                val responseBody = response.body?.string()
                android.util.Log.d("ApiService", "Response Code: ${response.code}")
                android.util.Log.d("ApiService", "Response Body: $responseBody")
                
                if (response.isSuccessful) {
                    val result = moshi.adapter(ValidateQRResponse::class.java).fromJson(responseBody)
                    android.util.Log.d("ApiService", "Parsed Result: $result")
                    result?.ok == true
                } else {
                    android.util.Log.e("ApiService", "HTTP Error: ${response.code} - $responseBody")
                    false
                }
            } else {
                android.util.Log.e("ApiService", "Failed to parse QR data")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Exception in validateQRCode: ${e.message}", e)
            false
        }
    }
    
    suspend fun getWeeksWithQR(courseId: Int): List<WeekWithQR>? {
        return try {
            val httpRequest = Request.Builder()
                .url("$functionsBaseUrl/get-weeks?course_id=$courseId")
                .get()
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val responseBody = response.body?.string()
            android.util.Log.d("ApiService", "GetWeeks Response Code: ${response.code}")
            android.util.Log.d("ApiService", "GetWeeks Response Body: $responseBody")
            
            if (response.isSuccessful) {
                val result = moshi.adapter(GetWeeksResponse::class.java).fromJson(responseBody)
                result?.weeks
            } else {
                android.util.Log.e("ApiService", "GetWeeks HTTP Error: ${response.code} - $responseBody")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Exception in getWeeksWithQR: ${e.message}", e)
            null
        }
    }
    
    suspend fun getAttendanceForWeek(courseId: Int, weekNumber: Int): List<AttendanceRecord>? {
        return try {
            val httpRequest = Request.Builder()
                .url("$functionsBaseUrl/get-attendance?course_id=$courseId&week_number=$weekNumber")
                .get()
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val responseBody = response.body?.string()
            android.util.Log.d("ApiService", "GetAttendance Response Code: ${response.code}")
            android.util.Log.d("ApiService", "GetAttendance Response Body: $responseBody")
            
            if (response.isSuccessful) {
                val result = moshi.adapter(GetAttendanceResponse::class.java).fromJson(responseBody)
                result?.attendance
            } else {
                android.util.Log.e("ApiService", "GetAttendance HTTP Error: ${response.code} - $responseBody")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Exception in getAttendanceForWeek: ${e.message}", e)
            null
        }
    }
}
