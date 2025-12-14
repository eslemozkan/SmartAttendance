package com.smartattendance.app

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class CreateQRRequest(
    @Json(name = "course_id") val courseId: Long, // BIGINT number (courses.id is BIGINT)
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "expire_after_minutes") val expireAfterMinutes: Int,
    @Json(name = "teacher_latitude") val teacherLatitude: Double? = null,
    @Json(name = "teacher_longitude") val teacherLongitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class CreateQRResponse(
    val id: String,
    val qr: QRData
)

@JsonClass(generateAdapter = true)
data class QRData(
    @Json(name = "course_id") val courseId: Long, // BIGINT number
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after") val expireAfter: Int,
    @Json(name = "teacher_latitude") val teacherLatitude: Double? = null,
    @Json(name = "teacher_longitude") val teacherLongitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class ValidateQRRequest(
    @Json(name = "course_id") val courseId: Long, // BIGINT number (courses.id is BIGINT)
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after") val expireAfter: Int,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "student_email") val studentEmail: String? = null,
    @Json(name = "student_latitude") val studentLatitude: Double? = null,
    @Json(name = "student_longitude") val studentLongitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class ValidateQRResponse(
    val ok: Boolean? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class WeekWithQR(
    val course_id: Long,
    val week_number: Int,
    val created_at: String,
    val is_active: Boolean
)

@JsonClass(generateAdapter = true)
data class CourseWeek(
    val id: Int,
    @Json(name = "course_id") val courseId: Long,
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "has_qr") val hasQr: Boolean,
    val locked: Boolean
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
data class TeacherAssignedCourse(
    @Json(name = "assignment_id") val assignmentId: String?,
    @Json(name = "teacher_email") val teacherEmail: String?,
    @Json(name = "course_id") val courseId: Long?, // BIGINT number (courses.id is BIGINT)
    @Json(name = "course_name") val courseName: String?,
    @Json(name = "course_code") val courseCode: String?
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
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    // Supabase Edge Functions base URL (functions subdomain)
    private val functionsBaseUrl = "https://oubvhffqbsxsnbtinzbl.functions.supabase.co"
    // Supabase anon public key (should be moved to secure storage for production)
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
    private val restBaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co/rest/v1"
    
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

    suspend fun getAssignedCoursesForTeacher(email: String): List<TeacherAssignedCourse>? {
        return try {
            val encoded = java.net.URLEncoder.encode(email, "UTF-8")
            
            // First try the view
            var url = "$restBaseUrl/teacher_assigned_courses?select=assignment_id,teacher_email,course_id,course_name,course_code&teacher_email=eq.$encoded"
            
            android.util.Log.d("ApiService", "Loading courses for teacher: $email")
            android.util.Log.d("ApiService", "Trying view URL: $url")
            
            var httpRequest = Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            var response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            
            var body = response.body?.string()
            android.util.Log.d("ApiService", "View Response Code: ${response.code}")
            android.util.Log.d("ApiService", "View Response Body: ${body?.take(500)}")
            
            // If view fails or returns empty, try direct table join
            if (!response.isSuccessful || body.isNullOrEmpty() || body == "[]") {
                android.util.Log.w("ApiService", "View failed or empty, trying direct table query")
                
                // Get teacher profile ID first
                val profileUrl = "$restBaseUrl/profiles?select=id&email=eq.$encoded"
                val profileRequest = Request.Builder()
                    .url(profileUrl)
                    .get()
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .build()
                
                val profileResponse = withContext(Dispatchers.IO) {
                    client.newCall(profileRequest).execute()
                }
                val profileBody = profileResponse.body?.string()
                
                if (profileResponse.isSuccessful && !profileBody.isNullOrEmpty() && profileBody != "[]") {
                    val profileData = moshi.adapter(List::class.java).fromJson(profileBody) as? List<Map<String, Any>>
                    val teacherId = profileData?.firstOrNull()?.get("id")?.toString()
                    
                    if (teacherId != null) {
                        android.util.Log.d("ApiService", "Found teacher_id: $teacherId")
                        
                        // Query teacher_courses with courses join
                        url = "$restBaseUrl/teacher_courses?select=id,course_id,courses(id,name,code)&teacher_id=eq.$teacherId"
                        android.util.Log.d("ApiService", "Direct table URL: $url")
                        
                        httpRequest = Request.Builder()
                            .url(url)
                            .get()
                            .addHeader("apikey", anonKey)
                            .addHeader("Authorization", "Bearer $anonKey")
                            .build()
                        
                        response = withContext(Dispatchers.IO) {
                            client.newCall(httpRequest).execute()
                        }
                        
                        body = response.body?.string()
                        android.util.Log.d("ApiService", "Direct table Response Code: ${response.code}")
                        android.util.Log.d("ApiService", "Direct table Response Body: ${body?.take(500)}")
                        
                        if (response.isSuccessful && !body.isNullOrEmpty() && body != "[]") {
                            // Parse nested structure
                            val teacherCoursesData = moshi.adapter(List::class.java).fromJson(body) as? List<Map<String, Any>>
                            val result = teacherCoursesData?.mapNotNull { tc ->
                                val assignmentId = tc["id"]?.toString()
                                val courseIdValue = tc["course_id"]
                                val courseId = when (courseIdValue) {
                                    is Number -> courseIdValue.toLong()
                                    else -> null
                                }
                                val coursesMap = tc["courses"] as? Map<String, Any>
                                val courseName = coursesMap?.get("name") as? String
                                val courseCode = coursesMap?.get("code") as? String
                                
                                if (courseId != null && courseName != null) {
                                    TeacherAssignedCourse(
                                        assignmentId = assignmentId,
                                        teacherEmail = email,
                                        courseId = courseId,
                                        courseName = courseName,
                                        courseCode = courseCode
                                    )
                                } else null
                            } ?: emptyList()
                            
                            android.util.Log.d("ApiService", "Parsed ${result.size} courses from direct table")
                            return result
                        }
                    }
                }
            }
            
            // Try parsing view response
            if (response.isSuccessful && !body.isNullOrEmpty() && body != "[]") {
                val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, TeacherAssignedCourse::class.java)
                val result = moshi.adapter<List<TeacherAssignedCourse>>(type).fromJson(body)
                android.util.Log.d("ApiService", "Parsed ${result?.size ?: 0} courses from view")
                return result
            }
            
            android.util.Log.w("ApiService", "No courses found for teacher: $email")
            emptyList()
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("ApiService", "getAssignedCoursesForTeacher network error: ${e.message}", e)
            throw RuntimeException("İnternet bağlantısı yok. Lütfen WiFi veya mobil veri bağlantınızı kontrol edin.", e)
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("ApiService", "getAssignedCoursesForTeacher timeout: ${e.message}", e)
            throw RuntimeException("Bağlantı zaman aşımına uğradı. Lütfen tekrar deneyin.", e)
        } catch (e: java.io.IOException) {
            android.util.Log.e("ApiService", "getAssignedCoursesForTeacher IO error: ${e.message}", e)
            throw RuntimeException("Ağ hatası: ${e.message}. İnternet bağlantınızı kontrol edin.", e)
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "getAssignedCoursesForTeacher error: ${e.javaClass.simpleName} - ${e.message}", e)
            android.util.Log.e("ApiService", "Stack trace: ${e.stackTraceToString()}")
            throw RuntimeException("Dersler yüklenirken hata oluştu: ${e.message}", e)
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
    
    suspend fun resetPassword(email: String): ResetPasswordResult {
        return try {
            android.util.Log.d("ApiService", "=== Password Reset Request ===")
            android.util.Log.d("ApiService", "Email: $email")
            
            // Use Edge Function instead of direct Supabase Auth API
            val url = "$functionsBaseUrl/reset-password"
            val payload = """{"email":"$email"}"""
            
            android.util.Log.d("ApiService", "URL: $url")
            android.util.Log.d("ApiService", "Payload: $payload")
            
            val request = Request.Builder()
                .url(url)
                .post(payload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            
            val body = response.body?.string()
            android.util.Log.d("ApiService", "Response Code: ${response.code}")
            android.util.Log.d("ApiService", "Response Body: $body")
            
            if (response.isSuccessful && body != null) {
                try {
                    // Parse JSON response from edge function
                    val jsonObject = org.json.JSONObject(body)
                    val ok = jsonObject.optBoolean("ok", false)
                    val message = jsonObject.optString("message", "Şifre sıfırlama isteği işlendi.")
                    val error = jsonObject.optString("error", null)
                    val resetLink = jsonObject.optString("resetLink", null)
                    val emailSent = jsonObject.optBoolean("emailSent", false)
                    
                    android.util.Log.d("ApiService", "Password reset response parsed: ok=$ok, emailSent=$emailSent, error=$error")
                    
                    if (!ok && error != null) {
                        // Edge function returned an error
                        android.util.Log.e("ApiService", "Edge function error: $error")
                        ResetPasswordResult(
                            false,
                            "Şifre sıfırlama hatası: $error",
                            null,
                            email
                        )
                    } else {
                        ResetPasswordResult(
                            ok,
                            message,
                            if (resetLink.isNullOrEmpty()) null else resetLink,
                            email
                        )
                    }
                } catch (e: org.json.JSONException) {
                    android.util.Log.e("ApiService", "Failed to parse reset password response: ${e.message}")
                    android.util.Log.e("ApiService", "Raw response body: $body")
                    ResetPasswordResult(
                        false,
                        "Sunucu yanıtı işlenemedi. Lütfen daha sonra tekrar deneyin.",
                        null,
                        email
                    )
                }
            } else {
                android.util.Log.w("ApiService", "Password reset request failed: ${response.code} - $body")
                
                // Try to parse error message from response body (for rate limit, etc.)
                var errorMessage: String? = null
                if (body != null) {
                    try {
                        val jsonObject = org.json.JSONObject(body)
                        errorMessage = jsonObject.optString("message", null)
                    } catch (e: Exception) {
                        // Ignore parse errors
                    }
                }
                
                // Check if it's a 404 (function not deployed), 429 (rate limit), or 500 (server error)
                val finalErrorMessage = errorMessage ?: when (response.code) {
                    404 -> "Şifre sıfırlama servisi bulunamadı. Lütfen sistem yöneticisi ile iletişime geçin."
                    429 -> "Güvenlik nedeniyle, şifre sıfırlama isteği çok sık gönderilemez. Lütfen 60 saniye bekleyip tekrar deneyin."
                    500 -> "Sunucu hatası oluştu. Lütfen daha sonra tekrar deneyin."
                    else -> "Şifre sıfırlama isteği gönderilemedi. Hata kodu: ${response.code}. Lütfen daha sonra tekrar deneyin."
                }
                
                ResetPasswordResult(
                    false,
                    finalErrorMessage,
                    null,
                    email
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Password reset exception: ${e.message}", e)
            android.util.Log.e("ApiService", "Exception type: ${e.javaClass.simpleName}")
            ResetPasswordResult(false, "Bağlantı hatası: ${e.message ?: "Bilinmeyen hata"}")
        }
    }
    
    data class ResetPasswordResult(val success: Boolean, val message: String, val resetLink: String? = null, val email: String? = null)
    
    suspend fun createQRCode(courseId: Long, weekNumber: Int, expireAfterMinutes: Int, teacherLatitude: Double? = null, teacherLongitude: Double? = null): CreateQRResponse? {
        return try {
            val request = CreateQRRequest(courseId, weekNumber, expireAfterMinutes, teacherLatitude, teacherLongitude)
            val json = moshi.adapter(CreateQRRequest::class.java).toJson(request)
            val url = "$functionsBaseUrl/create-qr"
            
            android.util.Log.d("ApiService", "CreateQR URL: $url")
            android.util.Log.d("ApiService", "CreateQR Request: $json")
            
            val httpRequest = Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .build()
            
            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            
            val body = response.body?.string()
            android.util.Log.d("ApiService", "CreateQR Response Code: ${response.code}")
            android.util.Log.d("ApiService", "CreateQR Response Body: $body")
            
            if (response.isSuccessful) {
                val result = moshi.adapter(CreateQRResponse::class.java).fromJson(body)
                if (result == null) {
                    android.util.Log.e("ApiService", "Failed to parse CreateQR response")
                    throw RuntimeException("Failed to parse response")
                }
                result
            } else {
                android.util.Log.e("ApiService", "CreateQR HTTP Error: ${response.code} - $body")
                throw RuntimeException("create-qr failed ${response.code}: ${body ?: "<empty>"}")
            }
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("ApiService", "CreateQR Network Error: Unknown host - ${e.message}", e)
            throw RuntimeException("Failed to connect to server. Check your internet connection.")
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("ApiService", "CreateQR Timeout Error: ${e.message}", e)
            throw RuntimeException("Request timeout. The server may be slow or unreachable.")
        } catch (e: java.io.IOException) {
            android.util.Log.e("ApiService", "CreateQR IO Error: ${e.message}", e)
            throw RuntimeException("Network error: ${e.message}")
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "CreateQR Error: ${e.javaClass.simpleName} - ${e.message}", e)
            throw e
        }
    }
    
    suspend fun validateQRCode(qrDataString: String, studentEmail: String, studentLatitude: Double? = null, studentLongitude: Double? = null): Boolean? {
        return try {
            android.util.Log.d("ApiService", "QR Data String: $qrDataString")
            android.util.Log.d("ApiService", "Student Email: $studentEmail")
            android.util.Log.d("ApiService", "Student Location: lat=$studentLatitude, lon=$studentLongitude")
            
            // Parse QR data string (assuming it's JSON)
            val qrData = moshi.adapter(QRData::class.java).fromJson(qrDataString)
            android.util.Log.d("ApiService", "Parsed QR Data: $qrData")
            
            // Konum kontrolü: Eğer hem hocanın hem öğrencinin konumu varsa, mesafe kontrolü yap
            if (qrData != null && qrData.teacherLatitude != null && qrData.teacherLongitude != null 
                && studentLatitude != null && studentLongitude != null) {
                val distance = LocationHelper.calculateDistance(
                    qrData.teacherLatitude,
                    qrData.teacherLongitude,
                    studentLatitude,
                    studentLongitude
                )
                android.util.Log.d("ApiService", "Distance from teacher: ${distance}m")
                
                // Mesafe threshold: 30 metre (sınıf/bina içi için uygun)
                val maxDistance = 30.0 // metre
                if (distance > maxDistance) {
                    android.util.Log.w("ApiService", "Student too far from teacher: ${distance}m > ${maxDistance}m")
                    return false
                }
                android.util.Log.d("ApiService", "Location check passed: ${distance}m <= ${maxDistance}m")
            } else {
                android.util.Log.w("ApiService", "Location check skipped: teacher location=${qrData?.teacherLatitude != null && qrData?.teacherLongitude != null}, student location=${studentLatitude != null && studentLongitude != null}")
            }
            
            if (qrData != null) {
                val request = ValidateQRRequest(
                    courseId = qrData.courseId,
                    weekNumber = qrData.weekNumber,
                    createdAt = qrData.createdAt,
                    expireAfter = qrData.expireAfter,
                    studentId = "",
                    studentEmail = studentEmail,
                    studentLatitude = studentLatitude,
                    studentLongitude = studentLongitude
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
    
    suspend fun getWeeksWithQR(courseId: Long): List<WeekWithQR>? {
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
            android.util.Log.d("ApiService", "getWeeksWithQR (edge) code=${response.code} body=$responseBody")
            
            if (response.isSuccessful) {
                val result = moshi.adapter(GetWeeksResponse::class.java).fromJson(responseBody)
                val weeks = result?.weeks
                android.util.Log.d("ApiService", "getWeeksWithQR courseId=$courseId found=${weeks?.size ?: 0} weeks")
                weeks ?: getWeeksWithQRRestFallback(courseId)
            } else {
                android.util.Log.e("ApiService", "getWeeksWithQR failed: ${response.code} body=$responseBody")
                getWeeksWithQRRestFallback(courseId)
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "getWeeksWithQR error: ${e.message}", e)
            getWeeksWithQRRestFallback(courseId)
        }
    }

    private suspend fun getWeeksWithQRRestFallback(courseId: Long): List<WeekWithQR>? {
        return try {
            val httpRequest = Request.Builder()
                .url("$restBaseUrl/qr_codes?course_id=eq.$courseId&is_active=eq.true&select=course_id,week_number,created_at,is_active&order=week_number.asc")
                .get()
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val responseBody = response.body?.string()
            android.util.Log.d("ApiService", "getWeeksWithQR fallback (REST) code=${response.code} body=$responseBody")

            if (!response.isSuccessful) {
                android.util.Log.e("ApiService", "getWeeksWithQR fallback failed: ${response.code} body=$responseBody")
                return null
            }

            val type = Types.newParameterizedType(List::class.java, WeekWithQR::class.java)
            val weeks = moshi.adapter<List<WeekWithQR>>(type).fromJson(responseBody ?: "[]")
            android.util.Log.d("ApiService", "getWeeksWithQR fallback parsed ${weeks?.size ?: 0} weeks for courseId=$courseId")
            weeks
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "getWeeksWithQR fallback error: ${e.message}", e)
            null
        }
    }
    
    suspend fun getCourseWeeks(courseId: Long): List<CourseWeek>? {
        return try {
            val httpRequest = Request.Builder()
                .url("$restBaseUrl/course_weeks?course_id=eq.$courseId&select=id,course_id,week_number,has_qr,locked&order=week_number.asc")
                .get()
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = withContext(Dispatchers.IO) {
                client.newCall(httpRequest).execute()
            }
            val responseBody = response.body?.string()
            android.util.Log.d("ApiService", "getCourseWeeks code=${response.code} body=$responseBody")

            if (!response.isSuccessful) {
                android.util.Log.e("ApiService", "getCourseWeeks failed: ${response.code} body=$responseBody")
                return null
            }

            val type = Types.newParameterizedType(List::class.java, CourseWeek::class.java)
            val weeks = moshi.adapter<List<CourseWeek>>(type).fromJson(responseBody ?: "[]")
            android.util.Log.d("ApiService", "getCourseWeeks parsed ${weeks?.size ?: 0} weeks for courseId=$courseId")
            weeks
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "getCourseWeeks error: ${e.message}", e)
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
    
    // Öğrencinin yoklama durumunu çek (tüm dersler ve haftalar)
    suspend fun getStudentAttendanceStatus(studentEmail: String): List<StudentCourseWithWeeks>? {
        return try {
            android.util.Log.d("ApiService", "=== getStudentAttendanceStatus START ===")
            android.util.Log.d("ApiService", "Student email: $studentEmail")
            
            // Önce öğrencinin department_id'sini bul (bölümdeki tüm dersleri göstermek için)
            val encodedEmail = java.net.URLEncoder.encode(studentEmail, "UTF-8")
            val studentUrl = "$restBaseUrl/students?select=class_id,department_id&email=eq.$encodedEmail"
            
            android.util.Log.d("ApiService", "Step 1: Getting student class and department: $studentUrl")
            
            val studentRequest = Request.Builder()
                .url(studentUrl)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val studentResponse = withContext(Dispatchers.IO) {
                client.newCall(studentRequest).execute()
            }
            val studentBody = studentResponse.body?.string()
            
            android.util.Log.d("ApiService", "Student response code: ${studentResponse.code}")
            android.util.Log.d("ApiService", "Student response body: $studentBody")
            
            if (!studentResponse.isSuccessful) {
                android.util.Log.e("ApiService", "Student request failed: ${studentResponse.code} - $studentBody")
                return emptyList()
            }
            
            if (studentBody.isNullOrEmpty() || studentBody == "[]") {
                android.util.Log.e("ApiService", "Student not found or empty response: $studentBody")
                return emptyList()
            }
            
            // Parse student data to get class_id and department_id
            val studentData = moshi.adapter(List::class.java).fromJson(studentBody) as? List<Map<String, Any>>
            val classId = studentData?.firstOrNull()?.get("class_id")?.toString()
            val departmentId = studentData?.firstOrNull()?.get("department_id")?.toString()
            
            if (classId.isNullOrBlank() || classId == "null") {
                android.util.Log.e("ApiService", "Student has no class_id. Student data: $studentData")
                return emptyList()
            }
            
            if (departmentId.isNullOrBlank() || departmentId == "null") {
                android.util.Log.e("ApiService", "Student has no department_id. Student data: $studentData")
                return emptyList()
            }
            
            android.util.Log.d("ApiService", "Step 2: Student class_id: $classId, department_id: $departmentId")
            android.util.Log.d("ApiService", "=== IMPORTANT: Student's actual class_id from database: $classId ===")
            
            // Öğrencinin sınıfının akademik yılını, grade_level ve department_id bilgilerini bul
            val encodedClassIdForQuery = java.net.URLEncoder.encode(classId, "UTF-8")
            val classUrl = "$restBaseUrl/classes?select=id,academic_year,name,grade_level,department_id&id=eq.$encodedClassIdForQuery"
            android.util.Log.d("ApiService", "Step 2.1: Getting class academic_year: $classUrl")
            
            val classRequest = Request.Builder()
                .url(classUrl)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val classResponse = withContext(Dispatchers.IO) {
                client.newCall(classRequest).execute()
            }
            val classBody = classResponse.body?.string()
            
            android.util.Log.d("ApiService", "Class response code: ${classResponse.code}")
            android.util.Log.d("ApiService", "Class response body: $classBody")
            
            var classAcademicYear: String? = null
            var actualClassId: String? = null
            var className: String? = null
            var classGradeLevel: Int? = null
            var classDepartmentId: String? = null
            if (classResponse.isSuccessful && !classBody.isNullOrEmpty() && classBody != "[]") {
                val classData = moshi.adapter(List::class.java).fromJson(classBody) as? List<Map<String, Any>>
                val classInfo = classData?.firstOrNull()
                classAcademicYear = classInfo?.get("academic_year")?.toString()
                actualClassId = classInfo?.get("id")?.toString()
                className = classInfo?.get("name")?.toString()
                classGradeLevel = (classInfo?.get("grade_level") as? Number)?.toInt()
                classDepartmentId = classInfo?.get("department_id")?.toString()
                android.util.Log.d("ApiService", "=== Class Info from database ===")
                android.util.Log.d("ApiService", "Class id: $actualClassId")
                android.util.Log.d("ApiService", "Class name: $className")
                android.util.Log.d("ApiService", "Class academic_year: $classAcademicYear")
                android.util.Log.d("ApiService", "Class grade_level: $classGradeLevel")
                android.util.Log.d("ApiService", "Class department_id: $classDepartmentId")
                android.util.Log.d("ApiService", "Student department_id: $departmentId")
                android.util.Log.d("ApiService", "=== Comparing: Student class_id ($classId) vs Actual class_id ($actualClassId) ===")
            }
            
            // Eğer sınıfın akademik yılı yoksa, varsayılan olarak şu anki akademik yılı kullan
            // (Settings tablosundan veya varsayılan değer)
            if (classAcademicYear.isNullOrBlank() || classAcademicYear == "null") {
                android.util.Log.w("ApiService", "Class has no academic_year, using default: 2024-2025")
                classAcademicYear = "2024-2025"
            }
            
            // Şu anki dönem bilgisini al (varsayılan: Güz)
            val currentSemester = "Güz" // TODO: Settings tablosundan veya sistem tarihinden al
            
            android.util.Log.d("ApiService", "Step 2.2: Using academic_year: $classAcademicYear, semester: $currentSemester")
            
            // Öğrencinin profile id'sini bul (attendances için)
            val profileUrl = "$restBaseUrl/profiles?select=id&email=eq.$encodedEmail"
            val profileRequest = Request.Builder()
                .url(profileUrl)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val profileResponse = withContext(Dispatchers.IO) {
                client.newCall(profileRequest).execute()
            }
            val profileBody = profileResponse.body?.string()
            
            android.util.Log.d("ApiService", "Profile response code: ${profileResponse.code}")
            android.util.Log.d("ApiService", "Profile response body: $profileBody")
            
            val profileData = moshi.adapter(List::class.java).fromJson(profileBody) as? List<Map<String, Any>>
            val studentProfileId = profileData?.firstOrNull()?.get("id")?.toString()
            
            if (studentProfileId.isNullOrBlank() || studentProfileId == "null") {
                android.util.Log.e("ApiService", "Student profile not found. Profile data: $profileData")
                return emptyList()
            }
            
            android.util.Log.d("ApiService", "Step 3: Student profile_id: $studentProfileId")
            
            // ÖNEMLİ: Öğrencinin gerçekten yoklama aldığı dersleri bul
            // attendances tablosundan course_id'leri çek (öğrencinin yoklama aldığı dersler)
            android.util.Log.d("ApiService", "Step 4: Getting courses from attendances (courses student actually attended)")
            val attendancesUrl = "$restBaseUrl/attendances?select=course_id,week_number,marked_at&student_id=eq.$studentProfileId"
            android.util.Log.d("ApiService", "Getting attendances: $attendancesUrl")
            
            val attendancesRequest = Request.Builder()
                .url(attendancesUrl)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val attendancesResponse = withContext(Dispatchers.IO) {
                client.newCall(attendancesRequest).execute()
            }
            val attendancesBody = attendancesResponse.body?.string()
            
            android.util.Log.d("ApiService", "Attendances response code: ${attendancesResponse.code}")
            android.util.Log.d("ApiService", "Attendances response body: $attendancesBody")
            
            // attendances tablosundan course_id'leri topla
            val courseIdsFromAttendances = mutableSetOf<Long>()
            val attendancesByCourse = mutableMapOf<Long, MutableMap<Int, String>>() // courseId -> (weekNumber -> marked_at)
            
            if (attendancesResponse.isSuccessful && !attendancesBody.isNullOrEmpty() && attendancesBody != "[]") {
                val attendancesData = moshi.adapter(List::class.java).fromJson(attendancesBody) as? List<Map<String, Any>>
                android.util.Log.d("ApiService", "Found ${attendancesData?.size ?: 0} attendance records")
                
                attendancesData?.forEach { attendance ->
                    // course_id'yi al
                    val courseId = attendance["course_id"]?.let {
                        when (it) {
                            is Number -> it.toLong()
                            is String -> it.toLongOrNull()
                            else -> null
                        }
                    }
                    
                    if (courseId != null) {
                        courseIdsFromAttendances.add(courseId)
                        
                        // week_number ve marked_at'i kaydet
                        val weekNumber = (attendance["week_number"] as? Number)?.toInt()
                        val markedAt = attendance["marked_at"] as? String
                        
                        if (weekNumber != null && markedAt != null) {
                            if (!attendancesByCourse.containsKey(courseId)) {
                                attendancesByCourse[courseId] = mutableMapOf()
                            }
                            attendancesByCourse[courseId]?.put(weekNumber, markedAt)
                        }
                    }
                }
            }
            
            android.util.Log.d("ApiService", "Found ${courseIdsFromAttendances.size} unique courses from attendances")
            
            val result = mutableListOf<StudentCourseWithWeeks>()
            
            // ÖNEMLİ: Öğrencinin sınıfına atanmış dersleri göster (course_class_assignments kullanarak)
            // Önce öğrencinin sınıfına atanmış dersleri çek (sadece öğrencinin akademik yılı ve dönemindeki dersler)
            android.util.Log.d("ApiService", "Step 4: Getting courses assigned to class_id: $classId, academic_year: $classAcademicYear, semester: $currentSemester")
            android.util.Log.d("ApiService", "=== Query Parameters ===")
            android.util.Log.d("ApiService", "Query class_id: $classId")
            android.util.Log.d("ApiService", "Query academic_year: $classAcademicYear")
            android.util.Log.d("ApiService", "Query semester: $currentSemester")
            
            // PostgREST için filtreleri doğru formatta oluştur
            val encodedAcademicYear = java.net.URLEncoder.encode(classAcademicYear, "UTF-8").replace("+", "%20")
            val encodedSemester = java.net.URLEncoder.encode(currentSemester, "UTF-8").replace("+", "%20")
            val encodedClassId = java.net.URLEncoder.encode(classId, "UTF-8")
            
            // PostgREST filtreleri: academic_year ve semester için exact match
            // courses tablosundan department_id bilgisini de al (filtreleme için)
            val assignmentsUrl = "$restBaseUrl/course_class_assignments?select=course_id,class_id,courses(id,name,code,department_id),academic_year,semester&class_id=eq.$encodedClassId&academic_year=eq.$encodedAcademicYear&semester=eq.$encodedSemester&limit=100"
            android.util.Log.d("ApiService", "Getting class course assignments: $assignmentsUrl")
            android.util.Log.d("ApiService", "=== End Query Parameters ===")
            
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
            
            android.util.Log.d("ApiService", "Assignments response code: ${assignmentsResponse.code}")
            android.util.Log.d("ApiService", "Assignments response body: $assignmentsBody")
            
            if (assignmentsResponse.isSuccessful && !assignmentsBody.isNullOrEmpty() && assignmentsBody != "[]") {
                val assignmentsData = moshi.adapter(List::class.java).fromJson(assignmentsBody) as? List<Map<String, Any>>
                android.util.Log.d("ApiService", "Found ${assignmentsData?.size ?: 0} course assignments for class")
                
                assignmentsData?.forEachIndexed { index, assignment ->
                    // Log academic_year and semester for debugging
                    val assignmentAcademicYear = assignment["academic_year"] as? String
                    val assignmentSemester = assignment["semester"] as? String
                    val assignmentClassId = assignment["class_id"] as? String
                    android.util.Log.d("ApiService", "Assignment $index: class_id=$assignmentClassId, academic_year=$assignmentAcademicYear, semester=$assignmentSemester")
                    
                    // KOD TARAFINDA FİLTRELEME: Sadece öğrencinin sınıfına, akademik yılına ve dönemine ait dersleri göster
                    if (assignmentClassId != classId) {
                        android.util.Log.w("ApiService", "Assignment $index filtered out: class_id mismatch ($assignmentClassId != $classId)")
                        return@forEachIndexed
                    }
                    if (assignmentAcademicYear != classAcademicYear || assignmentSemester != currentSemester) {
                        android.util.Log.w("ApiService", "Assignment $index filtered out: academic_year mismatch ($assignmentAcademicYear != $classAcademicYear) or semester mismatch ($assignmentSemester != $currentSemester)")
                        return@forEachIndexed
                    }
                    
                    // Nested structure: assignment["courses"] contains course data
                    val coursesMap = assignment["courses"] as? Map<String, Any>
                    if (coursesMap == null) {
                        android.util.Log.w("ApiService", "Assignment $index has no courses data")
                        return@forEachIndexed
                    }
                    
                    val courseIdValue = coursesMap["id"]
                    val courseId = when (courseIdValue) {
                        is Number -> courseIdValue.toLong()
                        is String -> {
                            android.util.Log.w("ApiService", "Course ID is UUID string, skipping: $courseIdValue")
                            return@forEachIndexed
                        }
                        else -> {
                            android.util.Log.w("ApiService", "Course ID is unknown type: ${courseIdValue?.javaClass?.simpleName}")
                            return@forEachIndexed
                        }
                    }
                    
                    val courseName = coursesMap["name"] as? String
                    if (courseName.isNullOrBlank()) {
                        android.util.Log.w("ApiService", "Course $index has no name")
                        return@forEachIndexed
                    }
                    
                    val courseCode = coursesMap["code"] as? String
                    val courseDepartmentId = coursesMap["department_id"]?.toString()
                    
                    // KOD TARAFINDA FİLTRELEME: Sadece öğrencinin bölümüne ait dersleri göster
                    if (!courseDepartmentId.isNullOrBlank() && courseDepartmentId != "null" && courseDepartmentId != departmentId) {
                        android.util.Log.w("ApiService", "Assignment $index filtered out: course department_id ($courseDepartmentId) != student department_id ($departmentId)")
                        return@forEachIndexed
                    }
                    
                    android.util.Log.d("ApiService", "Class course: id=$courseId, name=$courseName, code=$courseCode, department_id=$courseDepartmentId")
                    result.add(StudentCourseWithWeeks(
                        courseId = courseId,
                        courseName = courseName,
                        courseCode = courseCode,
                        weeks = emptyList() // Haftaları daha sonra toplu olarak dolduracağız
                    ))
                }
            } else {
                android.util.Log.w("ApiService", "No courses found for class_id: $classId, academic_year: $classAcademicYear, semester: $currentSemester")
                // Fallback kaldırıldı - sadece course_class_assignments'ten gelen dersleri gösteriyoruz
                // Bu sayede öğrenci sadece kendi sınıfına atanmış dersleri görür
            }
            
            // Tüm derslerin ID'lerini topla
            val courseIds = result.map { it.courseId }
            if (courseIds.isEmpty()) {
                android.util.Log.d("ApiService", "No courses found, returning empty list")
                return result
            }
            
            android.util.Log.d("ApiService", "Step 5: Fetching QR codes for ${courseIds.size} courses")
            
            // Tüm QR kodlarını tek bir sorguda çek (course_id IN (...))
            val courseIdsStr = courseIds.joinToString(",")
            val qrCodesUrl = "$restBaseUrl/qr_codes?select=course_id,week_number,created_at&course_id=in.($courseIdsStr)&is_active=eq.true&order=course_id,week_number.asc"
            android.util.Log.d("ApiService", "Getting all QR codes: $qrCodesUrl")
            
            val qrCodesRequest = Request.Builder()
                .url(qrCodesUrl)
                .get()
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val qrCodesResponse = withContext(Dispatchers.IO) {
                client.newCall(qrCodesRequest).execute()
            }
            val qrCodesBody = qrCodesResponse.body?.string()
            
            android.util.Log.d("ApiService", "QR codes response code: ${qrCodesResponse.code}")
            
            // QR kodları course_id ve week_number'ye göre grupla
            val qrCodesByCourse = mutableMapOf<Long, MutableList<Pair<Int, String?>>>()
            
            if (qrCodesResponse.isSuccessful && !qrCodesBody.isNullOrEmpty() && qrCodesBody != "[]") {
                val qrCodesData = moshi.adapter(List::class.java).fromJson(qrCodesBody) as? List<Map<String, Any>>
                android.util.Log.d("ApiService", "Found ${qrCodesData?.size ?: 0} QR codes total")
                
                qrCodesData?.forEach { qrCode ->
                    val courseId = (qrCode["course_id"] as? Number)?.toLong() ?: return@forEach
                    val weekNumber = (qrCode["week_number"] as? Number)?.toInt() ?: return@forEach
                    val qrCreatedAt = qrCode["created_at"] as? String
                    
                    if (!qrCodesByCourse.containsKey(courseId)) {
                        qrCodesByCourse[courseId] = mutableListOf()
                    }
                    qrCodesByCourse[courseId]?.add(Pair(weekNumber, qrCreatedAt))
                }
            }
            
            // Her ders için haftaları oluştur
            // attendancesByCourse zaten attendances'ten geldi (yukarıda oluşturuldu)
            android.util.Log.d("ApiService", "Step 6: Building week attendance data")
            result.forEach { course ->
                val weeks = mutableListOf<StudentWeekAttendance>()
                val qrCodes = qrCodesByCourse[course.courseId] ?: emptyList()
                val attendances = attendancesByCourse[course.courseId] ?: emptyMap()
                
                // Eğer QR kodları varsa, onları kullan
                if (qrCodes.isNotEmpty()) {
                    qrCodes.forEach { (weekNumber, qrCreatedAt) ->
                        val hasAttendance = attendances.containsKey(weekNumber)
                        val attendanceTime = attendances[weekNumber]
                        
                        weeks.add(StudentWeekAttendance(
                            weekNumber = weekNumber,
                            qrCreatedAt = qrCreatedAt,
                            hasAttendance = hasAttendance,
                            attendanceTime = attendanceTime
                        ))
                    }
                } else {
                    // QR kodları yoksa, sadece attendance kayıtlarını göster
                    attendances.forEach { (weekNumber, attendanceTime) ->
                        weeks.add(StudentWeekAttendance(
                            weekNumber = weekNumber,
                            qrCreatedAt = null,
                            hasAttendance = true,
                            attendanceTime = attendanceTime
                        ))
                    }
                }
                
                // weeks listesini güncelle
                val courseIndex = result.indexOf(course)
                result[courseIndex] = StudentCourseWithWeeks(
                    courseId = course.courseId,
                    courseName = course.courseName,
                    courseCode = course.courseCode,
                    weeks = weeks.sortedBy { it.weekNumber }
                )
            }
            
            android.util.Log.d("ApiService", "=== getStudentAttendanceStatus END ===")
            android.util.Log.d("ApiService", "Total courses found: ${result.size}")
            result
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "=== EXCEPTION in getStudentAttendanceStatus ===")
            android.util.Log.e("ApiService", "Exception message: ${e.message}")
            android.util.Log.e("ApiService", "Exception type: ${e.javaClass.simpleName}")
            android.util.Log.e("ApiService", "Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
}
