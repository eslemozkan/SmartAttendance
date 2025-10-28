package com.smartattendance.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.smartattendance.app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val api = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Role selection
        binding.btnStudent.setOnClickListener {
            binding.layoutStudentLogin.visibility = android.view.View.VISIBLE
            binding.layoutTeacherLogin.visibility = android.view.View.GONE
            binding.tvRoleTitle.text = "Öğrenci Girişi"
            binding.tvRoleSubtitle.text = "Öğrenci hesabınızla giriş yapın"
        }

        binding.btnTeacher.setOnClickListener {
            binding.layoutStudentLogin.visibility = android.view.View.GONE
            binding.layoutTeacherLogin.visibility = android.view.View.VISIBLE
            binding.tvRoleTitle.text = "Akademik Personel Girişi"
            binding.tvRoleSubtitle.text = "Öğretmen hesabınızla giriş yapın"
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
        }

        binding.btnBackFromTeacher.setOnClickListener {
            binding.layoutStudentLogin.visibility = android.view.View.GONE
            binding.layoutTeacherLogin.visibility = android.view.View.GONE
            binding.tvRoleTitle.text = "SmartAttendance"
            binding.tvRoleSubtitle.text = "Akademik Yoklama Sistemi"
        }

        // Signup link
        binding.tvSignupLink.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performStudentLogin(email: String, password: String) {
        binding.btnStudentLogin.isEnabled = false
        Toast.makeText(this, "Giriş yapılıyor...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            try {
                // Supabase REST API ile login yap
                val success = api.studentLogin(email, password)
                
                if (success) {
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
        // Teacher için geçici olarak basit validation
        if (email.contains("@") && password.isNotEmpty()) {
            val intent = Intent(this, TeacherActivity::class.java)
            intent.putExtra("user_type", "teacher")
            intent.putExtra("email", email)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Geçersiz öğretmen bilgileri", Toast.LENGTH_SHORT).show()
        }
    }
}
