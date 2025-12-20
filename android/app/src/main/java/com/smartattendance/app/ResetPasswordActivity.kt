package com.smartattendance.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartattendance.app.databinding.ActivityResetPasswordBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import okhttp3.MediaType.Companion.toMediaType

/**
 * Reset Password Activity
 * 
 * This activity is opened from the email link when user clicks "Reset Password"
 * User is already authenticated via Supabase magic link in the email
 * User just needs to enter a new password
 */
class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        handleDeepLink()
    }
    
    private fun setupUI() {
        binding.btnResetPassword.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            if (newPassword.isEmpty()) {
                binding.etNewPassword.error = "Yeni şifre girin"
                return@setOnClickListener
            }
            
            if (newPassword.length < 6) {
                binding.etNewPassword.error = "Şifre en az 6 karakter olmalı"
                return@setOnClickListener
            }
            
            if (newPassword != confirmPassword) {
                binding.etConfirmPassword.error = "Şifreler eşleşmiyor"
                return@setOnClickListener
            }
            
            updatePassword(newPassword)
        }
    }
    
    /**
     * Handle deep link from email
     * The email link contains authentication token
     * We need to verify the token with Supabase before allowing password update
     * 
     * Handles:
     * 1. Direct deep link: com.smartattendance.app://reset-password?token=xxx&type=recovery
     * 2. Web URL: https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/verify?token=xxx&type=recovery
     * 3. Gmail redirect: https://www.google.com/url?q=GERÇEK_LINK&source=gmail&...
     */
    private fun handleDeepLink() {
        val data = intent.data
        if (data != null) {
            android.util.Log.d("ResetPasswordActivity", "Deep link received: $data")
            
            // Check if this is a Gmail redirect URL
            if (data.scheme == "https" && data.host == "www.google.com" && data.pathSegments.contains("url")) {
                // Gmail redirect URL - extract the real link
                val realUrl = extractRealUrlFromGmailRedirect(data.toString())
                if (realUrl != null) {
                    android.util.Log.d("ResetPasswordActivity", "Extracted real URL from Gmail redirect: $realUrl")
                    // Parse the real URL
                    val realUri = android.net.Uri.parse(realUrl)
                    processResetPasswordLink(realUri)
                } else {
                    android.util.Log.w("ResetPasswordActivity", "Could not extract real URL from Gmail redirect")
                    showError("Geçersiz şifre sıfırlama bağlantısı. Lütfen e-postanızdaki en son bağlantıyı kullanın.")
                }
            } else {
                // Direct link (deep link or web URL)
                processResetPasswordLink(data)
            }
        } else {
            // No deep link data - user might have opened activity directly
            android.util.Log.w("ResetPasswordActivity", "No deep link data found")
            showError("Bu sayfaya sadece e-postanızdaki bağlantı üzerinden erişebilirsiniz.")
        }
    }
    
    /**
     * Extract the real URL from Gmail's redirect URL
     * Format: https://www.google.com/url?q=ENCODED_URL&source=gmail&...
     */
    private fun extractRealUrlFromGmailRedirect(gmailUrl: String): String? {
        return try {
            val uri = android.net.Uri.parse(gmailUrl)
            val qParam = uri.getQueryParameter("q")
            if (qParam != null) {
                // URL decode the parameter
                java.net.URLDecoder.decode(qParam, "UTF-8")
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ResetPasswordActivity", "Error extracting real URL: ${e.message}", e)
            null
        }
    }
    
    /**
     * Process the reset password link (either direct deep link or web URL)
     */
    private fun processResetPasswordLink(data: android.net.Uri) {
        // Extract token from URL
        val token = data.getQueryParameter("token")
        val type = data.getQueryParameter("type")
        
        if (token != null && type == "recovery") {
            // Token is present, verify it with Supabase
            android.util.Log.d("ResetPasswordActivity", "Reset password token received: $token")
            verifyTokenAndAuthenticate(token)
        } else if (data.scheme == "com.smartattendance.app" && data.host == "reset-password") {
            // Deep link format is correct, but token might be missing
            android.util.Log.d("ResetPasswordActivity", "Reset password deep link received")
            // User can still try to update password, Supabase will verify
        } else if (data.scheme == "https" && data.host == "oubvhffqbsxsnbtinzbl.supabase.co" && data.pathSegments.contains("verify")) {
            // Web URL format - token might be in the URL path or query
            android.util.Log.d("ResetPasswordActivity", "Supabase verify URL received")
            // Try to extract token from query parameters
            val urlToken = data.getQueryParameter("token")
            if (urlToken != null) {
                verifyTokenAndAuthenticate(urlToken)
            } else {
                android.util.Log.w("ResetPasswordActivity", "Token not found in URL")
                showError("Geçersiz şifre sıfırlama bağlantısı. Lütfen e-postanızdaki en son bağlantıyı kullanın.")
            }
        } else {
            // Invalid link format
            android.util.Log.w("ResetPasswordActivity", "Invalid deep link format: $data")
            showError("Geçersiz şifre sıfırlama bağlantısı. Lütfen e-postanızdaki en son bağlantıyı kullanın.")
        }
    }
    
    /**
     * Verify token with Supabase and authenticate user
     * This is necessary for web URL links
     * We exchange the token for a session, then user can update password
     */
    private fun verifyTokenAndAuthenticate(token: String) {
        lifecycleScope.launch {
            try {
                binding.progressBar.visibility = android.view.View.VISIBLE
                
                // Exchange token for session using Supabase
                // This will authenticate the user session
                val supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co"
                val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
                
                // Use Supabase's verify endpoint to exchange token for session
                val verifyUrl = "$supabaseUrl/auth/v1/verify?token=$token&type=recovery&redirect_to=${SupabaseClient.REDIRECT_URL}"
                val request = okhttp3.Request.Builder()
                    .url(verifyUrl)
                    .get()
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .build()
                
                val client = okhttp3.OkHttpClient()
                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                
                android.util.Log.d("ResetPasswordActivity", "Token verify response: ${response.code}")
                
                if (response.isSuccessful) {
                    // Token verified, user is now authenticated
                    // We can proceed with password update
                    android.util.Log.d("ResetPasswordActivity", "Token verified successfully")
                    runOnUiThread {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@ResetPasswordActivity, "Bağlantı doğrulandı. Yeni şifrenizi girebilirsiniz.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    android.util.Log.w("ResetPasswordActivity", "Token verification failed: ${response.code}")
                    val responseBody = response.body?.string()
                    android.util.Log.w("ResetPasswordActivity", "Response body: $responseBody")
                    runOnUiThread {
                        binding.progressBar.visibility = android.view.View.GONE
                        showError("Şifre sıfırlama bağlantısının süresi dolmuş veya geçersiz. Lütfen yeni bir şifre sıfırlama isteği gönderin.")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ResetPasswordActivity", "Token verification error: ${e.message}", e)
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    // Don't show error, let user try to update password
                    // Supabase will return error if token is invalid
                }
            }
        }
    }
    
    /**
     * Update user password using Supabase built-in API
     * User is already authenticated via the email link
     */
    private fun updatePassword(newPassword: String) {
        lifecycleScope.launch {
            try {
                binding.btnResetPassword.isEnabled = false
                binding.progressBar.visibility = android.view.View.VISIBLE
                
                // Update password using Supabase Auth API
                // User is already authenticated via the email link
                SupabaseClient.client.auth.updateUser {
                    password = newPassword
                }
                
                // Success
                runOnUiThread {
                    AlertDialog.Builder(this@ResetPasswordActivity)
                        .setTitle("Başarılı")
                        .setMessage("Şifreniz başarıyla güncellendi. Yeni şifrenizle giriş yapabilirsiniz.")
                        .setPositiveButton("Tamam") { _, _ ->
                            // Go back to login
                            finish()
                        }
                        .setCancelable(false)
                        .show()
                }
            } catch (e: Exception) {
                android.util.Log.e("ResetPasswordActivity", "Update password error: ${e.message}", e)
                
                val errorMessage = when {
                    e.message?.contains("expired", ignoreCase = true) == true -> 
                        "Şifre sıfırlama bağlantısının süresi dolmuş. Lütfen yeni bir şifre sıfırlama isteği gönderin."
                    e.message?.contains("invalid", ignoreCase = true) == true -> 
                        "Geçersiz şifre sıfırlama bağlantısı. Lütfen e-postanızdaki en son bağlantıyı kullanın."
                    else -> 
                        "Şifre güncellenemedi: ${e.message}"
                }
                
                runOnUiThread {
                    AlertDialog.Builder(this@ResetPasswordActivity)
                        .setTitle("Hata")
                        .setMessage(errorMessage)
                        .setPositiveButton("Tamam", null)
                        .show()
                }
            } finally {
                binding.btnResetPassword.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Hata")
            .setMessage(message)
            .setPositiveButton("Tamam") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}

