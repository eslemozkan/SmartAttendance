package com.smartattendance.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.smartattendance.app.databinding.ActivityTeacherBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TeacherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherBinding
    private val apiService = ApiService()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            generateQRCode()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        binding.btnGenerateQR.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                generateQRCode()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnStopAttendance.setOnClickListener {
            stopAttendance()
        }
    }

    private fun generateQRCode() {
        val duration = when {
            binding.rb5min.isChecked -> 5
            binding.rb10min.isChecked -> 10
            binding.rb15min.isChecked -> 15
            binding.rb30min.isChecked -> 30
            else -> 15
        }

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnGenerateQR.isEnabled = false

        lifecycleScope.launch {
            try {
                val qrData = withContext(Dispatchers.IO) {
                    apiService.createQRCode(assignmentId = 1, expireAfterMinutes = duration)
                }
                
                if (qrData != null) {
                    val qrBitmap = generateQRBitmap(qrData.qr.toString())
                    binding.ivQRCode.setImageBitmap(qrBitmap)
                    binding.tvStatus.text = "QR Code generated! Valid for $duration minutes"
                    binding.btnStopAttendance.isEnabled = true
                    
                    // Start countdown
                    startCountdown(duration * 60)
                } else {
                    Toast.makeText(this@TeacherActivity, "Failed to generate QR code", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@TeacherActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGenerateQR.isEnabled = true
            }
        }
    }

    private fun generateQRBitmap(data: String): android.graphics.Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    private fun startCountdown(seconds: Int) {
        var remainingSeconds = seconds
        val timer = Timer()
        
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val minutes = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    binding.tvCountdown.text = String.format("%02d:%02d", minutes, secs)
                    
                    if (remainingSeconds <= 0) {
                        timer.cancel()
                        binding.tvStatus.text = "QR Code expired"
                        binding.btnStopAttendance.isEnabled = false
                        binding.ivQRCode.setImageDrawable(null)
                    }
                    remainingSeconds--
                }
            }
        }, 0, 1000)
    }

    private fun stopAttendance() {
        binding.tvStatus.text = "Attendance stopped"
        binding.tvCountdown.text = "00:00"
        binding.btnStopAttendance.isEnabled = false
        binding.ivQRCode.setImageDrawable(null)
    }
}
