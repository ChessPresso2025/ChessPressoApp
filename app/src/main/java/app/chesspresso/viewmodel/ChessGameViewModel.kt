package app.chesspresso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.model.game.GameMoveResponse
import app.chesspresso.model.lobby.GameStartResponse
import app.chesspresso.websocket.StompWebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import app.chesspresso.model.TeamColor
import javax.inject.Inject

@HiltViewModel
class ChessGameViewModel @Inject constructor(
    private val webSocketService: StompWebSocketService
) : ViewModel() {

    private val _currentGameState = MutableStateFlow<GameMoveResponse?>(null)
    val currentGameState: StateFlow<GameMoveResponse?> = _currentGameState.asStateFlow()

    private val _initialGameData = MutableStateFlow<GameStartResponse?>(null)
    val initialGameData: StateFlow<GameStartResponse?> = _initialGameData.asStateFlow()

    private val _currentBoard = MutableStateFlow<Map<String, app.chesspresso.model.game.PieceInfo>>(emptyMap())
    val currentBoard: StateFlow<Map<String, app.chesspresso.model.game.PieceInfo>> = _currentBoard.asStateFlow()

    private val _currentPlayer = MutableStateFlow<app.chesspresso.model.TeamColor?>(null)
    val currentPlayer: StateFlow<app.chesspresso.model.TeamColor?> = _currentPlayer.asStateFlow()

    private val _whiteTime = MutableStateFlow(0)
    val whiteTime: StateFlow<Int> = _whiteTime.asStateFlow()
    private val _blackTime = MutableStateFlow(0)
    val blackTime: StateFlow<Int> = _blackTime.asStateFlow()

    private val _myColor = MutableStateFlow<TeamColor?>(null)
    val myColor: StateFlow<TeamColor?> = _myColor.asStateFlow()

    private var timerJob: Job? = null
    private var lastActivePlayer: TeamColor? = null

    init {
        viewModelScope.launch {
            webSocketService.gameStartedEvent.collect { event ->
                event?.let { initializeGame(it) }
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
                android.util.Log.e("ChessGameViewModel", "playerId ist nach 5 Sekunden immer noch null!")
            }
            _currentGameState.value = null
            _initialGameData.value = gameStartResponse
            _currentBoard.value = gameStartResponse.board
            _currentPlayer.value = TeamColor.WHITE // Weiß beginnt immer

            // Eigene Farbe bestimmen
            android.util.Log.d("ChessGameViewModel", "myId: $myId, whitePlayer: ${gameStartResponse.whitePlayer}, blackPlayer: ${gameStartResponse.blackPlayer}")
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

            // Lausche auf GameMoveResponse-Updates
            viewModelScope.launch {
                webSocketService.gameMoveUpdates.collect { gameMoveResponse ->
                    gameMoveResponse?.let { response ->
                        _currentGameState.value = response
                        _currentBoard.value = response.board
                        // Wenn sich der Spieler ändert, Timer umschalten
                        if (response.nextPlayer != lastActivePlayer) {
                            startTimer(response.nextPlayer)
                            lastActivePlayer = response.nextPlayer
                        }
                        _currentPlayer.value = response.nextPlayer
                    }
                }
            }
        }
    }

    private fun startTimer(activePlayer: TeamColor) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (activePlayer == TeamColor.WHITE) {
                    if (_whiteTime.value > 0) _whiteTime.value = _whiteTime.value - 1
                } else {
                    if (_blackTime.value > 0) _blackTime.value = _blackTime.value - 1
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        webSocketService.unsubscribeFromGame()
    }
}
