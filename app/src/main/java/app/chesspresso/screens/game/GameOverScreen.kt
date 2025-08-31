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
import androidx.navigation.NavHostController
import app.chesspresso.model.lobby.GameEndResponse
import app.chesspresso.screens.main.NavRoutes

@Composable
fun GameOverScreen(gameEndResponse: GameEndResponse, playerId: String, navController: NavHostController) {
    val ergebnisText = when {
        gameEndResponse.draw == true -> "Unentschieden"
        playerId == gameEndResponse.winner -> "Gewonnen"
        playerId == gameEndResponse.loser -> "Verloren"
        else -> "Unbekannt"
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text("Spiel beendet", style = MaterialTheme.typography.headlineMedium)
        Text("Lobby-ID: ${gameEndResponse.lobbyId}")
        Text("Ergebnis: $ergebnisText")
        Text("Endstellung:")


        Button(onClick = { navController.navigate(NavRoutes.HOME)}) {
            Text("Zurück")
        }
        Button(onClick = { /* TODO: Rematch-Logik */ }) {
            Text("Rematch")
        }
    }
}