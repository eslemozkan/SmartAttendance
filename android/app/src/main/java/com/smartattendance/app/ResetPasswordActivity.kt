package com.smartattendance.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartattendance.app.databinding.ActivityResetPasswordBinding
import kotlinx.coroutines.launch
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

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
     * Supabase automatically handles the token verification when we call updateUser
     */
    private fun handleDeepLink() {
        val data = intent.data
        if (data != null) {
            android.util.Log.d("ResetPasswordActivity", "Deep link received: $data")
            
            // Supabase email link format:
            // com.smartattendance.app://reset-password?token=xxx&type=recovery
            // OR
            // https://oubvhffqbsxsnbtinzbl.supabase.co/auth/v1/verify?token=xxx&type=recovery&redirect_to=com.smartattendance.app://reset-password
            
            // Extract token from deep link
            val token = data.getQueryParameter("token")
            val type = data.getQueryParameter("type")
            
            if (token != null && type == "recovery") {
                // Token is present, Supabase will verify it when we call updateUser
                android.util.Log.d("ResetPasswordActivity", "Reset password token received: $token")
            } else if (data.scheme == "com.smartattendance.app" && data.host == "reset-password") {
                // Deep link format is correct, token might be in fragment or different format
                android.util.Log.d("ResetPasswordActivity", "Reset password deep link received")
            } else {
                // Invalid link format
                android.util.Log.w("ResetPasswordActivity", "Invalid deep link format: $data")
                // Don't show error immediately, let user try to update password
                // Supabase will return error if token is invalid
            }
        } else {
            // No deep link data - user might have opened activity directly
            android.util.Log.w("ResetPasswordActivity", "No deep link data found")
            showError("Bu sayfaya sadece e-postanızdaki bağlantı üzerinden erişebilirsiniz.")
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

