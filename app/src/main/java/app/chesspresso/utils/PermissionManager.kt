package app.chesspresso.utils

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity

class PermissionManager(
    private val activity: FragmentActivity,
    private val onPermissionGranted: () -> Unit,
    private val onPermissionDenied: () -> Unit
) {

    private val permissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onPermissionGranted()
            } else {
                handlePermissionDenied()
            }
        }

    fun requestCameraPermission() {
        when {
            PermissionHelper.hasCameraPermission(activity) -> {
                onPermissionGranted()
            }
            PermissionHelper.shouldShowCameraPermissionRationale(activity) -> {
                showPermissionRationale()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(activity)
            .setTitle("Kamera-Berechtigung erforderlich")
            .setMessage("Diese App benötigt Zugriff auf die Kamera, um QR-Codes zu scannen.")
            .setPositiveButton("Berechtigung erteilen") { _, _ ->
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Abbrechen") { _, _ ->
                onPermissionDenied()
            }
            .show()
    }

    private fun handlePermissionDenied() {
        if (!PermissionHelper.shouldShowCameraPermissionRationale(activity)) {
            showSettingsDialog()
        } else {
            onPermissionDenied()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(activity)
            .setTitle("Berechtigung dauerhaft verweigert")
            .setMessage("Bitte aktiviere die Kamera-Berechtigung in den App-Einstellungen.")
            .setPositiveButton("Einstellungen öffnen") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Abbrechen") { _, _ ->
                onPermissionDenied()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
}
