package app.chesspresso.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.chesspresso.ui.theme.CoffeeBrownContrast
import app.chesspresso.ui.theme.CoffeeBrownDark
import app.chesspresso.ui.theme.CoffeeCreme
import app.chesspresso.ui.theme.CoffeeGreen
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeOrange
import app.chesspresso.ui.theme.CoffeeRust
import app.chesspresso.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import app.chesspresso.ui.theme.CoffeeBrownSoft
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeText

@Composable
fun StatsScreen(
    navController: NavController,
    gameViewModel: GameViewModel = hiltViewModel(),
) {
    val uiState by gameViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Stats und GameHistory beim Start laden
    LaunchedEffect(Unit) {
        gameViewModel.loadStats()
        gameViewModel.loadGameHistory()
    }

    // Snackbar für Nachrichten
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            gameViewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            gameViewModel.clearMessages()
        }
    }

    // Fehler aus GameHistory anzeigen
    LaunchedEffect(uiState.historyErrorMessage) {
        uiState.historyErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            gameViewModel.clearHistoryMessages()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CoffeeHeadlineText(
                text = "Meine Statistiken"
            )

            // Stats Karte
            CoffeeCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    uiState.stats?.let { stats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatItem(
                                icon = Icons.Filled.EmojiEvents,
                                iconTint = CoffeeOrange,
                                longLabel = "Siege",
                                value = stats.wins.toString(),
                                circleColor = CoffeeBrownSoft,
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                icon = Icons.Filled.Close,
                                iconTint = CoffeeRust,
                                longLabel = "Niederlagen",
                                value = stats.losses.toString(),
                                circleColor = CoffeeBrownContrast,
                                modifier = Modifier.weight(1f)
                            )
                            StatItem(
                                icon = Icons.Filled.Remove,
                                iconTint = CoffeeGreen, // sanftes Grün für Unentschieden
                                longLabel = "Unentschieden",
                                value = stats.draws.toString(),
                                circleColor = CoffeeCreme, // Theme-Creme für Unentschieden
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } ?: CoffeeText(
                        text = "Keine Statistiken verfügbar",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game History Titel
            CoffeeHeadlineText(
                text = "Letzte Spiele",
                modifier = Modifier.padding(bottom = 8.dp),
                fontSizeSp = 22
            )

            // Game History Liste
            when {
                uiState.isHistoryLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.gameHistory.isNullOrEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CoffeeText(
                            text = "Keine Spiele gefunden.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Nimmt den restlichen verfügbaren Platz ein
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.gameHistory!!) { game ->
                            CoffeeCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("game_detail/${game.id}") }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    CoffeeText(
                                        text = "Datum: " + (game.startedAt.takeIf { it.isNotBlank() }
                                            ?.let { formatDate(it) } ?: "Unbekannt")
                                    )
                                    CoffeeText(
                                        text = "Ergebnis: ${game.result ?: "Unbekannt"}"
                                    )
                                    CoffeeText(
                                        text = "Züge: ${game.moves.size}"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    longLabel: String,
    value: String,
    circleColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = circleColor,
            tonalElevation = 4.dp,
            modifier = Modifier.size(58.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CoffeeText(
                    text = value,
                    color = Color.White,
                    fontSizeSp = 30
                )
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = longLabel,
            tint = iconTint,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp).size(25.dp)
        )
        CoffeeText(
            text = longLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSizeSp = 16
        )
    }
}

// formatDate Funktion aus GameHistoryScreen.kt hierher kopiert
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) outputFormat.format(date) else dateString
    } catch (_: Exception) {
        dateString
    }
}
