package com.smartattendance.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartattendance.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val api = ApiService()
    private lateinit var sharedPrefs: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "SmartAttendancePrefs"
        private const val KEY_LAST_USER_EMAIL = "last_user_email"
        private const val KEY_LAST_USER_TYPE = "last_user_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        sharedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupUI()
    }

    private fun setupUI() {
        // Role selection
        binding.btnStudent.setOnClickListener {
            binding.layoutStudentLogin.visibility = android.view.View.VISIBLE
            binding.layoutTeacherLogin.visibility = android.view.View.GONE
            binding.tvRoleTitle.text = "Öğrenci Girişi"
            binding.tvRoleSubtitle.text = "Öğrenci hesabınızla giriş yapın"
            
            // Aktif buton görsel geri bildirimi
            updateButtonStates(true)
        }

        binding.btnTeacher.setOnClickListener {
            binding.layoutStudentLogin.visibility = android.view.View.GONE
            binding.layoutTeacherLogin.visibility = android.view.View.VISIBLE
            binding.tvRoleTitle.text = "Akademik Personel Girişi"
            binding.tvRoleSubtitle.text = "Öğretmen hesabınızla giriş yapın"
            
            // Aktif buton görsel geri bildirimi
            updateButtonStates(false)
        }

        // Student login
        binding.btnStudentLogin.setOnClickListener {
            val email = binding.etStudentEmail.text.toString().trim()
            val password = binding.etStudentPassword.text.toString().trim()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                performStudentLogin(email, password)
            } else {
                Toast.makeText(this, "Email ve şifre girin", Toast.LENGTH_SHORT).show()
            }
        }

        // Teacher login
        binding.btnTeacherLogin.setOnClickListener {
            val email = binding.etTeacherEmail.text.toString().trim()
            val password = binding.etTeacherPassword.text.toString().trim()
            
            // Teacher login için hala basit validation (henüz tam entegre değil)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                performTeacherLogin(email, password)
            } else {
                Toast.makeText(this, "Email ve şifre girin", Toast.LENGTH_SHORT).show()
            }
        }

        // Back buttons
        binding.btnBackFromStudent.setOnClickListener {
            binding.layoutStudentLogin.visibility = android.view.View.GONE
            binding.layoutTeacherLogin.visibility = android.view.View.GONE
            binding.tvRoleTitle.text = "SmartAttendance"
            binding.tvRoleSubtitle.text = "Akademik Yoklama Sistemi"
            // Buton durumlarını sıfırla
            resetButtonStates()
        }

        binding.btnBackFromTeacher.setOnClickListener {
            binding.layoutStudentLogin.visibility = android.view.View.GONE
            binding.layoutTeacherLogin.visibility = android.view.View.GONE
            binding.tvRoleTitle.text = "SmartAttendance"
            binding.tvRoleSubtitle.text = "Akademik Yoklama Sistemi"
            // Buton durumlarını sıfırla
            resetButtonStates()
        }

        // Forgot password link (Student)
        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog(true) // true = student
        }

        // Forgot password link (Teacher)
        binding.tvTeacherForgotPassword.setOnClickListener {
            showForgotPasswordDialog(false) // false = teacher
        }

        // Signup link
        binding.tvSignupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        // Teacher signup link
        binding.tvTeacherSignupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            intent.putExtra("prefill_role", "teacher")
            startActivity(intent)
        }
    }
    
    private fun updateButtonStates(isStudentSelected: Boolean) {
        // Öğrenci butonu
        binding.btnStudent.background = if (isStudentSelected) {
            resources.getDrawable(com.smartattendance.app.R.drawable.button_academic_selected, null)
        } else {
            resources.getDrawable(com.smartattendance.app.R.drawable.button_academic_unselected, null)
        }
        binding.btnStudent.setTextColor(if (isStudentSelected) {
            android.graphics.Color.WHITE
        } else {
            android.graphics.Color.parseColor("#1976D2")
        })
        
        // Öğretmen butonu
        binding.btnTeacher.background = if (!isStudentSelected) {
            resources.getDrawable(com.smartattendance.app.R.drawable.button_academic_selected, null)
        } else {
            resources.getDrawable(com.smartattendance.app.R.drawable.button_academic_unselected, null)
        }
        binding.btnTeacher.setTextColor(if (!isStudentSelected) {
            android.graphics.Color.WHITE
        } else {
            android.graphics.Color.parseColor("#1976D2")
        })
        
        // Animasyon ekle
        binding.btnStudent.animate()
            .scaleX(if (isStudentSelected) 0.95f else 1.0f)
            .scaleY(if (isStudentSelected) 0.95f else 1.0f)
            .setDuration(150)
            .withEndAction {
                binding.btnStudent.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            }
            .start()
            
        binding.btnTeacher.animate()
            .scaleX(if (!isStudentSelected) 0.95f else 1.0f)
            .scaleY(if (!isStudentSelected) 0.95f else 1.0f)
            .setDuration(150)
            .withEndAction {
                binding.btnTeacher.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }
    
    private fun resetButtonStates() {
        binding.btnStudent.background = resources.getDrawable(com.smartattendance.app.R.drawable.button_academic_unselected, null)
        binding.btnStudent.setTextColor(android.graphics.Color.parseColor("#1976D2"))
        binding.btnTeacher.background = resources.getDrawable(com.smartattendance.app.R.drawable.button_academic_unselected, null)
        binding.btnTeacher.setTextColor(android.graphics.Color.parseColor("#1976D2"))
    }
    
    private fun showForgotPasswordDialog(isStudent: Boolean) {
        val email = if (isStudent) {
            binding.etStudentEmail.text.toString().trim()
        } else {
            binding.etTeacherEmail.text.toString().trim()
        }
        
        val input = android.widget.EditText(this)
        input.hint = "E-posta adresinizi girin"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        if (email.isNotEmpty()) {
            input.setText(email)
        }
        
        // Set padding for EditText
        val padding = (16 * resources.displayMetrics.density).toInt()
        input.setPadding(padding, padding, padding, padding)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("Şifremi Unuttum")
            .setMessage("E-posta adresinize şifre sıfırlama bağlantısı gönderilecektir.")
            .setView(input)
            .setPositiveButton("Gönder") { _, _ ->
                val emailToSend = input.text.toString().trim()
                if (emailToSend.isNotEmpty()) {
                    resetPassword(emailToSend)
                } else {
                    Toast.makeText(this, "Lütfen e-posta adresinizi girin", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .create()
        
        dialog.show()
    }
    
    private fun resetPassword(email: String) {
        lifecycleScope.launch {
            try {
                binding.btnStudentLogin.isEnabled = false
                binding.btnTeacherLogin.isEnabled = false
                
                // Use Supabase REST API directly for password reset
                // This is the standard way to use Supabase's built-in password reset
                val supabaseUrl = "https://oubvhffqbsxsnbtinzbl.supabase.co"
                val anonKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI"
                
                val url = "$supabaseUrl/auth/v1/recover"
                val payload = org.json.JSONObject().apply {
                    put("email", email)
                    put("redirect_to", SupabaseClient.REDIRECT_URL)
                }
                
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .post(payload.toString().toByteArray().toRequestBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .build()
                
                val client = okhttp3.OkHttpClient()
                val response = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                
                val responseBody = response.body?.string()
                android.util.Log.d("LoginActivity", "Reset password response: ${response.code} - $responseBody")
                
                if (response.isSuccessful) {
                    // Success - Supabase will send email automatically
                    runOnUiThread {
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("Şifre Sıfırlama")
                            .setMessage("Şifre sıfırlama bağlantısı e-posta adresinize gönderildi. Lütfen e-postanızı (ve spam klasörünü) kontrol edin.")
                            .setPositiveButton("Tamam", null)
                            .show()
                    }
                } else {
                    // Parse error message from response body
                    var errorMsg = ""
                    try {
                        if (responseBody != null) {
                            val jsonObject = org.json.JSONObject(responseBody)
                            errorMsg = jsonObject.optString("msg", "")
                        }
                    } catch (e: Exception) {
                        android.util.Log.w("LoginActivity", "Failed to parse error response: ${e.message}")
                    }
                    
                    // Handle errors
                    val errorMessage = when (response.code) {
                        429 -> "Güvenlik nedeniyle, şifre sıfırlama isteği çok sık gönderilemez. Lütfen 60 saniye bekleyip tekrar deneyin."
                        400 -> "Geçersiz e-posta adresi veya bu e-posta adresi sistemde kayıtlı değil."
                        500 -> {
                            if (errorMsg.contains("Error sending recovery email", ignoreCase = true)) {
                                "E-posta gönderilemedi. Supabase SMTP ayarları yapılandırılmamış olabilir. Lütfen sistem yöneticisi ile iletişime geçin veya daha sonra tekrar deneyin."
                            } else {
                                "Sunucu hatası oluştu. Lütfen daha sonra tekrar deneyin."
                            }
                        }
                        else -> "Şifre sıfırlama isteği gönderilemedi. Hata kodu: ${response.code}"
                    }
                    
                    runOnUiThread {
                        AlertDialog.Builder(this@LoginActivity)
                            .setTitle("Şifre Sıfırlama Hatası")
                            .setMessage(errorMessage)
                            .setPositiveButton("Tamam", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Reset password error: ${e.message}", e)
                
                runOnUiThread {
                    AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Şifre Sıfırlama Hatası")
                        .setMessage("Şifre sıfırlama isteği gönderilemedi: ${e.message}")
                        .setPositiveButton("Tamam", null)
                        .show()
                }
            } finally {
                binding.btnStudentLogin.isEnabled = true
                binding.btnTeacherLogin.isEnabled = true
            }
        }
    }
    
    private fun showResetLinkDialog(resetLink: String, email: String) {
        val message = "Şifre sıfırlama bağlantınız hazır. Aşağıdaki bağlantıyı tarayıcınızda açarak şifrenizi sıfırlayabilirsiniz.\n\n$resetLink"
        
        AlertDialog.Builder(this)
            .setTitle("Şifre Sıfırlama Bağlantısı")
            .setMessage(message)
            .setPositiveButton("Bağlantıyı Kopyala") { _, _ ->
                val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Reset Link", resetLink)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Bağlantı kopyalandı! Tarayıcınızda yapıştırıp açabilirsiniz.", Toast.LENGTH_LONG).show()
            }
            .setNeutralButton("Tarayıcıda Aç") { _, _ ->
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(resetLink))
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Tarayıcı açılamadı. Lütfen bağlantıyı kopyalayın.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Kapat", null)
            .show()
    }

    private fun performStudentLogin(email: String, password: String) {
        binding.btnStudentLogin.isEnabled = false
        Toast.makeText(this, "Giriş yapılıyor...", Toast.LENGTH_SHORT).show()
        
        // Tek cihaz kontrolü: Sadece öğrenci hesapları için kontrol yap (öğretmen hesapları sayılmaz)
        val lastUserEmail = sharedPrefs.getString(KEY_LAST_USER_EMAIL, null)
        val lastUserType = sharedPrefs.getString(KEY_LAST_USER_TYPE, null)
        
        // Sadece öğrenci hesabı varsa kontrol yap, öğretmen hesabı varsa görmezden gel
        if (lastUserEmail != null && lastUserEmail != email && lastUserType == "student") {
            // Farklı bir öğrenci hesabı giriş yapıyor, önceki session'ı temizle
            android.util.Log.d("LoginActivity", "Different student login detected: $lastUserEmail -> $email")
            AlertDialog.Builder(this)
                .setTitle("Farklı Öğrenci Hesabı")
                .setMessage("Bu cihazda başka bir öğrenci hesabı ($lastUserEmail) açık. Yeni hesap ile giriş yapmak için önceki oturum kapatılacak. Devam etmek istiyor musunuz?")
                .setPositiveButton("Evet") { _, _ ->
                    proceedWithStudentLogin(email, password)
                }
                .setNegativeButton("İptal") { _, _ ->
                    binding.btnStudentLogin.isEnabled = true
                }
                .setCancelable(false)
                .show()
            return
        }
        
        proceedWithStudentLogin(email, password)
    }
    
    private fun proceedWithStudentLogin(email: String, password: String) {
        lifecycleScope.launch {
            try {
                // Allow dummy admin/admin for homework convenience
                if (email == "admin" && password == "admin") {
                    // Session bilgisini kaydet
                    sharedPrefs.edit()
                        .putString(KEY_LAST_USER_EMAIL, email)
                        .putString(KEY_LAST_USER_TYPE, "student")
                        .apply()
                    
                    val intent = Intent(this@LoginActivity, StudentActivity::class.java)
                    intent.putExtra("user_type", "student")
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                    return@launch
                }
                // Supabase REST API ile login yap
                val success = api.studentLogin(email, password)
                
                if (success) {
                    // Session bilgisini kaydet
                    sharedPrefs.edit()
                        .putString(KEY_LAST_USER_EMAIL, email)
                        .putString(KEY_LAST_USER_TYPE, "student")
                        .apply()
                    
                    val intent = Intent(this@LoginActivity, StudentActivity::class.java)
                    intent.putExtra("user_type", "student")
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Geçersiz email veya şifre", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Giriş hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnStudentLogin.isEnabled = true
            }
        }
    }

    private fun performTeacherLogin(email: String, password: String) {
        // Öğretmen hesapları için tek cihaz kontrolü yok - direkt giriş yap
        proceedWithTeacherLogin(email, password)
    }
    
    private fun proceedWithTeacherLogin(email: String, password: String) {
        // Allow dummy admin/admin or simple validation
        if ((email == "admin" && password == "admin") || (email.contains("@") && password.isNotEmpty())) {
            // Session bilgisini kaydet
            sharedPrefs.edit()
                .putString(KEY_LAST_USER_EMAIL, email)
                .putString(KEY_LAST_USER_TYPE, "teacher")
                .apply()
            
            val intent = Intent(this, TeacherActivity::class.java)
            intent.putExtra("user_type", "teacher")
            intent.putExtra("email", email)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Geçersiz öğretmen bilgileri", Toast.LENGTH_SHORT).show()
        }
    }
}
