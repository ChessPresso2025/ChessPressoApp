package app.chesspresso.utils

import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import app.chesspresso.ui.qr.QRScannerActivity

class QRScannerHelper(
    private val activity: FragmentActivity,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit = { error ->
        Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
    }
) {

    private lateinit var permissionManager: PermissionManager

    private val qrScannerLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                FragmentActivity.RESULT_OK -> {
                    val scannedData = result.data?.getStringExtra("scanned_data")
                    if (scannedData != null) {
                        onResult(scannedData)
                    } else {
                        onError("Fehler beim Scannen des QR-Codes")
                    }
                }
                FragmentActivity.RESULT_CANCELED -> {
                    onError("QR-Code Scan abgebrochen")
                }
                else -> {
                    onError("Unbekannter Fehler beim QR-Code Scan")
                }
            }
        }

    init {
        permissionManager = PermissionManager(
            activity = activity,
            onPermissionGranted = { startQRScanner() },
            onPermissionDenied = { onError("Kamera-Berechtigung verweigert") }
        )
    }

    fun scanQRCode() {
        permissionManager.requestCameraPermission()
    }

    private fun startQRScanner() {
        try {
            val intent = Intent(activity, QRScannerActivity::class.java)
            qrScannerLauncher.launch(intent)
        } catch (e: Exception) {
            onError("Fehler beim Starten des QR-Scanners: ${e.message}")
        }
    }
}
