package app.chesspresso.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.chesspresso.model.lobby.GameEndResponse

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