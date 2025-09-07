package app.chesspresso.ui.qr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.chesspresso.ui.theme.ChessPressoAppTheme
import app.chesspresso.ui.theme.CoffeeText
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QRScannerActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            setContent {
                ChessPressoAppTheme {
                    QRScannerScreen(
                        onQRCodeScanned = { qrData ->
                            handleScannedData(qrData)
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        } else {
            Toast.makeText(this, "Kamera-Berechtigung ist erforderlich für QR-Code Scanning", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Prüfe Kamera-Berechtigung
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // Berechtigung bereits erteilt
                setContent {
                    ChessPressoAppTheme {
                        QRScannerScreen(
                            onQRCodeScanned = { qrData ->
                                handleScannedData(qrData)
                            },
                            onCancel = {
                                finish()
                            }
                        )
                    }
                }
            }
            else -> {
                // Berechtigung anfordern
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun handleScannedData(data: String) {
        val intent = Intent()
        intent.putExtra("scanned_data", data)
        setResult(RESULT_OK, intent)
        finish()
    }
}

@Composable
fun QRScannerScreen(
    onQRCodeScanned: (String) -> Unit,
    onCancel: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isScanning by remember { mutableStateOf(true) }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Kamera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { imageAnalysis ->
                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (isScanning) {
                                    scanQRCode(imageProxy) { result ->
                                        if (result != null) {
                                            isScanning = false
                                            onQRCodeScanned(result)
                                        }
                                    }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (exc: Exception) {
                        // Fehlerbehandlung
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoffeeText(
                        text = "QR-Code scannen",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )

                    IconButton(
                        onClick = onCancel
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = Color.White
                        )
                    }
                }
            }

            // Bottom instruction
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                CoffeeText(
                    text = "Richte die Kamera auf den QR-Code",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Scanning frame overlay
        ScanningFrame()
    }
}

@Composable
fun ScanningFrame() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .padding(16.dp)
        ) {
            // Scanning frame corners
            // Top-left corner
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .offset((-8).dp, (-8).dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(4.dp)
                        .height(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
                Card(
                    modifier = Modifier
                        .width(30.dp)
                        .height(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }

            // Top-right corner
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .offset(228.dp, (-8).dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(4.dp)
                        .height(30.dp)
                        .offset(26.dp, 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
                Card(
                    modifier = Modifier
                        .width(30.dp)
                        .height(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }

            // Bottom-left corner
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .offset((-8).dp, 228.dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(4.dp)
                        .height(30.dp)
                        .offset(0.dp, (-26).dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
                Card(
                    modifier = Modifier
                        .width(30.dp)
                        .height(4.dp)
                        .offset(0.dp, 26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }

            // Bottom-right corner
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .offset(228.dp, 228.dp)
            ) {
                Card(
                    modifier = Modifier
                        .width(4.dp)
                        .height(30.dp)
                        .offset(26.dp, (-26).dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
                Card(
                    modifier = Modifier
                        .width(30.dp)
                        .height(4.dp)
                        .offset(0.dp, 26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun scanQRCode(
    imageProxy: ImageProxy,
    onResult: (String?) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        barcode.rawValue?.let { value ->
                            onResult(value)
                            return@addOnSuccessListener
                        }
                    }
                }
                onResult(null)
            }
            .addOnFailureListener {
                onResult(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
        onResult(null)
    }
}
