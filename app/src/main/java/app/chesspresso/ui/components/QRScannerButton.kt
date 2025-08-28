package app.chesspresso.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.chesspresso.utils.QRCodeGenerator

@Composable
fun QRScannerButton(
    onLobbyScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Permission Launcher für Kamera-Berechtigung
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission erteilt - starte Scanner
            launchQRScanner(context, onLobbyScanned) { error ->
                showError = error
            }
        } else {
            showError = "Kamera-Berechtigung ist für das Scannen von QR-Codes erforderlich"
            showPermissionDialog = true
        }
    }

    // Activity Result Launcher für den QR-Scanner
    val qrScannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                val scannedData = result.data?.getStringExtra("scanned_data")
                if (scannedData != null) {
                    val lobbyId = QRCodeGenerator.parseLobbyQRCode(scannedData)
                    if (lobbyId != null) {
                        onLobbyScanned(lobbyId)
                    } else {
                        showError = "Ungültiger QR-Code. Bitte scanne einen gültigen Lobby-QR-Code."
                    }
                } else {
                    showError = "Fehler beim Scannen des QR-Codes"
                }
            }
            android.app.Activity.RESULT_CANCELED -> {
                // Benutzer hat abgebrochen - kein Fehler
            }
            else -> {
                showError = "Unbekannter Fehler beim QR-Code Scan"
            }
        }
    }

    // Funktion zum Starten des QR-Scanners
    fun startQRScanner() {
        val permission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(context, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                // Permission bereits erteilt
                try {
                    val intent = android.content.Intent(context, app.chesspresso.ui.qr.QRScannerActivity::class.java)
                    qrScannerLauncher.launch(intent)
                } catch (e: Exception) {
                    showError = "Fehler beim Starten des Scanners: ${e.message}"
                }
            }
            else -> {
                // Permission anfordern
                permissionLauncher.launch(permission)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lobby via QR-Code beitreten",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Scanne den QR-Code einer privaten Lobby mit der Kamera",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { startQRScanner() },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Kamera für QR-Code scannen",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kamera öffnen")
            }
        }
    }

    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Kamera-Berechtigung erforderlich") },
            text = {
                Text("Diese App benötigt Zugriff auf die Kamera, um QR-Codes zu scannen. Bitte erteile die Berechtigung in den App-Einstellungen.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        // Öffne App-Einstellungen
                        val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                ) {
                    Text("Einstellungen")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Error Display
    showError?.let { error ->
        LaunchedEffect(error) {
            // Error wird für 4 Sekunden angezeigt, dann automatisch ausgeblendet
            kotlinx.coroutines.delay(4000)
            showError = null
        }

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = error,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun launchQRScanner(
    context: android.content.Context,
    onLobbyScanned: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val intent = android.content.Intent(context, app.chesspresso.ui.qr.QRScannerActivity::class.java)
        if (context is androidx.activity.ComponentActivity) {
            context.startActivity(intent)
        } else {
            onError("Scanner kann nicht gestartet werden")
        }
    } catch (e: Exception) {
        onError("Fehler beim Starten des Scanners: ${e.message}")
    }
}
