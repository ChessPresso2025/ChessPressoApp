package app.chesspresso.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import app.chesspresso.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun GameDetailScreen(
    navController: NavController, // bleibt für Navigationserweiterung
    gameId: String,
    gameViewModel: GameViewModel // kein Default mehr!
) {
    val uiState by gameViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Hole das Spiel aus der Historie (direkter Vergleich, kein .toString())
    val game = uiState.gameHistory?.find { it.id.toString() == gameId }

    Box(modifier = Modifier.fillMaxSize()) {
        if (game == null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Spiel nicht gefunden.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Übergebene gameId: $gameId",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Vorhandene IDs:",
                    style = MaterialTheme.typography.bodySmall
                )
                uiState.gameHistory?.forEach {
                    Text(
                        text = it.id.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Partiedetails",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Datum: " + (game.startedAt.takeIf { it.isNotBlank() }?.let { formatDate(it) } ?: "Unbekannt"),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Ergebnis: ${game.result ?: "Unbekannt"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Züge:",
                    style = MaterialTheme.typography.titleMedium
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(game.moves) { move ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${move.moveNumber}.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = move.moveNotation,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = move.createdAt.takeIf { it.isNotBlank() }?.let { formatDate(it) } ?: "",
                                    style = MaterialTheme.typography.bodySmall
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
