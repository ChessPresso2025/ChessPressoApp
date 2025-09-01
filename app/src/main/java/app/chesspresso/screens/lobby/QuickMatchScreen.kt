package app.chesspresso.screens.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.chesspresso.model.lobby.GameTime
import app.chesspresso.viewmodel.QuickMatchViewModel
import app.chesspresso.viewmodel.RematchDialogState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMatchScreen(
    onGameStart: (String) -> Unit,
    viewModel: QuickMatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isWaiting by viewModel.isWaitingForMatch.collectAsStateWithLifecycle()
    val error by viewModel.lobbyError.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()
    val rematchDialogState by viewModel.rematchDialogState.collectAsStateWithLifecycle()

    var selectedGameTime by remember { mutableStateOf(GameTime.MIDDLE) }

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
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Suche nach Gegner...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Spielzeit: ${selectedGameTime.displayName}",
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

                    GameTime.entries.filter { it != GameTime.UNLIMITED }.forEach { gameTime ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedGameTime == gameTime,
                                    onClick = { selectedGameTime = gameTime }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedGameTime == gameTime,
                                onClick = { selectedGameTime = gameTime }
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
                onClick = { viewModel.joinQuickMatch(selectedGameTime) },
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

        // Rematch-Dialoge
        when (rematchDialogState) {
            is RematchDialogState.WaitingForResponse -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch angefragt") },
                    text = { Text("Warte auf Antwort des Gegners...") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearRematchDialog() }) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
            is RematchDialogState.OfferReceived -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch erhalten") },
                    text = { Text("Dein Gegner möchte ein Rematch. Annehmen?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.respondRematch(true)
                        }) { Text("Annehmen") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            viewModel.respondRematch(false)
                        }) { Text("Ablehnen") }
                    }
                )
            }
            is RematchDialogState.WaitingForResult -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Antwort gesendet") },
                    text = { Text("Warte auf Bestätigung...") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearRematchDialog() }) {
                            Text("Schließen")
                        }
                    }
                )
            }
            is RematchDialogState.Accepted -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch angenommen") },
                    text = { Text("Das Rematch startet jetzt!") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearRematchDialog() }) {
                            Text("OK")
                        }
                    }
                )
                // TODO: Hier ggf. Navigation ins neue Spiel auslösen
            }
            is RematchDialogState.Declined -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch abgelehnt") },
                    text = { Text("Der Gegner hat das Rematch abgelehnt. Du wirst zum Startbildschirm zurückgeleitet.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.clearRematchDialog()
                            // TODO: Navigation zum Startscreen auslösen
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> {}
        }
    }
}