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
}
