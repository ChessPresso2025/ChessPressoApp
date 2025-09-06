package app.chesspresso.screens.game

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import app.chesspresso.model.TeamColor
import app.chesspresso.model.board.Board
import app.chesspresso.model.lobby.GameStartResponse
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeOrange
import app.chesspresso.viewmodel.ChessGameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(
    gameStartResponse: GameStartResponse,
    viewModel: ChessGameViewModel,
    playerId: String,
    navController: NavHostController,
    paddingValues: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
    onGameEnd: (() -> Unit)? = null
) {
    val board = remember { Board() }

    // Collect ViewModel states
    val currentBoard by viewModel.currentBoard.collectAsState()
    val currentPlayer by viewModel.currentPlayer.collectAsState()
    val currentGameState by viewModel.currentGameState.collectAsState()
    val whiteTime by viewModel.whiteTime.collectAsState()
    val blackTime by viewModel.blackTime.collectAsState()
    val myColor by viewModel.myColor.collectAsState()
    val possibleMoves by viewModel.possibleMoves.collectAsState()
    val promotionRequest by viewModel.promotionRequest.collectAsState()
    val gameEndEvent by viewModel.gameEndEvent.collectAsState()
    val fieldHighlights by viewModel.fieldHighlights.collectAsState()
    val pendingRemisRequest by viewModel.pendingRemisRequest.collectAsState()

    // Determine which board state to use (current or initial)
    val boardToDisplay = currentBoard.ifEmpty { gameStartResponse.board }
    val activePlayer = currentPlayer ?: TeamColor.WHITE

    // Neue Zustände für das Game Over Dialog
    var showGameOverDialog by remember { mutableStateOf(false) }
    var gameEndResult by remember { mutableStateOf<app.chesspresso.model.lobby.GameEndResponse?>(null) }

    // Spielende erkennen
    LaunchedEffect(gameEndEvent) {
        if (gameEndEvent != null) {
            onGameEnd?.invoke()
            gameEndResult = gameEndEvent
            showGameOverDialog = true
        } else {
            // Overlay ausblenden, wenn ein neues Spiel startet
            showGameOverDialog = false
            gameEndResult = null
        }
    }

    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game Over UI direkt im Layout anzeigen
            if (showGameOverDialog && gameEndResult != null) {
                GameOverResultInfo(gameEndResult!!, playerId)
                Spacer(modifier = Modifier.height(16.dp))
            }
            // Spieler und Uhren
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val leftPlayerInfo: PlayerUiInfo
                val rightPlayerInfo: PlayerUiInfo
                if (myColor == TeamColor.WHITE) {
                    leftPlayerInfo = PlayerUiInfo(
                        playerName = gameStartResponse.whitePlayer,
                        remainingTime = whiteTime,
                        capturedPieces = viewModel.capturedBlackPieces.collectAsState().value,
                        isActive = activePlayer == TeamColor.WHITE,
                        isUnlimited = gameStartResponse.gameTime.isUnlimited()
                    )
                    rightPlayerInfo = PlayerUiInfo(
                        playerName = gameStartResponse.blackPlayer,
                        remainingTime = blackTime,
                        capturedPieces = viewModel.capturedWhitePieces.collectAsState().value,
                        isActive = activePlayer == TeamColor.BLACK,
                        isUnlimited = gameStartResponse.gameTime.isUnlimited()
                    )
                } else {
                    leftPlayerInfo = PlayerUiInfo(
                        playerName = gameStartResponse.blackPlayer,
                        remainingTime = blackTime,
                        capturedPieces = viewModel.capturedWhitePieces.collectAsState().value,
                        isActive = activePlayer == TeamColor.BLACK,
                        isUnlimited = gameStartResponse.gameTime.isUnlimited()
                    )
                    rightPlayerInfo = PlayerUiInfo(
                        playerName = gameStartResponse.whitePlayer,
                        remainingTime = whiteTime,
                        capturedPieces = viewModel.capturedBlackPieces.collectAsState().value,
                        isActive = activePlayer == TeamColor.WHITE,
                        isUnlimited = gameStartResponse.gameTime.isUnlimited()
                    )
                }
                PlayerInfoPanel(info = leftPlayerInfo, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                PlayerInfoPanel(info = rightPlayerInfo, modifier = Modifier.weight(1f))
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
                    viewModel.sendPositionRequest(
                        gameStartResponse.lobbyId,
                        positionRequest.position
                    )
                },
                isFlipped = (myColor == TeamColor.BLACK),
                possibleMoves = if (myColor == currentPlayer) possibleMoves else emptyList(),
                nextPlayer = currentPlayer ?: TeamColor.WHITE,
                fieldHighlights = fieldHighlights, // NEU: Markierungen für Schachmatt
                myColor = myColor,
                isCheck = currentGameState?.isCheck ?: "",
                onGameMove = { from, to ->
                    val color = myColor
                    if (color != null) {
                        viewModel.sendGameMoveMessage(
                            gameStartResponse.lobbyId,
                            from,
                            to,
                            color
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Promotion Auswahl unter dem Brett ---
            if (promotionRequest != null && promotionRequest!!.activeTeam == myColor) {
                val promotionPosition = promotionRequest!!.position
                val promotionFrom = promotionRequest!!.from
                val promotionOptions = listOf(
                    app.chesspresso.model.PieceType.QUEEN,
                    app.chesspresso.model.PieceType.ROOK,
                    app.chesspresso.model.PieceType.BISHOP,
                    app.chesspresso.model.PieceType.KNIGHT
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Wähle die Figur für die Umwandlung:", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            promotionOptions.forEach { pieceType ->
                                val drawableRes = when (pieceType) {
                                    app.chesspresso.model.PieceType.QUEEN -> if (myColor == TeamColor.WHITE) app.chesspresso.R.drawable.queen_white else app.chesspresso.R.drawable.queen_black
                                    app.chesspresso.model.PieceType.ROOK -> if (myColor == TeamColor.WHITE) app.chesspresso.R.drawable.rook_white else app.chesspresso.R.drawable.rook_black
                                    app.chesspresso.model.PieceType.BISHOP -> if (myColor == TeamColor.WHITE) app.chesspresso.R.drawable.bishop_white else app.chesspresso.R.drawable.bishop_black
                                    app.chesspresso.model.PieceType.KNIGHT -> if (myColor == TeamColor.WHITE) app.chesspresso.R.drawable.knight_white else app.chesspresso.R.drawable.knight_black
                                    else -> 0
                                }
                                if (drawableRes != 0) {
                                    Image(
                                        painter = painterResource(id = drawableRes),
                                        contentDescription = pieceType.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(horizontal = 6.dp)
                                            .clickable {
                                                // Sende jetzt eine MoveMessage mit promotedPiece
                                                viewModel.sendGameMoveMessage(
                                                    gameStartResponse.lobbyId,
                                                    promotionFrom,
                                                    promotionPosition,
                                                    myColor!!,
                                                    pieceType
                                                )
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Remis-Anfrage-Dialog
            if (pendingRemisRequest != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { viewModel.respondToRemisRequest(false) },
                    title = { Text("Unentschieden?") },
                    text = { Text("Dein Gegner bietet ein Remis an. Möchtest du das Remis annehmen?") },
                    confirmButton = {
                        Button(onClick = { viewModel.respondToRemisRequest(true) }) {
                            Text("Annehmen")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { viewModel.respondToRemisRequest(false) }) {
                            Text("Ablehnen")
                        }
                    }
                )
            }

            // Am unteren Rand die Buttons anzeigen, falls Game Over
            if (showGameOverDialog && gameEndResult != null) {
                Spacer(modifier = Modifier.weight(1f))
                GameOverActions(gameEndResult!!, viewModel, navController = navController)
                // Rematch-Dialoge zentral anzeigen
                RematchDialogsHandler(viewModel, gameEndResult!!, navController)
            }
        }
    }
}

@Composable
fun PlayerClock(
    playerName: String,
    remainingTime: String,
    remainingSeconds: Int,
    isActive: Boolean,
    isUnlimited: Boolean
) {
    CoffeeCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) Modifier else Modifier
            ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary
        ) else null,
        content = {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = playerName,
                    style = MaterialTheme.typography.headlineMedium, // statt titleMedium
                    color = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                // Anzeige der verbleibenden Zeit oder "Unbegrenzt"
                if (isUnlimited) {
                    Text(
                        text = "∞",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    // Zeit blinkt rot, wenn weniger als 10 Sekunden übrig sind und aktiv
                    val blink = rememberInfiniteTransition()
                    val animatedColor by blink.animateColor(
                        initialValue = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        targetValue = if (isActive && remainingSeconds < 10 && remainingSeconds > 0) CoffeeOrange else if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 500),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Text(
                        text = remainingTime,
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (isActive && remainingSeconds < 10 && remainingSeconds > 0) animatedColor else if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    )
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
fun CapturedPieces(captured: List<app.chesspresso.model.game.PieceInfo>, isActive: Boolean) {
    if(captured.isEmpty()){
        Spacer(modifier = Modifier.height(50.dp))
    }else{
        CoffeeCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            border = if (isActive) androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            ) else null,
            content = {
                LazyRow(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(captured) { piece ->
                        Text(text = pieceToUnicode(piece), fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        )
    }
}

fun formatSecondsToTimeString(seconds: Int): String {
    val min = seconds / 60
    val sec = seconds % 60
    return "%02d:%02d".format(min, sec)
}

data class PlayerUiInfo(
    val playerName: String,
    val remainingTime: Int,
    val capturedPieces: List<app.chesspresso.model.game.PieceInfo>,
    val isActive: Boolean,
    val isUnlimited: Boolean
)

@Composable
fun PlayerInfoPanel(
    info: PlayerUiInfo,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (info.isActive) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerClock(
            playerName = info.playerName,
            remainingTime = formatSecondsToTimeString(info.remainingTime),
            remainingSeconds = info.remainingTime,
            isActive = info.isActive,
            isUnlimited = info.isUnlimited
        )
        Spacer(modifier = Modifier.height(8.dp))
        CapturedPieces(captured = info.capturedPieces, isActive = info.isActive)
    }
}
