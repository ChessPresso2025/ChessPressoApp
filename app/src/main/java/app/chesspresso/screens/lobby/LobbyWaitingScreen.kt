package app.chesspresso.screens.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.chesspresso.model.lobby.GameDuration
import app.chesspresso.viewmodel.PrivateLobbyViewModel

@Composable
fun LobbyWaitingScreen(
    lobbyCode: String,
    onGameStart: (String) -> Unit,
    viewModel: PrivateLobbyViewModel = hiltViewModel()
) {
    val currentLobby by viewModel.currentLobby.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()
    val error by viewModel.lobbyError.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedGameDuration by remember { mutableStateOf(GameDuration.MEDIUM) }
    var selectedWhitePlayer by remember { mutableStateOf("") }
    var randomColors by remember { mutableStateOf(true) }

    // Automatische Navigation bei Spielstart
    LaunchedEffect(gameStarted) {
        gameStarted?.let { gameStart ->
            onGameStart(gameStart.lobbyId)
            viewModel.clearGameStart()
        }
    }

    // Lobby-Info beim Laden des Screens abrufen
    LaunchedEffect(lobbyCode) {
        viewModel.refreshLobbyInfo(lobbyCode)
    }

    // Regelmäßige Aktualisierung der Lobby-Info alle 3 Sekunden
    LaunchedEffect(lobbyCode) {
        while (true) {
            kotlinx.coroutines.delay(3000) // 3 Sekunden warten
            viewModel.refreshLobbyInfo(lobbyCode)
        }
    }

    // Spieler-Namen aktualisieren wenn Lobby geladen wird
    LaunchedEffect(currentLobby) {
        currentLobby?.let { lobby ->
            if (lobby.players.isNotEmpty()) {
                selectedWhitePlayer = lobby.players.first()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Große Überschrift mit Lobby-Code
        Text(
            text = "Deine Lobby: $lobbyCode",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        // Info-Card für den Lobby-Code
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Gib deinem Freund diesen Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sobald dein Freund der Lobby beitritt, kann das Spiel beginnen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Spieler-Status Card
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Spieler (${currentLobby?.players?.size ?: 1}/2)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                currentLobby?.let { lobby ->
                    lobby.players.forEachIndexed { index, player ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Spieler ${index + 1}${if (player == lobby.creator) " (Ersteller)" else ""}",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                if (currentLobby?.players?.size != 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Warte auf weiteren Spieler...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Fehleranzeige
        error?.let { errorMessage ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
