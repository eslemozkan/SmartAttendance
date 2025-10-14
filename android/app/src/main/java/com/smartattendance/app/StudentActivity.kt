package com.smartattendance.app

import android.Manifest
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
        binding.tvStatus.text = "Processing QR code..."
        
        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiService.validateQRCode(qrData, "student-${System.currentTimeMillis()}")
                }
                
                runOnUiThread {
                    when {
                        result == true -> {
                            binding.tvStatus.text = "Attendance marked successfully!"
                            Toast.makeText(this@StudentActivity, "Attendance recorded!", Toast.LENGTH_LONG).show()
                        }
                        else -> {
                            binding.tvStatus.text = "Failed to mark attendance"
                            Toast.makeText(this@StudentActivity, "Error marking attendance", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.tvStatus.text = "Error: ${e.message}"
                    Toast.makeText(this@StudentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
