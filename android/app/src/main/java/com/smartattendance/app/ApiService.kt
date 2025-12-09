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
    @Json(name = "expire_after_minutes") val expireAfterMinutes: Int
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
    @Json(name = "expire_after") val expireAfter: Int
)

@JsonClass(generateAdapter = true)
data class ValidateQRRequest(
    @Json(name = "course_id") val courseId: Long, // BIGINT number (courses.id is BIGINT)
    @Json(name = "week_number") val weekNumber: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after") val expireAfter: Int,
    @Json(name = "student_id") val studentId: String,
    @Json(name = "student_email") val studentEmail: String? = null
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
    
    suspend fun createQRCode(courseId: Long, weekNumber: Int, expireAfterMinutes: Int): CreateQRResponse? {
        return try {
            val request = CreateQRRequest(courseId, weekNumber, expireAfterMinutes)
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
    
    suspend fun validateQRCode(qrDataString: String, studentEmail: String): Boolean? {
        return try {
            android.util.Log.d("ApiService", "QR Data String: $qrDataString")
            android.util.Log.d("ApiService", "Student Email: $studentEmail")
            
            // Parse QR data string (assuming it's JSON)
            val qrData = moshi.adapter(QRData::class.java).fromJson(qrDataString)
            android.util.Log.d("ApiService", "Parsed QR Data: $qrData")
            
            if (qrData != null) {
                val request = ValidateQRRequest(
                    courseId = qrData.courseId,
                    weekNumber = qrData.weekNumber,
                    createdAt = qrData.createdAt,
                    expireAfter = qrData.expireAfter,
                    studentId = "",
                    studentEmail = studentEmail
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
            // Önce öğrencinin sınıfına atanmış dersleri çek
            android.util.Log.d("ApiService", "Step 4: Getting courses assigned to class_id: $classId")
            val assignmentsUrl = "$restBaseUrl/course_class_assignments?select=course_id,courses(id,name,code)&class_id=eq.$classId&limit=100"
            android.util.Log.d("ApiService", "Getting class course assignments: $assignmentsUrl")
            
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
                    
                    android.util.Log.d("ApiService", "Class course: id=$courseId, name=$courseName, code=$courseCode")
                    result.add(StudentCourseWithWeeks(
                        courseId = courseId,
                        courseName = courseName,
                        courseCode = courseCode,
                        weeks = emptyList() // Haftaları daha sonra toplu olarak dolduracağız
                    ))
                }
            } else {
                android.util.Log.w("ApiService", "Could not get department courses. Falling back to old method.")
                // Fallback: Eğer attendances'ten ders bulduysak, onları kullan
                if (courseIdsFromAttendances.isNotEmpty()) {
                    android.util.Log.d("ApiService", "Using courses from attendances: $courseIdsFromAttendances")
                
                // Bu derslerin detaylarını çek
                val courseIdsStr = courseIdsFromAttendances.joinToString(",")
                val coursesUrl = "$restBaseUrl/courses?select=id,name,code&id=in.($courseIdsStr)"
                android.util.Log.d("ApiService", "Step 5: Getting course details: $coursesUrl")
                
                val coursesRequest = Request.Builder()
                    .url(coursesUrl)
                    .get()
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                val coursesResponse = withContext(Dispatchers.IO) {
                    client.newCall(coursesRequest).execute()
                }
                val coursesBody = coursesResponse.body?.string()
                
                android.util.Log.d("ApiService", "Courses response code: ${coursesResponse.code}")
                
                if (coursesResponse.isSuccessful && !coursesBody.isNullOrEmpty() && coursesBody != "[]") {
                    val coursesData = moshi.adapter(List::class.java).fromJson(coursesBody) as? List<Map<String, Any>>
                    android.util.Log.d("ApiService", "Parsed ${coursesData?.size ?: 0} courses")
                    
                    coursesData?.forEachIndexed { index, course ->
                        val courseIdValue = course["id"]
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
                        
                        val courseName = course["name"] as? String
                        if (courseName.isNullOrBlank()) {
                            android.util.Log.w("ApiService", "Course $index has no name")
                            return@forEachIndexed
                        }
                        
                        val courseCode = course["code"] as? String
                        
                        android.util.Log.d("ApiService", "Course: id=$courseId, name=$courseName, code=$courseCode")
                        result.add(StudentCourseWithWeeks(
                            courseId = courseId,
                            courseName = courseName,
                            courseCode = courseCode,
                            weeks = emptyList() // Haftaları daha sonra toplu olarak dolduracağız
                        ))
                    }
                }
            } else {
                // Eğer attendances'ten ders bulamadıysak, course_class_assignments'ten al
                android.util.Log.d("ApiService", "No courses from attendances, trying course_class_assignments")
                val assignmentsUrl = "$restBaseUrl/course_class_assignments?select=course_id&class_id=eq.$classId&limit=100"
                android.util.Log.d("ApiService", "Step 4a: Getting course assignments: $assignmentsUrl")
                
                val assignmentsRequest = Request.Builder()
                    .url(assignmentsUrl)
                    .get()
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .build()
                
                val assignmentsResponse = try {
                    withContext(Dispatchers.IO) {
                        client.newCall(assignmentsRequest).execute()
                    }
                } catch (e: java.net.SocketTimeoutException) {
                    android.util.Log.e("ApiService", "Timeout getting course_class_assignments. This might be an RLS/permission issue.")
                    android.util.Log.e("ApiService", "Trying alternative approach: query courses directly")
                    null
                }
                
                val assignmentsBody = assignmentsResponse?.body?.string()
                android.util.Log.d("ApiService", "Assignments response code: ${assignmentsResponse?.code}")
                android.util.Log.d("ApiService", "Assignments response body: $assignmentsBody")
                
                // Eğer course_class_assignments'e erişilemiyorsa, alternatif yaklaşım dene
                if (assignmentsResponse == null || !assignmentsResponse.isSuccessful || assignmentsBody.isNullOrEmpty() || assignmentsBody == "[]") {
                    android.util.Log.w("ApiService", "Could not get course_class_assignments. Trying alternative: get all courses for now")
                    // Alternatif: Tüm dersleri çek (geçici çözüm - daha sonra düzeltilebilir)
                    val allCoursesUrl = "$restBaseUrl/courses?select=id,name,code&limit=100"
                    android.util.Log.d("ApiService", "Step 4b (alternative): Getting all courses: $allCoursesUrl")
                    
                    val allCoursesRequest = Request.Builder()
                        .url(allCoursesUrl)
                        .get()
                        .addHeader("apikey", anonKey)
                        .addHeader("Authorization", "Bearer $anonKey")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    
                    val allCoursesResponse = withContext(Dispatchers.IO) {
                        client.newCall(allCoursesRequest).execute()
                    }
                    val allCoursesBody = allCoursesResponse.body?.string()
                    
                    android.util.Log.d("ApiService", "All courses response code: ${allCoursesResponse.code}")
                    
                    if (!allCoursesResponse.isSuccessful || allCoursesBody.isNullOrEmpty() || allCoursesBody == "[]") {
                        android.util.Log.e("ApiService", "Could not get courses either. Returning empty list.")
                        return emptyList()
                    }
                    
                    // Parse all courses
                    val allCoursesData = moshi.adapter(List::class.java).fromJson(allCoursesBody) as? List<Map<String, Any>>
                    android.util.Log.d("ApiService", "Parsed ${allCoursesData?.size ?: 0} courses (alternative method)")
                    
                    // Use all courses as fallback (not ideal, but works for testing)
                    val coursesData = allCoursesData ?: emptyList()
                    
                    // Process courses from alternative method
                    coursesData.forEachIndexed { index, course ->
                        android.util.Log.d("ApiService", "Processing course $index: $course")
                        
                        // courses.id BIGINT veya UUID olabilir
                        val courseIdValue = course["id"]
                        val courseId = when (courseIdValue) {
                            is Number -> courseIdValue.toLong()
                            is String -> {
                                // UUID string ise, Long'a çeviremeyiz, atla
                                android.util.Log.w("ApiService", "Course ID is UUID string, skipping: $courseIdValue")
                                return@forEachIndexed
                            }
                            else -> {
                                android.util.Log.w("ApiService", "Course ID is unknown type: ${courseIdValue?.javaClass?.simpleName}")
                                return@forEachIndexed
                            }
                        }
                        
                        val courseName = course["name"] as? String
                        if (courseName.isNullOrBlank()) {
                            android.util.Log.w("ApiService", "Course $index has no name")
                            return@forEachIndexed
                        }
                        
                        val courseCode = course["code"] as? String
                        
                        android.util.Log.d("ApiService", "Course: id=$courseId, name=$courseName, code=$courseCode")
                        result.add(StudentCourseWithWeeks(
                            courseId = courseId,
                            courseName = courseName,
                            courseCode = courseCode,
                            weeks = emptyList() // Haftaları daha sonra toplu olarak dolduracağız
                        ))
                    }
                } else {
                    // Normal flow: parse course_class_assignments
                    val assignmentsData = moshi.adapter(List::class.java).fromJson(assignmentsBody) as? List<Map<String, Any>>
                    val courseIdsFromAssignments = assignmentsData?.mapNotNull { 
                        val courseId = it["course_id"]
                        when (courseId) {
                            is Number -> courseId.toLong()
                            is String -> courseId.toLongOrNull() // Try to parse UUID as Long (might fail)
                            else -> null
                        }
                    }?.distinct() ?: emptyList()
                    
                    android.util.Log.d("ApiService", "Found ${courseIdsFromAssignments.size} unique course IDs from assignments")
                    
                    if (courseIdsFromAssignments.isEmpty()) {
                        android.util.Log.e("ApiService", "No course IDs found in assignments")
                        return emptyList()
                    }
                    
                    // Get course details using course IDs
                    val courseIdsStr = courseIdsFromAssignments.joinToString(",")
                    val coursesUrl = "$restBaseUrl/courses?select=id,name,code&id=in.($courseIdsStr)"
                    android.util.Log.d("ApiService", "Step 4c: Getting course details: $coursesUrl")
                    
                    val coursesRequest = Request.Builder()
                        .url(coursesUrl)
                        .get()
                        .addHeader("apikey", anonKey)
                        .addHeader("Authorization", "Bearer $anonKey")
                        .addHeader("Content-Type", "application/json")
                        .build()
                    
                    val coursesResponse = withContext(Dispatchers.IO) {
                        client.newCall(coursesRequest).execute()
                    }
                    val coursesBody = coursesResponse.body?.string()
                    
                    android.util.Log.d("ApiService", "Courses response code: ${coursesResponse.code}")
                    android.util.Log.d("ApiService", "Courses response body: $coursesBody")
                    
                    if (!coursesResponse.isSuccessful || coursesBody.isNullOrEmpty() || coursesBody == "[]") {
                        android.util.Log.e("ApiService", "Courses request failed: ${coursesResponse.code} - $coursesBody")
                        return emptyList()
                    }
                    
                    // Parse courses
                    val coursesData = moshi.adapter(List::class.java).fromJson(coursesBody) as? List<Map<String, Any>>
                    android.util.Log.d("ApiService", "Parsed ${coursesData?.size ?: 0} course assignments")
                    
                    coursesData?.forEachIndexed { index, course ->
                        android.util.Log.d("ApiService", "Processing course $index: $course")
                        
                        // courses.id BIGINT veya UUID olabilir
                        val courseIdValue = course["id"]
                        val courseId = when (courseIdValue) {
                            is Number -> courseIdValue.toLong()
                            is String -> {
                                // UUID string ise, Long'a çeviremeyiz, atla
                                android.util.Log.w("ApiService", "Course ID is UUID string, skipping: $courseIdValue")
                                return@forEachIndexed
                            }
                            else -> {
                                android.util.Log.w("ApiService", "Course ID is unknown type: ${courseIdValue?.javaClass?.simpleName}")
                                return@forEachIndexed
                            }
                        }
                        
                        val courseName = course["name"] as? String
                        if (courseName.isNullOrBlank()) {
                            android.util.Log.w("ApiService", "Course $index has no name")
                            return@forEachIndexed
                        }
                        
                        val courseCode = course["code"] as? String
                        
                        android.util.Log.d("ApiService", "Course: id=$courseId, name=$courseName, code=$courseCode")
                        result.add(StudentCourseWithWeeks(
                            courseId = courseId,
                            courseName = courseName,
                            courseCode = courseCode,
                            weeks = emptyList() // Haftaları daha sonra toplu olarak dolduracağız
                        ))
                    }
                }
                }
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
