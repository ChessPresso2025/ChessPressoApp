package app.chesspresso.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeText

@Composable
fun LobbyCreatorControls(
    lobbyId: String,
    modifier: Modifier = Modifier
) {
    var showQRCode by remember { mutableStateOf(false) }

    CoffeeCard(
        modifier = modifier.fillMaxWidth(),
        content = {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoffeeText(
                    text = "Lobby teilen"
                )

                Spacer(modifier = Modifier.height(16.dp))

                CoffeeButton(
                    onClick = { showQRCode = !showQRCode },
                    modifier = Modifier.fillMaxWidth(),
                    content = {
                        Icon(
                            imageVector = if (showQRCode) Icons.Default.Close else Icons.Outlined.QrCode,
                            contentDescription = if (showQRCode) "QR-Code ausblenden" else "QR-Code anzeigen"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (showQRCode) "QR-Code ausblenden" else "QR-Code anzeigen")
                    }
                )

                if (showQRCode) {
                    Spacer(modifier = Modifier.height(20.dp))
                    QRCodeDisplay(lobbyId = lobbyId)
                }
            }
        },
    )
}
