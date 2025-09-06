package app.chesspresso.screens.lobby

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.chesspresso.model.lobby.GameTime
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.viewmodel.QuickMatchViewModel

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
            CoffeeCard(
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
                    CoffeeText(
                        text = "Suche nach Gegner..."
                    )
                    CoffeeText(
                        text = "Spielzeit: ${selectedGameTime.displayName}"
                    )

                    CoffeeButton(
                        onClick = { viewModel.cancelSearch() },
                        error = true,
                        content = {
                            Text("Suche abbrechen")
                        }
                    )
                }
            }
        } else {
            CoffeeHeadlineText(
                text = "Quick Match",
                modifier = Modifier.padding(bottom = 8.dp)
                    .align(Alignment.CenterHorizontally)
            )
            // Spielzeit-Auswahl
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoffeeText(
                        text = "Spielzeit wählen:"
                    )
                    GameTimeSelectionRow(
                        selectedGameTime = selectedGameTime,
                        onGameTimeSelected = { selectedGameTime = it }
                    )
                }
            }

            // Fehleranzeige
            error?.let { errorMessage ->
                CoffeeCard(
                    modifier = Modifier.fillMaxWidth(),
                    // Optional: Farbe anpassen
                ) {
                    CoffeeText(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Spiel starten Button
            CoffeeButton(
                onClick = { viewModel.joinQuickMatch(selectedGameTime) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                content = {
                    Text("Spiel suchen")
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            )
        }
    }
}

@Composable
fun GameTimeSelectionRow(
    selectedGameTime: GameTime,
    onGameTimeSelected: (GameTime) -> Unit,
    allowedTimes: List<GameTime> = GameTime.entries.filter { it != GameTime.UNLIMITED }
) {
    val cardSize: Dp = 80.dp
    val borderWidth = 3.dp
    val defaultBorderColor = MaterialTheme.colorScheme.primary
    val selectedBorderColor = MaterialTheme.colorScheme.secondary
    val cardShape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        allowedTimes.forEach { time ->
            val isSelected = selectedGameTime == time
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .size(cardSize)
                    .clickable { onGameTimeSelected(time) },
                shape = cardShape,
                border = BorderStroke(
                    width = borderWidth,
                    color = if (isSelected) selectedBorderColor else defaultBorderColor
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (time == GameTime.UNLIMITED) {
                        Text(
                            text = "∞",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        Text(
                            text = (time.seconds / 60).toString(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center)
                        )
                        Text(
                            text = "min",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(top = 48.dp)
                        )
                    }
                }
            }
        }
    }
}
