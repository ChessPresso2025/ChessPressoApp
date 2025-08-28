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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.chesspresso.viewmodel.PrivateLobbyViewModel
import app.chesspresso.ui.components.LobbyCreatorControls
import app.chesspresso.ui.components.QRScannerButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateLobbyScreen(
    onLobbyCreated: (String) -> Unit,
    onLobbyJoined: (String) -> Unit,
    viewModel: PrivateLobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val error by viewModel.lobbyError.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Navigation bei erfolgreichem Lobby-Erstellen/Beitreten
    LaunchedEffect(uiState.isLobbyCreated, uiState.createdLobbyCode) {
        if (uiState.isLobbyCreated && uiState.createdLobbyCode != null) {
            onLobbyCreated(uiState.createdLobbyCode!!)
        }
    }

    LaunchedEffect(uiState.isLobbyJoined, uiState.joinedLobbyCode) {
        if (uiState.isLobbyJoined && uiState.joinedLobbyCode != null) {
            onLobbyJoined(uiState.joinedLobbyCode!!)
        }
    }

    // Automatische Navigation bei Spielstart
    LaunchedEffect(gameStarted) {
        gameStarted?.let { gameStart ->
            // Navigation zum Spiel
            viewModel.clearGameStart()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Lobby erstellen
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Neue Lobby erstellen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Erstelle eine private Lobby und teile den Code mit deinem Freund.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.createPrivateLobby() },
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
                    Text("Lobby erstellen")
                }
            }
        }

        // QR-Code für erstellte Lobby anzeigen
        uiState.createdLobbyCode?.let { lobbyCode ->
            LobbyCreatorControls(
                lobbyId = lobbyCode
            )
        }

        // Trennlinie
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
            Text(
                text = "ODER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
        }

        // Lobby beitreten
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Lobby beitreten",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gib den 6-stelligen Lobby-Code ein, den du von deinem Freund erhalten hast.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = uiState.joinCode,
                    onValueChange = viewModel::updateJoinCode,
                    label = { Text("Lobby-Code") },
                    placeholder = { Text("ABC123") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            if (uiState.joinCode.length == 6) {
                                keyboardController?.hide()
                                viewModel.joinPrivateLobby(uiState.joinCode)
                            }
                        }
                    ),
                    supportingText = {
                        Text("${uiState.joinCode.length}/6 Zeichen")
                    }
                )

                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.joinPrivateLobby(uiState.joinCode)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading && uiState.joinCode.length == 6
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Lobby beitreten")
                }
            }
        }

        // QR-Code Scanner für Lobby beitreten
        QRScannerButton(
            onLobbyScanned = { lobbyId ->
                // Setze den gescannten Lobby-Code und trete der Lobby bei
                viewModel.updateJoinCode(lobbyId)
                viewModel.joinPrivateLobby(lobbyId)
            },
            enabled = !uiState.isLoading
        )

        // Fehleranzeige
        error?.let { errorMessage ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.error?.let { errorMessage ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
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
