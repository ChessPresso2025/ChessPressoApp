package app.chesspresso.screens.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
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
import app.chesspresso.model.lobby.GameTime
import app.chesspresso.viewmodel.PrivateLobbyViewModel

@Composable
fun LobbyWaitingScreen(
    lobbyCode: String,
    onBackClick: () -> Unit,
    onGameStart: (String) -> Unit,
    viewModel: PrivateLobbyViewModel = hiltViewModel()
) {
    val currentLobby by viewModel.currentLobby.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()
    val error by viewModel.lobbyError.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedGameTime by remember { mutableStateOf(GameTime.MIDDLE) }
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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header (ohne Zurück-Button)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lobby: $lobbyCode",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Lobby-Code anzeigen
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lobby-Code:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = lobbyCode,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = MaterialTheme.typography.headlineLarge.letterSpacing * 2
                )
                Text(
                    text = "Teile diesen Code mit deinem Freund",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Spieler-Status
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Spieler (${currentLobby?.players?.size ?: 1}/2):",
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Warte auf zweiten Spieler...",
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Spiel-Konfiguration (nur für Lobby-Ersteller sichtbar)
        currentLobby?.let { lobby ->
            if (lobby.players.size == 2 && lobby.creator == lobby.players.firstOrNull()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Spiel-Einstellungen:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Spielzeit-Auswahl
                        Text(
                            text = "Spielzeit:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        GameTime.entries.forEach { gameTime ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selectedGameTime == gameTime,
                                        onClick = { selectedGameTime = gameTime }
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedGameTime == gameTime,
                                    onClick = { selectedGameTime = gameTime }
                                )
                                Text(
                                    text = gameTime.displayName,
                                    modifier = Modifier.padding(start = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        HorizontalDivider()

                        // Farb-Auswahl
                        Text(
                            text = "Farben:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = randomColors,
                                    onClick = { randomColors = true }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = randomColors,
                                onClick = { randomColors = true }
                            )
                            Text(
                                text = "Zufällig",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = !randomColors,
                                    onClick = { randomColors = false }
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = !randomColors,
                                onClick = { randomColors = false }
                            )
                            Text(
                                text = "Manuell auswählen",
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Manuelle Farbauswahl
                        if (!randomColors && lobby.players.size == 2) {
                            Text(
                                text = "Weiß spielt:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            lobby.players.forEachIndexed { index, player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = selectedWhitePlayer == player,
                                            onClick = { selectedWhitePlayer = player }
                                        )
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedWhitePlayer == player,
                                        onClick = { selectedWhitePlayer = player }
                                    )
                                    Text(
                                        text = "Spieler ${index + 1}${if (player == lobby.creator) " (Ersteller)" else ""}",
                                        modifier = Modifier.padding(start = 8.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val whitePlayerFinal = if (randomColors) null else selectedWhitePlayer
                                val blackPlayerFinal = if (randomColors) null else lobby.players.find { it != selectedWhitePlayer }

                                viewModel.configureAndStartGame(
                                    lobbyCode = lobbyCode,
                                    gameTime = selectedGameTime,
                                    whitePlayer = whitePlayerFinal,
                                    blackPlayer = blackPlayerFinal,
                                    randomColors = randomColors
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Spiel starten")
                        }
                    }
                }
            } else if (lobby.players.size == 2) {
                // Für nicht-Ersteller: Warten auf Spiel-Start
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = "Warte auf Spiel-Start...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Der Lobby-Ersteller konfiguriert das Spiel.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
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

        Spacer(modifier = Modifier.weight(1f))

        // Lobby verlassen Button
        OutlinedButton(
            onClick = {
                viewModel.leaveLobby()
                onBackClick()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Lobby verlassen")
        }
    }
}
