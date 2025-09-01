package app.chesspresso.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.chesspresso.model.TeamColor
import app.chesspresso.model.lobby.GameEndResponse
import app.chesspresso.screens.main.NavRoutes
import app.chesspresso.viewmodel.ChessGameViewModel
import app.chesspresso.viewmodel.RematchDialogState

@Composable
fun RematchDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String? = null,
    onDismiss: (() -> Unit)? = null
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(confirmButtonText)
                }
            },
            dismissButton = if (dismissButtonText != null && onDismiss != null) {
                {
                    TextButton(onClick = onDismiss) {
                        Text(dismissButtonText)
                    }
                }
            } else null
        )
    }
}

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
                RematchDialog(
                    show = true,
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = "Rematch angefragt",
                    text = "Warte auf Antwort des Gegners...",
                    confirmButtonText = "Abbrechen",
                    onConfirm = { viewModel.clearRematchDialog() }
                )
            }
            is RematchDialogState.OfferReceived -> {
                RematchDialog(
                    show = true,
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = "Rematch erhalten",
                    text = "Dein Gegner möchte ein Rematch. Annehmen?",
                    confirmButtonText = "Annehmen",
                    onConfirm = { viewModel.respondRematch(true) },
                    dismissButtonText = "Ablehnen",
                    onDismiss = { viewModel.respondRematch(false) }
                )
            }
            is RematchDialogState.WaitingForResult -> {
                RematchDialog(
                    show = true,
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = "Antwort gesendet",
                    text = "Warte auf Bestätigung...",
                    confirmButtonText = "Schließen",
                    onConfirm = { viewModel.clearRematchDialog() }
                )
            }
            is RematchDialogState.Accepted -> {
                RematchDialog(
                    show = true,
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = "Rematch angenommen",
                    text = "Das Rematch startet jetzt!",
                    confirmButtonText = "OK",
                    onConfirm = {
                        viewModel.clearRematchDialog()
                        val isPrivate = gameEndResponse.lobbyId.length <= 6
                        if (isPrivate) {
                            if(viewModel.rematchResult.value == null) {
                                android.util.Log.e(
                                    "GameOverScreen",
                                    "rematchResult ist null, kann nicht navigieren"
                                )
                                return@RematchDialog
                            }
                            val isCreator = viewModel.myColor.value == TeamColor.WHITE
                            navController.navigate("lobby_waiting/${viewModel.rematchResult.value!!.newlobbyid}/$isCreator") {
                                popUpTo(NavRoutes.HOME) { inclusive = false }
                            }
                        } else {
                            navController.navigate("game/${gameEndResponse.lobbyId}") {
                                popUpTo(NavRoutes.HOME) { inclusive = false }
                            }
                        }
                    }
                )
            }
            is RematchDialogState.Declined -> {
                RematchDialog(
                    show = true,
                    onDismissRequest = { viewModel.clearRematchDialog() },
                    title = "Rematch abgelehnt",
                    text = "Der Gegner hat das Rematch abgelehnt. Du wirst zum Startbildschirm zurückgeleitet.",
                    confirmButtonText = "OK",
                    onConfirm = {
                        viewModel.clearRematchDialog()
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            else -> null
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