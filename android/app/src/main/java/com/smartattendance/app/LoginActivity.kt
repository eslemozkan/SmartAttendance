package com.smartattendance.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.smartattendance.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

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
            
            if (validateStudentLogin(email, password)) {
                val intent = Intent(this, StudentActivity::class.java)
                intent.putExtra("user_type", "student")
                intent.putExtra("email", email)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Geçersiz öğrenci bilgileri", Toast.LENGTH_SHORT).show()
            }
        }

        // Teacher login
        binding.btnTeacherLogin.setOnClickListener {
            val email = binding.etTeacherEmail.text.toString().trim()
            val password = binding.etTeacherPassword.text.toString().trim()
            
            if (validateTeacherLogin(email, password)) {
                val intent = Intent(this, TeacherActivity::class.java)
                intent.putExtra("user_type", "teacher")
                intent.putExtra("email", email)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Geçersiz öğretmen bilgileri", Toast.LENGTH_SHORT).show()
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
    }

    private fun validateStudentLogin(email: String, password: String): Boolean {
        // Dummy validation - replace with real authentication
        return email.isNotEmpty() && password.isNotEmpty() && 
               (email.contains("@") || email == "admin") && password == "admin"
    }

    private fun validateTeacherLogin(email: String, password: String): Boolean {
        // Dummy validation - replace with real authentication
        return email.isNotEmpty() && password.isNotEmpty() && 
               (email.contains("@") || email == "admin") && password == "admin"
    }
}
