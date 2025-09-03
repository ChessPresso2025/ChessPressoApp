package app.chesspresso.screens.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun RematchDialogsHandler(viewModel: ChessGameViewModel, gameEndResponse: GameEndResponse, navController: NavHostController?) {
    val rematchDialogState = viewModel.rematchDialogState.collectAsStateWithLifecycle().value
    when (rematchDialogState) {
        is RematchDialogState.WaitingForResponse -> {
            RematchDialog(
                show = true,
                onDismissRequest = { viewModel.clearRematchDialog() },
                title = "Rematch angefragt",
                text = "Warte auf Antwort des Gegners...",
                confirmButtonText = "Abbrechen",
                onConfirm = {
                    viewModel.clearRematchDialog() }
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
                onConfirm = {
                    viewModel.clearRematchDialog() }
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
                        navController!!.navigate("lobby_waiting/${viewModel.rematchResult.value!!.newlobbyid}/$isCreator") {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                        }
                    } else {
                        navController!!.navigate("game/${gameEndResponse.lobbyId}") {
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
                text = "Der Gegner hat das Rematch abgelehnt.",
                confirmButtonText = "OK",
                onConfirm = {
                    viewModel.unsubscribeFromLobbyAndGame()
                    viewModel.resetGameState()
                    viewModel.clearRematchDialog()
                    navController!!.navigate(NavRoutes.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        else -> null
    }
}

@Composable
fun GameOverResultInfo(
    gameEndResponse: GameEndResponse,
    playerId: String
) {
    val ergebnisText = when {
        gameEndResponse.draw -> "Unentschieden"
        playerId == gameEndResponse.winner -> "Sieg"
        playerId == gameEndResponse.loser -> "Niederlage"
        else -> "Unbekannt"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = ergebnisText,
            style = MaterialTheme.typography.displayMedium,
            color = when (ergebnisText) {
                "Sieg" -> Color(0xFF4CAF50)
                "Niederlage" -> Color(0xFFF44336)
                "Unentschieden" -> Color(0xFF9E9E9E)
                else -> MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = if (playerId == gameEndResponse.winner) "Du hast gewonnen!" else if (playerId == gameEndResponse.loser) "Du hast verloren." else "",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
fun GameOverActions(
    gameEndResponse: GameEndResponse,
    viewModel: ChessGameViewModel,
    navController: NavHostController? = null
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(onClick = { viewModel.requestRematch() }) {
            Text("Rematch")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = {
            viewModel.closeLobby(gameEndResponse.lobbyId)
            navController?.navigate(NavRoutes.HOME) {
                popUpTo(0) { inclusive = true }
            }
        }) {
            Text("Zurück")
        }
    }
}
