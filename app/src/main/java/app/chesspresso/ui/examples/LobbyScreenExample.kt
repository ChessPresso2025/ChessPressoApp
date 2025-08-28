package app.chesspresso.ui.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.chesspresso.ui.components.LobbyCreatorControls
import app.chesspresso.ui.components.QRScannerButton

@Composable
fun LobbyScreenExample(
    isLobbyCreator: Boolean,
    lobbyId: String?,
    onJoinLobby: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Private Lobby",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        if (isLobbyCreator && lobbyId != null) {
            // Für Lobby-Ersteller: QR-Code anzeigen
            LobbyCreatorControls(
                lobbyId = lobbyId
            )
        } else {
            // Für andere Spieler: QR-Code scannen
            QRScannerButton(
                onLobbyScanned = { scannedLobbyId ->
                    onJoinLobby(scannedLobbyId)
                }
            )
        }

        // Weitere Lobby-Informationen...
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Lobby-Informationen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (lobbyId != null) {
                    Text("Lobby-ID: $lobbyId")
                    Text("Status: ${if (isLobbyCreator) "Ersteller" else "Teilnehmer"}")
                } else {
                    Text("Noch keiner Lobby beigetreten")
                }
            }
        }
    }
}
