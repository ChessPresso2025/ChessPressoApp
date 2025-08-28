package app.chesspresso.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LobbyCreatorControls(
    lobbyId: String,
    modifier: Modifier = Modifier
) {
    var showQRCode by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Lobby teilen",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showQRCode = !showQRCode },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (showQRCode) Icons.Default.Close else Icons.Default.Settings,
                    contentDescription = if (showQRCode) "QR-Code ausblenden" else "QR-Code anzeigen"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (showQRCode) "QR ausblenden" else "QR anzeigen")
            }

            if (showQRCode) {
                Spacer(modifier = Modifier.height(20.dp))
                QRCodeDisplay(lobbyId = lobbyId)
            }
        }
    }
}
