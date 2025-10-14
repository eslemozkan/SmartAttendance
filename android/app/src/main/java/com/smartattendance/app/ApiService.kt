package com.smartattendance.app

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class CreateQRRequest(
    @Json(name = "assignment_id") val assignmentId: Int,
    @Json(name = "expire_after_minutes") val expireAfterMinutes: Int
)

@JsonClass(generateAdapter = true)
data class CreateQRResponse(
    val id: String,
    val qr: QRData
)

@JsonClass(generateAdapter = true)
data class QRData(
    @Json(name = "assignment_id") val assignmentId: Int,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "expire_after") val expireAfter: Int
)

@JsonClass(generateAdapter = true)
data class ValidateQRRequest(
    @Json(name = "assignment_id") val assignmentId: Int,
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
    
    // TODO: Replace with your actual Supabase URL
    private val baseUrl = "https://your-project.supabase.co"
    
    suspend fun createQRCode(assignmentId: Int, expireAfterMinutes: Int): CreateQRResponse? {
        val request = CreateQRRequest(assignmentId, expireAfterMinutes)
        val json = moshi.adapter(CreateQRRequest::class.java).toJson(request)
        
        val httpRequest = Request.Builder()
            .url("$baseUrl/functions/v1/create-qr")
            .post(json.toRequestBody("application/json".toMediaType()))
            .addHeader("Content-Type", "application/json")
            .build()
        
        return try {
            val response = client.newCall(httpRequest).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                moshi.adapter(CreateQRResponse::class.java).fromJson(responseBody)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun validateQRCode(qrDataString: String, studentId: String): Boolean? {
        return try {
            // Parse QR data string (assuming it's JSON)
            val qrData = moshi.adapter(QRData::class.java).fromJson(qrDataString)
            if (qrData != null) {
                val request = ValidateQRRequest(
                    assignmentId = qrData.assignmentId,
                    createdAt = qrData.createdAt,
                    expireAfter = qrData.expireAfter,
                    studentId = studentId
                )
                
                val json = moshi.adapter(ValidateQRRequest::class.java).toJson(request)
                
                val httpRequest = Request.Builder()
                    .url("$baseUrl/functions/v1/validate-qr")
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                val response = client.newCall(httpRequest).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val result = moshi.adapter(ValidateQRResponse::class.java).fromJson(responseBody)
                    result?.ok == true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
