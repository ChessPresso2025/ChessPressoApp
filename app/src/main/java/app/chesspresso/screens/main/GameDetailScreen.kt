package app.chesspresso.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@Composable
fun GameDetailScreen(
    navController: NavController,
    gameId: String,
    gameViewModel: GameViewModel
) {
    val uiState by gameViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hole das Spiel aus der Historie (Vergleich als UUID)
    val game = uiState.gameHistory?.find { it.id == runCatching { UUID.fromString(gameId) }.getOrNull() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (game == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoffeeText(
                    text = "Spiel nicht gefunden.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                CoffeeText(
                    text = "Übergebene gameId: $gameId"
                )
                CoffeeText(
                    text = "Vorhandene IDs:"
                )
                uiState.gameHistory?.forEach {
                    CoffeeText(
                        text = it.id.toString()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoffeeHeadlineText(
                    text = "Partiedetails",
                    textAlign = TextAlign.Center
                )
                CoffeeText(
                    text = "Datum: " + (game.startedAt.takeIf { it.isNotBlank() }?.let { formatDate(it) } ?: "Unbekannt")
                )
                CoffeeText(
                    text = "Ergebnis: ${game.result ?: "Unbekannt"}"
                )
                Spacer(modifier = Modifier.height(8.dp))
                CoffeeText(
                    text = "Züge:"
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(game.moves) { move ->
                        CoffeeCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CoffeeText(
                                    text = "${move.moveNumber}.",
                                    modifier = Modifier.width(32.dp)
                                )
                                CoffeeText(
                                    text = move.moveNotation
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                CoffeeText(
                                    text = move.createdAt.takeIf { it.isNotBlank() }?.let { formatDate(it) } ?: ""
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
    // Kompatibel mit API 24: ISO-String nach SimpleDateFormat parsen
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) outputFormat.format(date) else dateString
    } catch (_: Exception) {
        dateString
    }
}
