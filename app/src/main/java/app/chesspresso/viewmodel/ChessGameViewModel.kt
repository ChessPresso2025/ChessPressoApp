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

    private var timerJob: Job? = null
    private var lastActivePlayer: TeamColor? = null

    fun initializeGame(gameStartResponse: GameStartResponse) {
        _initialGameData.value = gameStartResponse
        _currentBoard.value = gameStartResponse.board
        _currentPlayer.value = TeamColor.WHITE // Weiß beginnt immer

        // Zeit aus gameTime (Format "mm:ss" oder "mm")
        val totalSeconds = parseTimeStringToSeconds(gameStartResponse.gameTime)
        _whiteTime.value = totalSeconds
        _blackTime.value = totalSeconds
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

    private fun parseTimeStringToSeconds(time: String): Int {
        val parts = time.split(":")
        return if (parts.size == 2) {
            val min = parts[0].toIntOrNull() ?: 0
            val sec = parts[1].toIntOrNull() ?: 0
            min * 60 + sec
        } else {
            (parts[0].toIntOrNull() ?: 0) * 60
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        webSocketService.unsubscribeFromGame()
    }
}
