package app.chesspresso.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.chesspresso.model.lobby.GameEndResponse
import androidx.compose.foundation.lazy.items

@Composable
fun GameOverScreen(gameEndResponse: GameEndResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Spiel beendet", style = MaterialTheme.typography.headlineMedium)
        Text("Lobby-ID: ${gameEndResponse.lobbyId}")
        Text("Ergebnis: ${gameEndResponse.result}")
        Text("Grund: ${gameEndResponse.reason}")
        Text("Endstellung:")

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            //durch  Chessboard(board = gameEndResponse.finalBoard) ersetzen wenn Chessboard Composable fertig ist
            items(gameEndResponse.finalBoard.entries.toList()) { entry ->
                Text("${entry.key}: ${entry.value}")
            }
        }
        Button(onClick = { /* Zurück zum Hauptmenü */ }) {
            Text("Zurück")
        }
    }
}