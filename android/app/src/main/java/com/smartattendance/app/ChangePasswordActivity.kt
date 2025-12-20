package com.smartattendance.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartattendance.app.databinding.ActivityChangePasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val apiService = ApiService()
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co"
    private val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        binding.btnChangePassword.setOnClickListener {
            changePassword()
        }
        
        binding.btnBack.setOnClickListener {
            finish()
        }
    }
    
    private fun changePassword() {
        val currentPassword = binding.etCurrentPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        
        // Validation
        if (currentPassword.isBlank()) {
            showError("Lütfen mevcut şifrenizi girin")
            return
        }
        
        if (newPassword.isBlank()) {
            showError("Lütfen yeni şifrenizi girin")
            return
        }
        
        if (newPassword.length < 6) {
            showError("Yeni şifre en az 6 karakter olmalıdır")
            return
        }
        
        if (newPassword != confirmPassword) {
            showError("Yeni şifreler eşleşmiyor")
            return
        }
        
        if (currentPassword == newPassword) {
            showError("Yeni şifre mevcut şifre ile aynı olamaz")
            return
        }
        
        val email = intent.getStringExtra("email") ?: ""
        if (email.isBlank()) {
            showError("Email bilgisi bulunamadı")
            return
        }
        
        binding.btnChangePassword.isEnabled = false
        binding.tvError.visibility = android.view.View.GONE
        
        lifecycleScope.launch {
            try {
                // Önce mevcut şifre ile login denemesi yap (doğrulama için)
                val loginSuccess = apiService.studentLogin(email, currentPassword)
                if (!loginSuccess) {
                    runOnUiThread {
                        showError("Mevcut şifre yanlış")
                        binding.btnChangePassword.isEnabled = true
                    }
                    return@launch
                }
                
                // Şifre değiştirme işlemi
                val success = withContext(Dispatchers.IO) {
                    changePasswordOnServer(email, currentPassword, newPassword)
                }
                
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@ChangePasswordActivity, "Şifre başarıyla değiştirildi", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        showError("Şifre değiştirilemedi. Lütfen tekrar deneyin.")
                        binding.btnChangePassword.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChangePassword", "Error: ${e.message}", e)
                runOnUiThread {
                    showError("Hata: ${e.message}")
                    binding.btnChangePassword.isEnabled = true
                }
            }
        }
    }
    
    private suspend fun changePasswordOnServer(email: String, currentPassword: String, newPassword: String): Boolean {
        return try {
            // Supabase Auth API: Update user password
            // Önce login yaparak access token al
            val loginUrl = "$supabaseUrl/auth/v1/token?grant_type=password"
            val loginPayload = """{"email":"$email","password":"$currentPassword"}"""
            
            val loginRequest = Request.Builder()
                .url(loginUrl)
                .post(loginPayload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("apikey", anonKey)
                .build()
            
            val loginResponse = client.newCall(loginRequest).execute()
            val loginBody = loginResponse.body?.string()
            
            if (!loginResponse.isSuccessful || loginBody == null) {
                android.util.Log.e("ChangePassword", "Login failed: ${loginResponse.code}")
                return false
            }
            
            // Parse access token from response
            val accessToken = extractAccessToken(loginBody)
            if (accessToken == null) {
                android.util.Log.e("ChangePassword", "Failed to extract access token")
                return false
            }
            
            // Update password using access token
            val updateUrl = "$supabaseUrl/auth/v1/user"
            val updatePayload = """{"password":"$newPassword"}"""
            
            val updateRequest = Request.Builder()
                .url(updateUrl)
                .put(updatePayload.toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("apikey", anonKey)
                .build()
            
            val updateResponse = client.newCall(updateRequest).execute()
            val updateBody = updateResponse.body?.string()
            
            android.util.Log.d("ChangePassword", "Update password response: ${updateResponse.code} - $updateBody")
            
            updateResponse.isSuccessful
        } catch (e: Exception) {
            android.util.Log.e("ChangePassword", "Error changing password: ${e.message}", e)
            false
        }
    }
    
    private fun extractAccessToken(jsonResponse: String): String? {
        return try {
            // Simple JSON parsing for access_token
            val tokenStart = jsonResponse.indexOf("\"access_token\":\"") + 16
            val tokenEnd = jsonResponse.indexOf("\"", tokenStart)
            if (tokenStart > 15 && tokenEnd > tokenStart) {
                jsonResponse.substring(tokenStart, tokenEnd)
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ChangePassword", "Error extracting token: ${e.message}")
            null
        }
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = android.view.View.VISIBLE
    }
}




