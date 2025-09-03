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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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
            CoffeeText(
                text = "Statistiken",
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Karte
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoffeeText(
                        text = "Meine Statistiken"
                    )

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
                                StatItem("Siege", stats.wins.toString())
                                StatItem("Niederlagen", stats.losses.toString())
                                StatItem("Unentschieden", stats.draws.toString())
                            }
                        } ?: CoffeeText(
                            text = "Keine Statistiken verfügbar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Game History Titel
            CoffeeText(
                text = "Letzte Spiele",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Game History Liste
            when {
                uiState.isHistoryLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.gameHistory.isNullOrEmpty() -> {
                     Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
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
                                        text = "Datum: " + (game.startedAt.takeIf { it.isNotBlank() }?.let { formatDate(it) } ?: "Unbekannt")
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
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoffeeText(
            text = value,
            color = MaterialTheme.colorScheme.primary
        )
        CoffeeText(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
