package app.chesspresso.screens.game

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.chesspresso.model.TeamColor
import app.chesspresso.model.board.Board
import app.chesspresso.model.lobby.GameStartResponse
import app.chesspresso.viewmodel.ChessGameViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(
    gameStartResponse: GameStartResponse,
    viewModel: ChessGameViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val board = remember { Board() }

    // Collect ViewModel states
    val currentBoard by viewModel.currentBoard.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val currentGameState by viewModel.currentGameState.collectAsState()
    val whiteTime by viewModel.whiteTime.collectAsState()
    val blackTime by viewModel.blackTime.collectAsState()
    val myColor by viewModel.myColor.collectAsState()
    val possibleMoves by viewModel.possibleMoves.collectAsState()

    // Determine which board state to use (current or initial)
    val boardToDisplay = currentBoard.ifEmpty { gameStartResponse.board }
    val activePlayer = currentPlayer ?: TeamColor.WHITE


    LaunchedEffect(myColor) {
        if (myColor != null) {
            Log.d("ChessGameScreen", "Meine Spielerfarbe laut ViewModel: $myColor")
        }
    }

    // Initialize game when component first loads
    LaunchedEffect(gameStartResponse) {
        viewModel.initializeGame(gameStartResponse)
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Spielverlauf",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Display game state information
                    currentGameState?.let { gameState ->
                        if (gameState.isCheck != "") {
                            Text(
                                "Schach!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    Button(
                        onClick = { /* Aufgeben Logik */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aufgeben")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Remis Logik */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remis anbieten")
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    // Hier später die Liste der Züge
                    Text("Hier werden die Züge angezeigt...")
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Schachpartie") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spieler und Uhren
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (myColor == TeamColor.WHITE) {
                        // Eigener Spieler (Weiß) links
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .alpha(if (activePlayer == TeamColor.WHITE) 1f else 0.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PlayerClock(
                                playerName = gameStartResponse.whitePlayer,
                                remainingTime = formatSecondsToTimeString(whiteTime),
                                isActive = activePlayer == TeamColor.WHITE
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CapturedPieces(captured = viewModel.capturedBlackPieces.collectAsState().value)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Gegner (Schwarz) rechts
                        Column(
                            modifier = Modifier.weight(1f)
                                .alpha(if (activePlayer == TeamColor.BLACK) 1f else 0.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PlayerClock(
                                playerName = gameStartResponse.blackPlayer,
                                remainingTime = formatSecondsToTimeString(blackTime),
                                isActive = activePlayer == TeamColor.BLACK
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CapturedPieces(captured = viewModel.capturedWhitePieces.collectAsState().value)
                        }
                    } else if (myColor == TeamColor.BLACK) {
                        // Eigener Spieler (Schwarz) links
                        Column(
                            modifier = Modifier.weight(1f)
                                .alpha(if (activePlayer == TeamColor.BLACK) 1f else 0.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PlayerClock(
                                playerName = gameStartResponse.blackPlayer,
                                remainingTime = formatSecondsToTimeString(blackTime),
                                isActive = activePlayer == TeamColor.BLACK
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CapturedPieces(captured = viewModel.capturedWhitePieces.collectAsState().value)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        // Gegner (Weiß) rechts
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .alpha(if (activePlayer == TeamColor.WHITE) 1f else 0.4f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PlayerClock(
                                playerName = gameStartResponse.whitePlayer,
                                remainingTime = formatSecondsToTimeString(whiteTime),
                                isActive = activePlayer == TeamColor.WHITE
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CapturedPieces(captured = viewModel.capturedBlackPieces.collectAsState().value)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Schachbrett - verwende den aktuellen Spielbrett-Zustand
                board.BoardContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    boardState = boardToDisplay,
                    lobbyId = gameStartResponse.lobbyId,
                    onPositionRequest = { positionRequest ->
                        viewModel.sendPositionRequest(gameStartResponse.lobbyId, positionRequest.position)
                    },
                    isFlipped = (myColor == TeamColor.BLACK),
                    possibleMoves = if (myColor == currentPlayer) possibleMoves else emptyList(), // Nur eigene möglichen Züge anzeigen
                    nextPlayer = currentPlayer ?: TeamColor.WHITE, // nextPlayer übergeben
                    myColor = myColor, // eigene Spielerfarbe übergeben
                    isCheck = currentGameState?.isCheck ?: "",
                    onGameMove = { from, to ->
                        val color = myColor
                        if (color != null) {
                            viewModel.sendGameMoveMessage(gameStartResponse.lobbyId, from, to, color)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlayerClock(
    playerName: String,
    remainingTime: String,
    isActive: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = remainingTime,
                style = MaterialTheme.typography.headlineLarge,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

fun pieceToUnicode(piece: app.chesspresso.model.game.PieceInfo): String {
    return when (piece.type.name) {
        "KING" -> if (piece.color.name == "WHITE") "\u2654" else "\u265A"
        "QUEEN" -> if (piece.color.name == "WHITE") "\u2655" else "\u265B"
        "ROOK" -> if (piece.color.name == "WHITE") "\u2656" else "\u265C"
        "BISHOP" -> if (piece.color.name == "WHITE") "\u2657" else "\u265D"
        "KNIGHT" -> if (piece.color.name == "WHITE") "\u2658" else "\u265E"
        "PAWN" -> if (piece.color.name == "WHITE") "\u2659" else "\u265F"
        else -> "?"
    }
}

@Composable
fun CapturedPieces(captured: List<app.chesspresso.model.game.PieceInfo>) {
    if (captured.isEmpty()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kein Text mehr, nur Icons anzeigen
            captured.forEach { piece ->
                Text(text = pieceToUnicode(piece), fontSize = 28.sp)
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

fun formatSecondsToTimeString(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%02d:%02d".format(min, sec)
}