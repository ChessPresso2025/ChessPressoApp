package app.chesspresso.screens.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.chesspresso.model.lobby.GameDuration
import app.chesspresso.viewmodel.QuickMatchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMatchScreen(
    onBackClick: () -> Unit,
    onGameStart: (String) -> Unit,
    viewModel: QuickMatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWaiting by viewModel.isWaitingForMatch.collectAsStateWithLifecycle()
    val error by viewModel.lobbyError.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()

    var selectedGameDuration by remember { mutableStateOf(GameDuration.MEDIUM) }

    // Automatische Navigation bei Spielstart
    LaunchedEffect(gameStarted) {
        gameStarted?.let { gameStart ->
            onGameStart(gameStart.lobbyId)
            viewModel.clearGameStart()
        }
    }

    // Fehler anzeigen
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Hier könnte ein Snackbar oder Dialog angezeigt werden
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isWaiting) {
            // Wartezustand
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Suche nach Gegner...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Spielzeit: ${selectedGameDuration.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { viewModel.cancelSearch() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Suche abbrechen")
                    }
                }
            }
        } else {
            // Spielzeit-Auswahl
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Spielzeit wählen:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    GameDuration.entries.filter { it != GameDuration.UNLIMITED }.forEach { gameTime ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedGameDuration == gameTime,
                                    onClick = { selectedGameDuration = gameTime }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedGameDuration == gameTime,
                                onClick = { selectedGameDuration = gameTime }
                            )
                            Text(
                                text = gameTime.displayName,
                                modifier = Modifier.padding(start = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
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

            // Spiel starten Button
            Button(
                onClick = { viewModel.joinQuickMatch(selectedGameDuration) },
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
                Text("Spiel suchen")
            }
        }
    }
}
