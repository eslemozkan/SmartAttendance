package com.smartattendance.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.smartattendance.app.databinding.ActivityStudentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class StudentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentBinding
    private val apiService = ApiService()
    private lateinit var cameraExecutor: ExecutorService
    private var imageAnalyzer: ImageAnalysis? = null
    private var isScanning = false
    private var hasAttended = false // Track if student has already attended

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()
        setupUI()
    }

    private fun setupUI() {
        binding.btnStartScan.setOnClickListener {
            if (hasAttended) {
                binding.tvStatus.text = "Yoklama zaten alındı!"
                Toast.makeText(this, "Bu ders için yoklama zaten alındı", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startScanning()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnStopScan.setOnClickListener {
            stopScanning()
        }
        
        // Yoklama durumu kontrol butonu
        binding.btnCheckAttendance.setOnClickListener {
            val email = intent.getStringExtra("email") ?: ""
            if (email.isNotBlank()) {
                val intent = Intent(this, StudentAttendanceStatusActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Email bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Logout butonu
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun startScanning() {
        isScanning = true
        binding.btnStartScan.isEnabled = false
        binding.btnStopScan.isEnabled = true
        binding.tvStatus.text = "Scanning for QR code..."
        startCamera()
    }

    private fun stopScanning() {
        isScanning = false
        binding.btnStartScan.isEnabled = true
        binding.btnStopScan.isEnabled = false
        binding.tvStatus.text = "Scanning stopped"
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, QRCodeAnalyzer())
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera error: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private inner class QRCodeAnalyzer : ImageAnalysis.Analyzer {
        private val scanner = BarcodeScanning.getClient()

        override fun analyze(imageProxy: ImageProxy) {
            if (!isScanning) {
                imageProxy.close()
                return
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            barcode.rawValue?.let { qrData ->
                                processQRCode(qrData)
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }
    }

    private fun processQRCode(qrData: String) {
        if (!isScanning) return
        
        stopScanning()
        binding.tvStatus.text = "QR kod işleniyor..."
        
        lifecycleScope.launch {
            try {
                // Öğrencinin konumunu al
                var studentLatitude: Double? = null
                var studentLongitude: Double? = null
                
                if (LocationHelper.hasLocationPermission(this@StudentActivity)) {
                    if (LocationHelper.isLocationEnabled(this@StudentActivity)) {
                        android.util.Log.d("StudentActivity", "Getting student location...")
                        val location = LocationHelper.getCurrentLocation(this@StudentActivity)
                        if (location != null) {
                            studentLatitude = location.latitude
                            studentLongitude = location.longitude
                            android.util.Log.d("StudentActivity", "Student location: lat=$studentLatitude, lon=$studentLongitude")
                        } else {
                            android.util.Log.w("StudentActivity", "Could not get student location")
                            runOnUiThread {
                                Toast.makeText(this@StudentActivity, "Konum alınamadı. Yoklama alınamayabilir.", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        android.util.Log.w("StudentActivity", "Location services disabled")
                        runOnUiThread {
                            Toast.makeText(this@StudentActivity, "Konum servisleri kapalı. Lütfen açın.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    android.util.Log.w("StudentActivity", "Location permission not granted")
                    runOnUiThread {
                        Toast.makeText(this@StudentActivity, "Konum izni verilmedi. Yoklama alınamayabilir.", Toast.LENGTH_LONG).show()
                    }
                }
                
                // Girişte gelen email'i kullan (server email'den profile id çözer)
                val email = intent.getStringExtra("email") ?: ""
                apiService.lastError = null // Reset error
                val ok = apiService.validateQRCode(qrData, email, studentLatitude, studentLongitude) == true

                runOnUiThread {
                    if (ok) {
                        hasAttended = true
                        binding.tvStatus.text = "Yoklama alındı!"
                        binding.btnStartScan.isEnabled = false
                        binding.btnStartScan.text = "Yoklama Alındı"
                        Toast.makeText(this@StudentActivity, "Yoklama başarıyla alındı!", Toast.LENGTH_LONG).show()
                    } else {
                        val errorMsg = apiService.lastError ?: "QR kod geçersiz, süresi dolmuş veya konum uygun değil"
                        binding.tvStatus.text = errorMsg
                        Toast.makeText(this@StudentActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.tvStatus.text = "Hata: ${e.message}"
                    Toast.makeText(this@StudentActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

