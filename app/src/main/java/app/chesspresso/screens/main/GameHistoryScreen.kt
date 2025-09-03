package app.chesspresso.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.navigation.NavController
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun GameHistoryScreen(
    navController: NavController,
    gameViewModel: GameViewModel // kein Default mehr!
) {
    val uiState by gameViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Historie beim Start laden
    LaunchedEffect(Unit) {
        gameViewModel.loadGameHistory()
    }

    // Fehler anzeigen
    LaunchedEffect(uiState.historyErrorMessage) {
        uiState.historyErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            gameViewModel.clearHistoryMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isHistoryLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.gameHistory.isNullOrEmpty() -> {
                CoffeeText(
                    text = "Keine Spiele gefunden.",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

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
