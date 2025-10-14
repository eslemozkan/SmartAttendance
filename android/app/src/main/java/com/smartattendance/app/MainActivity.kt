package com.smartattendance.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.smartattendance.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTeacher.setOnClickListener {
            startActivity(Intent(this, TeacherActivity::class.java))
        }
        binding.btnStudent.setOnClickListener {
            startActivity(Intent(this, StudentActivity::class.java))
        }
    }
}


