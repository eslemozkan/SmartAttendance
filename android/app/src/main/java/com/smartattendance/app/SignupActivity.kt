package com.smartattendance.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartattendance.app.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private val api = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preselect role if provided
        val prefillRole = intent.getStringExtra("prefill_role")
        if (prefillRole == "teacher") {
            binding.rbTeacher.isChecked = true
            binding.tvTitle.text = "Öğretmen Kayıt"
        }

        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Update title based on selected role for clarity
        binding.rbStudent.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.tvTitle.text = "Öğrenci Kayıt"
        }
        binding.rbTeacher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.tvTitle.text = "Öğretmen Kayıt"
        }
        // Initialize title according to default selection
        binding.tvTitle.text = if (binding.rbTeacher.isChecked) "Öğretmen Kayıt" else "Öğrenci Kayıt"

        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirm.text.toString().trim()
            val isTeacher = binding.rbTeacher.isChecked

            if (email.isEmpty() || !email.contains("@")) {
                Toast.makeText(this, "Geçerli okul maili girin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Şifre en az 6 karakter olmalı", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Toast.makeText(this, "Şifreler uyuşmuyor", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            lifecycleScope.launch {
                try {
                    val result = if (isTeacher) api.teacherSignup(email, password) else api.studentSignup(email, password)
                    
                    if (result.ok) {
                        Toast.makeText(this@SignupActivity, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                        val dest = if (isTeacher) TeacherActivity::class.java else StudentActivity::class.java
                        val intent = Intent(this@SignupActivity, dest)
                        intent.putExtra("user_type", if (isTeacher) "teacher" else "student")
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = when (result.error) {
                            "email_not_whitelisted" -> if (isTeacher) "Bu email ile öğretmen kaydı izni yok. Admin'e başvurun." else "Bu email ile kayıt izni yok. Admin'e başvurun."
                            "user_already_exists" -> "Bu email ile zaten kayıtlı bir hesap var."
                            "user_already_registered" -> "Bu email zaten sisteme bağlanmış."
                            "password_too_short" -> "Şifre en az 6 karakter olmalı."
                            "email_and_password_required" -> "Email ve şifre gerekli."
                            else -> "Kayıt sırasında hata oluştu: ${result.error ?: "Bilinmeyen hata"}"
                        }
                        Toast.makeText(this@SignupActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@SignupActivity, "Ağ hatası: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    setLoading(false)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnSignup.isEnabled = !loading
        binding.etEmail.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.etConfirm.isEnabled = !loading
    }
}


