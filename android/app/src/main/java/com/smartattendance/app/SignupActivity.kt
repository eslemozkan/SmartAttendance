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

        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirm.text.toString().trim()

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
                    val result = api.studentSignup(email, password)
                    
                    if (result.ok) {
                        Toast.makeText(this@SignupActivity, "Kayıt başarılı!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignupActivity, StudentActivity::class.java)
                        intent.putExtra("user_type", "student")
                        intent.putExtra("email", email)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = when (result.error) {
                            "email_not_whitelisted" -> "Bu email ile kayıt izni yok. Admin'e başvurun."
                            "user_already_exists" -> "Bu email ile zaten kayıtlı bir hesap var."
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


