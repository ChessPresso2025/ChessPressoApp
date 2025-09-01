package app.chesspresso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.model.EndType
import app.chesspresso.model.PieceType
import app.chesspresso.model.TeamColor
import app.chesspresso.model.game.GameMoveResponse
import app.chesspresso.model.lobby.GameEndMessage
import app.chesspresso.model.lobby.GameEndResponse
import app.chesspresso.model.lobby.GameStartResponse
import app.chesspresso.websocket.StompWebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChessGameViewModel @Inject constructor(
    val webSocketService: StompWebSocketService
) : ViewModel() {

    private val _currentGameState = MutableStateFlow<GameMoveResponse?>(null)
    val currentGameState: StateFlow<GameMoveResponse?> = _currentGameState.asStateFlow()

    private val _initialGameData = MutableStateFlow<GameStartResponse?>(null)
    val initialGameData: StateFlow<GameStartResponse?> = _initialGameData.asStateFlow()

    private val _currentBoard =
        MutableStateFlow<Map<String, app.chesspresso.model.game.PieceInfo>>(emptyMap())
    val currentBoard: StateFlow<Map<String, app.chesspresso.model.game.PieceInfo>> =
        _currentBoard.asStateFlow()

    private val _currentPlayer = MutableStateFlow<app.chesspresso.model.TeamColor?>(null)
    val currentPlayer: StateFlow<app.chesspresso.model.TeamColor?> = _currentPlayer.asStateFlow()

    private val _whiteTime = MutableStateFlow(0)
    val whiteTime: StateFlow<Int> = _whiteTime.asStateFlow()
    private val _blackTime = MutableStateFlow(0)
    val blackTime: StateFlow<Int> = _blackTime.asStateFlow()

    private val _myColor = MutableStateFlow<TeamColor?>(null)
    val myColor: StateFlow<TeamColor?> = _myColor.asStateFlow()

    private val _possibleMoves = MutableStateFlow<List<String>>(emptyList())
    val possibleMoves: StateFlow<List<String>> = _possibleMoves.asStateFlow()

    private val _capturedWhitePieces =
        MutableStateFlow<List<app.chesspresso.model.game.PieceInfo>>(emptyList())
    val capturedWhitePieces: StateFlow<List<app.chesspresso.model.game.PieceInfo>> =
        _capturedWhitePieces.asStateFlow()
    private val _capturedBlackPieces =
        MutableStateFlow<List<app.chesspresso.model.game.PieceInfo>>(emptyList())
    val capturedBlackPieces: StateFlow<List<app.chesspresso.model.game.PieceInfo>> =
        _capturedBlackPieces.asStateFlow()
    private val _promotionRequest =
        MutableStateFlow<app.chesspresso.model.game.PromotionRequest?>(null)
    val promotionRequest: StateFlow<app.chesspresso.model.game.PromotionRequest?> =
        _promotionRequest.asStateFlow()

    private val _gameEndEvent = MutableStateFlow<GameEndResponse?>(null)
    val gameEndEvent: StateFlow<GameEndResponse?> = _gameEndEvent

    private val _moveHistory = MutableStateFlow<List<GameMoveResponse>>(emptyList())
    val moveHistory: StateFlow<List<GameMoveResponse>> = _moveHistory.asStateFlow()

    private var timerJob: Job? = null
    private var lastActivePlayer: TeamColor? = null
    private var timeoutSent = false // Neu: Flag, um Mehrfachsendung zu verhindern

    enum class FieldHighlight {
        NONE,
        CHECKMATE_KING,
        CHECKMATE_ATTACKER
    }

    private val _fieldHighlights = MutableStateFlow<Map<String, FieldHighlight>>(emptyMap())
    val fieldHighlights: StateFlow<Map<String, FieldHighlight>> = _fieldHighlights.asStateFlow()

    private val _pendingRemisRequest =
        MutableStateFlow<app.chesspresso.model.lobby.RemisMessage?>(null)
    val pendingRemisRequest: StateFlow<app.chesspresso.model.lobby.RemisMessage?> =
        _pendingRemisRequest.asStateFlow()

    init {
        viewModelScope.launch {
            webSocketService.gameStartedEvent.collect { event ->
                event?.let { initializeGame(it) }
            }
        }
        viewModelScope.launch {
            webSocketService.possibleMoves.collect { moves ->
                _possibleMoves.value = moves
            }
        }

        viewModelScope.launch {
            webSocketService.gameMoveUpdates.collect { gameMoveResponse ->
                gameMoveResponse?.let { response ->
                    val captured = response.move.captured
                    if (captured != null && captured.type != null && captured.color != null) {
                        val capturedPiece = app.chesspresso.model.game.PieceInfo(
                            type = captured.type,
                            color = captured.color
                        )
                        when (captured.color) {
                            TeamColor.WHITE -> _capturedWhitePieces.value =
                                _capturedWhitePieces.value + capturedPiece

                            TeamColor.BLACK -> _capturedBlackPieces.value =
                                _capturedBlackPieces.value + capturedPiece

                            else -> {}
                        }
                    }
                    val newBoard = response.board
                    _currentGameState.value = response
                    _currentBoard.value = newBoard
                    _possibleMoves.value = emptyList()
                    if (response.nextPlayer != lastActivePlayer) {
                        startTimer(response.nextPlayer)
                        lastActivePlayer = response.nextPlayer
                    }
                    _currentPlayer.value = response.nextPlayer
                    // Promotion-UI ausblenden, sobald ein Zug vom Server kommt
                    _promotionRequest.value = null
                    // Zug zur History hinzufügen
                    _moveHistory.value = _moveHistory.value + response

                    // --- Schachmatt-Markierungen setzen ---
                    val highlights = mutableMapOf<String, FieldHighlight>()
                    val checkmateFields = response.checkmate
                    if (!checkmateFields.isNullOrEmpty()) {
                        // König des aktiven Teams suchen
                        val kingField = newBoard.entries.find { (_, piece) ->
                            piece.type == app.chesspresso.model.PieceType.KING && piece.color == response.nextPlayer
                        }?.key
                        if (kingField != null) {
                            highlights[kingField] = FieldHighlight.CHECKMATE_KING
                        }
                        // Angreiferfelder markieren
                        checkmateFields.forEach { field ->
                            highlights[field] = FieldHighlight.CHECKMATE_ATTACKER
                        }
                    }
                    _fieldHighlights.value = highlights
                    // --- Ende Schachmatt-Markierungen ---
                }
            }
        }
        viewModelScope.launch {
            webSocketService.promotionRequest.collect { request ->
                _promotionRequest.value = request
            }
        }
        viewModelScope.launch {
            webSocketService.gameEndEvent.collect { event ->
                _gameEndEvent.value = event
            }
        }
        viewModelScope.launch {
            webSocketService.remisRequest.collect { remisMessage ->
                // Nur anzeigen, wenn es eine Anfrage vom Gegner ist (responder == null, requester != ich)
                if (remisMessage != null && !remisMessage.accept && remisMessage.responder == null) {
                    _pendingRemisRequest.value = remisMessage
                } else {
                    _pendingRemisRequest.value = null
                }
            }
        }
    }

    fun initializeGame(gameStartResponse: GameStartResponse) {
        viewModelScope.launch {
            // Warte, bis playerId gesetzt ist
            var myId = webSocketService.playerId
            var retry = 0
            while (myId == null && retry < 50) { // max. 5 Sekunden warten
                delay(100)
                myId = webSocketService.playerId
                retry++
            }
            if (myId == null) {
                // Fehlerfall: ID konnte nicht ermittelt werden
                android.util.Log.e(
                    "ChessGameViewModel",
                    "playerId ist nach 5 Sekunden immer noch null!"
                )
            }
            _currentGameState.value = null
            _initialGameData.value = gameStartResponse
            _currentBoard.value = gameStartResponse.board
            _currentPlayer.value = TeamColor.WHITE // Weiß beginnt immer

            _myColor.value = when (myId) {
                gameStartResponse.whitePlayer -> TeamColor.WHITE
                gameStartResponse.blackPlayer -> TeamColor.BLACK
                else -> null
            }

            // Zeit direkt aus gameTime (jetzt Int in Sekunden)
            _whiteTime.value = gameStartResponse.gameTime.seconds
            _blackTime.value = gameStartResponse.gameTime.seconds
            lastActivePlayer = TeamColor.WHITE
            startTimer(TeamColor.WHITE)

            // Subscribe zu Spiel-Updates für diese Lobby
            webSocketService.subscribeToGame(gameStartResponse.lobbyId)
        }
    }

    private fun startTimer(activePlayer: TeamColor) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            timeoutSent = false // Timer-Start: Reset Flag
            while (true) {
                delay(1000)
                if (activePlayer == TeamColor.WHITE) {
                    if (_whiteTime.value > 0) {
                        _whiteTime.value = _whiteTime.value - 1
                        if (_whiteTime.value == 0 && !timeoutSent && _myColor.value == TeamColor.WHITE) {
                            timeoutSent = true
                            sendTimeoutEndMessage(TeamColor.WHITE)
                        }
                    }
                } else {
                    if (_blackTime.value > 0) {
                        _blackTime.value = _blackTime.value - 1
                        if (_blackTime.value == 0 && !timeoutSent && _myColor.value == TeamColor.BLACK) {
                            timeoutSent = true
                            sendTimeoutEndMessage(TeamColor.BLACK)
                        }
                    }
                }
            }
        }
    }

    private fun sendTimeoutEndMessage(teamColor: TeamColor) {
        val lobbyId = _initialGameData.value?.lobbyId ?: return
        val gameEndMessage = GameEndMessage(
            lobbyId = lobbyId,
            player = teamColor.name,
            endType = EndType.TIMEOUT
        )
        webSocketService.sendEndGameMessage(gameEndMessage)
    }

    fun sendPositionRequest(lobbyId: String, position: String) {
        viewModelScope.launch {
            val message = app.chesspresso.model.game.PositionRequestMessage(lobbyId, position)
            webSocketService.sendPositionRequest(message)
        }
    }

    fun sendGameMoveMessage(
        lobbyId: String,
        from: String,
        to: String,
        teamColor: TeamColor,
        promotedPiece: PieceType? = null
    ) {
        val message = app.chesspresso.model.game.GameMoveMessage(
            lobbyId = lobbyId,
            from = from,
            to = to,
            teamColor = teamColor,
            promotedPiece = promotedPiece
        )
        webSocketService.sendGameMoveMessage(message)
    }

    fun resignGame(teamColor: TeamColor, lobbyId: String) {
        val gameEndMessage = GameEndMessage(
            lobbyId = lobbyId,
            player = teamColor.name,
            endType = EndType.RESIGNATION
        )
        webSocketService.sendEndGameMessage(gameEndMessage)
    }

    fun closeLobby(lobbyId: String) {
        webSocketService.sendLobbyCloseMessage(lobbyId)
    }

    fun clearGameEndEvent() {
        _gameEndEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        webSocketService.unsubscribeFromGame()
        webSocketService.resetGameFlows()
    }

    fun offerDraw(lobbyId: String, player: TeamColor) {
        val remisMessage = app.chesspresso.model.lobby.RemisMessage(
            lobbyId = lobbyId,
            requester = player,
            responder = TeamColor.NULL,
            accept = false // Remis wird angeboten
        )
        webSocketService.sendRemisMessage(remisMessage)
    }

    fun respondToRemisRequest(accept: Boolean) {
        val request = _pendingRemisRequest.value ?: return
        val response = app.chesspresso.model.lobby.RemisMessage(
            lobbyId = request.lobbyId,
            requester = request.requester,
            responder = myColor.value ?: TeamColor.NULL,
            accept = accept
        )
        webSocketService.sendRemisMessage(response)
        _pendingRemisRequest.value = null
    }
}