package app.chesspresso.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.chesspresso.model.lobby.GameEndResponse
import app.chesspresso.screens.main.NavRoutes
import app.chesspresso.viewmodel.ChessGameViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import app.chesspresso.viewmodel.RematchDialogState

@Composable
fun GameOverScreen(
    gameEndResponse: GameEndResponse,
    playerId: String,
    navController: NavHostController,
    viewModel: ChessGameViewModel
) {
    val lobbyId = gameEndResponse.lobbyId
    val stompWebSocketService = viewModel.webSocketService

    androidx.compose.runtime.LaunchedEffect(lobbyId) {
        android.util.Log.d("GameOverScreen", "LaunchedEffect: subscribeToRematchOffer wird aufgerufen mit lobbyId=$lobbyId")
        stompWebSocketService.subscribeToRematchOffer(lobbyId)
    }
    androidx.compose.runtime.DisposableEffect(lobbyId) {
        onDispose {
            android.util.Log.d("GameOverScreen", "DisposableEffect: unsubscribeFromRematchOffer wird aufgerufen mit lobbyId=$lobbyId")
            stompWebSocketService.unsubscribeFromRematchOffer(lobbyId)
        }
    }
    val ergebnisText = when {
        gameEndResponse.draw -> "Unentschieden"
        playerId == gameEndResponse.winner -> "Gewonnen"
        playerId == gameEndResponse.loser -> "Verloren"
        else -> "Unbekannt"
    }
    val rematchDialogState by viewModel.rematchDialogState.collectAsStateWithLifecycle()

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

        // Rematch-Dialoge
        when (rematchDialogState) {
            is RematchDialogState.WaitingForResponse -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch angefragt") },
                    text = { Text("Warte auf Antwort des Gegners...") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearRematchDialog() }) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
            is RematchDialogState.OfferReceived -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch erhalten") },
                    text = { Text("Dein Gegner möchte ein Rematch. Annehmen?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.respondRematch(true)
                        }) { Text("Annehmen") }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            viewModel.respondRematch(false)
                        }) { Text("Ablehnen") }
                    }
                )
            }
            is RematchDialogState.WaitingForResult -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Antwort gesendet") },
                    text = { Text("Warte auf Bestätigung...") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.clearRematchDialog() }) {
                            Text("Schließen")
                        }
                    }
                )
            }
            is RematchDialogState.Accepted -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch angenommen") },
                    text = { Text("Das Rematch startet jetzt!") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.clearRematchDialog()
                            val isPrivate = gameEndResponse.lobbyId.length <= 6
                            if (isPrivate) {
                                navController.navigate(NavRoutes.PRIVATE_LOBBY) {
                                    popUpTo(NavRoutes.HOME) { inclusive = false }
                                }
                            } else {
                                navController.navigate("game/${gameEndResponse.lobbyId}") {
                                    popUpTo(NavRoutes.HOME) { inclusive = false }
                                }
                            }
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
            is RematchDialogState.Declined -> {
                AlertDialog(
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = { Text("Rematch abgelehnt") },
                    text = { Text("Der Gegner hat das Rematch abgelehnt. Du wirst zum Startbildschirm zurückgeleitet.") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.clearRematchDialog()
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(0) { inclusive = true }
                            }
                        }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> {}
        }

        Button(onClick = {
            viewModel.closeLobby(gameEndResponse.lobbyId)
            navController.navigate(NavRoutes.HOME) {
                popUpTo(0) { inclusive = true }
            }
        }) {
            Text("Zurück")
        }
        Button(onClick = {
            viewModel.requestRematch()
        }) {
            Text("Rematch")
        }
    }
}