package app.chesspresso.service

import android.util.Log
import app.chesspresso.api.LobbyApiService
import app.chesspresso.model.game.GameStartMessage
import app.chesspresso.model.lobby.GameStartResponse
import app.chesspresso.model.lobby.GameTime
import app.chesspresso.model.lobby.JoinPrivateLobbyRequest
import app.chesspresso.model.lobby.LeaveLobbyRequest
import app.chesspresso.model.lobby.Lobby
import app.chesspresso.model.lobby.LobbyErrorMessage
import app.chesspresso.model.lobby.LobbyMessage
import app.chesspresso.model.lobby.LobbyStatus
import app.chesspresso.model.lobby.LobbyType
import app.chesspresso.model.lobby.LobbyWaitingMessage
import app.chesspresso.model.lobby.QuickJoinRequest
import app.chesspresso.websocket.StompWebSocketService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LobbyService @Inject constructor(
    private val lobbyApiService: LobbyApiService,
    private val webSocketService: StompWebSocketService,
    private val gson: Gson
) : LobbyListener {
    private val _currentLobby = MutableStateFlow<Lobby?>(null)
    override val currentLobby: StateFlow<Lobby?> = _currentLobby.asStateFlow()

    private val _lobbyMessages = MutableStateFlow<List<LobbyMessage>>(emptyList())
    val lobbyMessages: StateFlow<List<LobbyMessage>> = _lobbyMessages.asStateFlow()

    private val _lobbyError = MutableStateFlow<String?>(null)
    val lobbyError: StateFlow<String?> = _lobbyError.asStateFlow()

    private val _isWaitingForMatch = MutableStateFlow(false)
    val isWaitingForMatch: StateFlow<Boolean> = _isWaitingForMatch.asStateFlow()

    private val _gameStarted = MutableStateFlow<GameStartResponse?>(null)
    val gameStarted: StateFlow<GameStartResponse?> = _gameStarted.asStateFlow()
    private val _lobbyLeft = MutableSharedFlow<Unit>()
    val lobbyLeft = _lobbyLeft.asSharedFlow()

    init {
        webSocketService.setLobbyListener(this)
        // GameStarted-Event aus WebSocketService übernehmen
        kotlinx.coroutines.GlobalScope.launch {
            webSocketService.gameStartedEvent.collectLatest { response ->
                if (response != null) {
                    _gameStarted.value = response
                    Log.d("LobbyService", "Game started Event empfangen: $response")
                }
            }
        }
    }

    // Quick Match beitreten
    suspend fun joinQuickMatch(gameTime: GameTime): Result<String> {
        return try {
            val response = lobbyApiService.joinQuickMatch(QuickJoinRequest(gameTime))
            if (response.isSuccessful && response.body()?.success == true) {
                val lobbyId = response.body()?.lobbyId
                    ?: return Result.failure(Exception("Keine Lobby-ID erhalten"))
                _isWaitingForMatch.value = true

                // Nach erfolgreichem REST-Call: WebSocket-Lobby beitreten für Real-time Updates
                webSocketService.subscribeToLobby(lobbyId)

                Log.d("LobbyService", "Quick Match erfolgreich beigetreten: $lobbyId")
                Result.success(lobbyId)
            } else {
                val errorMsg = response.body()?.error ?: "Unbekannter Fehler beim Quick Match"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("LobbyService", "Fehler beim Quick Match beitreten", e)
            Result.failure(e)
        }
    }

    // Private Lobby erstellen
    suspend fun createPrivateLobby(): Result<String> {
        return try {
            val response = lobbyApiService.createPrivateLobby()
            if (response.isSuccessful && response.body()?.success == true) {
                val lobbyCode = response.body()?.lobbyCode
                    ?: return Result.failure(Exception("Kein Lobby-Code erhalten"))

                // Nach erfolgreichem REST-Call: WebSocket-Lobby beitreten für Real-time Updates
                webSocketService.subscribeToLobby(lobbyCode)

                Log.d("LobbyService", "Private Lobby erstellt: $lobbyCode")
                Result.success(lobbyCode)
            } else {
                val errorMsg = response.body()?.error ?: "Fehler beim Erstellen der Lobby"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("LobbyService", "Fehler beim Erstellen der Private Lobby", e)
            Result.failure(e)
        }
    }

    // Private Lobby beitreten
    suspend fun joinPrivateLobby(lobbyCode: String): Result<String> {
        return try {
            val response = lobbyApiService.joinPrivateLobby(JoinPrivateLobbyRequest(lobbyCode))
            if (response.isSuccessful && response.body()?.success == true) {

                // Nach erfolgreichem REST-Call: WebSocket-Lobby beitreten für Real-time Updates
                webSocketService.subscribeToLobby(lobbyCode)

                Log.d("LobbyService", "Private Lobby beigetreten: $lobbyCode")
                Result.success(lobbyCode)
            } else {
                val errorMsg = response.body()?.error ?: "Fehler beim Beitreten der Lobby"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e("LobbyService", "Fehler beim Beitreten der Private Lobby", e)
            Result.failure(e)
        }
    }

    // Lobby verlassen
    override suspend fun leaveLobby(lobbyId: String): Result<Unit> {
        return try {
            // Zuerst WebSocket-Subscription beenden
            webSocketService.unsubscribeFromLobby()

            Log.d("LobbyService", "Sende Leave-Request an API: LeaveLobbyRequest(lobbyId=$lobbyId)")
            val response = lobbyApiService.leaveLobby(LeaveLobbyRequest(lobbyId))
            if (response.isSuccessful) {
                _currentLobby.value = null
                _isWaitingForMatch.value = false
                _lobbyMessages.value = emptyList()
                _lobbyLeft.emit(Unit)

                Log.d("LobbyService", "Lobby verlassen: $lobbyId")
                Result.success(Unit)
            } else {
                Result.failure(Exception("Fehler beim Verlassen der Lobby"))
            }
        } catch (e: Exception) {
            Log.e("LobbyService", "Fehler beim Verlassen der Lobby", e)
            Result.failure(e)
        }
    }

    fun startGame(gameStartMessage: GameStartMessage): Result<Unit> {
        return try {
            webSocketService.sendStartGame(gameStartMessage)
            Log.d("LobbyService", "Spiel gestartet mit: $gameStartMessage")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("LobbyService", "Fehler beim Starten des Spiels", e)
            Result.failure(e)
        }
    }

    // Lobby-Info abrufen
    suspend fun getLobbyInfo(lobbyId: String): Result<Lobby> {
        return try {
            val response = lobbyApiService.getLobbyInfo(lobbyId)
            if (response.isSuccessful) {
                val lobbyInfo = response.body()
                    ?: return Result.failure(Exception("Keine Lobby-Daten erhalten"))

                // Sichere Behandlung von gameTime - kann null oder "null" sein
                val gameTime = when {
                    lobbyInfo.gameTime == null -> null
                    lobbyInfo.gameTime == "null" -> null
                    lobbyInfo.gameTime.isBlank() -> null
                    else -> try {
                        GameTime.valueOf(lobbyInfo.gameTime)
                    } catch (e: IllegalArgumentException) {
                        Log.w("LobbyService", "Unbekannte GameTime: ${lobbyInfo.gameTime}")
                        null
                    }
                }

                val lobby = Lobby(
                    lobbyId = lobbyInfo.lobbyId,
                    lobbyType = LobbyType.valueOf(lobbyInfo.lobbyType),
                    gameTime = gameTime,
                    players = lobbyInfo.players,
                    creator = lobbyInfo.creator,
                    isGameStarted = lobbyInfo.isGameStarted,
                    status = LobbyStatus.valueOf(lobbyInfo.status)
                )
                _currentLobby.value = lobby
                Result.success(lobby)
            } else {
                Result.failure(Exception("Lobby nicht gefunden"))
            }
        } catch (e: Exception) {
            Log.e("LobbyService", "Fehler beim Abrufen der Lobby-Info", e)
            Result.failure(e)
        }
    }

    // Neue Methode für Player-Ready-Status
    fun setPlayerReady(lobbyId: String, ready: Boolean) {
        webSocketService.sendPlayerReady(ready)
        Log.d("LobbyService", "Setze Spieler-Status: ${if (ready) "bereit" else "nicht bereit"}")
    }

    // Fehler zurücksetzen
    fun clearError() {
        _lobbyError.value = null
    }

    // Spiel-Start zurücksetzen
    fun clearGameStart() {
        _gameStarted.value = null
    }

    // Warte-State zurücksetzen
    fun forceResetWaitingState() {
        _isWaitingForMatch.value = false
    }
}
