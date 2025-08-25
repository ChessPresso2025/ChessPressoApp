package app.chesspresso.service

import android.util.Log
import app.chesspresso.api.LobbyApiService
import app.chesspresso.model.lobby.*
import app.chesspresso.websocket.StompWebSocketService
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LobbyService @Inject constructor(
    private val lobbyApiService: LobbyApiService,
    private val webSocketService: StompWebSocketService,
    private val gson: Gson
) {
    private val _currentLobby = MutableStateFlow<Lobby?>(null)
    val currentLobby: StateFlow<Lobby?> = _currentLobby.asStateFlow()

    private val _lobbyMessages = MutableStateFlow<List<LobbyMessage>>(emptyList())
    val lobbyMessages: StateFlow<List<LobbyMessage>> = _lobbyMessages.asStateFlow()

    private val _lobbyError = MutableStateFlow<String?>(null)
    val lobbyError: StateFlow<String?> = _lobbyError.asStateFlow()

    private val _isWaitingForMatch = MutableStateFlow(false)
    val isWaitingForMatch: StateFlow<Boolean> = _isWaitingForMatch.asStateFlow()

    private val _gameStarted = MutableStateFlow<GameStartMessage?>(null)
    val gameStarted: StateFlow<GameStartMessage?> = _gameStarted.asStateFlow()

    init {
        webSocketService.setLobbyMessageHandler { message ->
            handleWebSocketMessage(message)
        }
    }

    // Quick Match beitreten
    suspend fun joinQuickMatch(gameTime: GameTime): Result<String> {
        return try {
            val response = lobbyApiService.joinQuickMatch(QuickJoinRequest(gameTime))
            if (response.isSuccessful && response.body()?.success == true) {
                val lobbyId = response.body()?.lobbyId ?: return Result.failure(Exception("Keine Lobby-ID erhalten"))
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
                val lobbyCode = response.body()?.lobbyCode ?: return Result.failure(Exception("Kein Lobby-Code erhalten"))

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
    suspend fun leaveLobby(lobbyId: String): Result<Unit> {
        return try {
            // Zuerst WebSocket-Subscription beenden
            webSocketService.unsubscribeFromLobby()
            
            Log.d("LobbyService", "Sende Leave-Request an API: LeaveLobbyRequest(lobbyId=$lobbyId)")
            val response = lobbyApiService.leaveLobby(LeaveLobbyRequest(lobbyId))
            if (response.isSuccessful) {
                _currentLobby.value = null
                _isWaitingForMatch.value = false
                _lobbyMessages.value = emptyList()

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

    // Lobby-Info abrufen
    suspend fun getLobbyInfo(lobbyId: String): Result<Lobby> {
        return try {
            val response = lobbyApiService.getLobbyInfo(lobbyId)
            if (response.isSuccessful) {
                val lobbyInfo = response.body() ?: return Result.failure(Exception("Keine Lobby-Daten erhalten"))

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

    // WebSocket-Nachrichten verarbeiten
    fun handleWebSocketMessage(message: String) {
        try {
            Log.d("LobbyService", "WebSocket-Nachricht erhalten: $message")

            // Parse JSON-Nachricht
            val jsonObject = gson.fromJson(message, Map::class.java)
            val messageType = jsonObject["type"] as? String

            when (messageType) {
                "lobby-waiting" -> {
                    val lobbyId = jsonObject["lobbyId"] as? String
                    val waitingMessage = jsonObject["message"] as? String
                    _isWaitingForMatch.value = true
                    Log.d("LobbyService", "Warte auf Gegner in Lobby: $lobbyId")
                }

                "lobby-error" -> {
                    val error = jsonObject["error"] as? String ?: "Unbekannter Lobby-Fehler"
                    _lobbyError.value = error
                    _isWaitingForMatch.value = false
                    Log.e("LobbyService", "Lobby-Fehler: $error")
                }

                "player-joined" -> {
                    val lobbyId = jsonObject["lobbyId"] as? String
                    val newPlayerId = jsonObject["newPlayerId"] as? String
                    val players = jsonObject["players"] as? List<String> ?: emptyList()
                    val status = jsonObject["status"] as? String
                    val message = jsonObject["message"] as? String
                    val isLobbyFull = jsonObject["isLobbyFull"] as? Boolean ?: false

                    // Aktualisiere die aktuelle Lobby
                    _currentLobby.value?.let { currentLobby ->
                        if (currentLobby.lobbyId == lobbyId) {
                            val updatedLobby = currentLobby.copy(
                                players = players,
                                status = status?.let { LobbyStatus.valueOf(it) } ?: currentLobby.status
                            )
                            _currentLobby.value = updatedLobby
                        }
                    }

                    Log.d("LobbyService", "Spieler beigetreten: $newPlayerId - $message")
                }

                "lobby-joined" -> {
                    val lobbyId = jsonObject["lobbyId"] as? String
                    val creatorId = jsonObject["creatorId"] as? String
                    val players = jsonObject["players"] as? List<String> ?: emptyList()
                    val status = jsonObject["status"] as? String
                    val message = jsonObject["message"] as? String
                    val isLobbyFull = jsonObject["isLobbyFull"] as? Boolean ?: false

                    // Aktualisiere die aktuelle Lobby
                    _currentLobby.value?.let { currentLobby ->
                        if (currentLobby.lobbyId == lobbyId) {
                            val updatedLobby = currentLobby.copy(
                                players = players,
                                status = status?.let { LobbyStatus.valueOf(it) } ?: currentLobby.status,
                                creator = creatorId ?: currentLobby.creator
                            )
                            _currentLobby.value = updatedLobby
                        }
                    }

                    Log.d("LobbyService", "Lobby erfolgreich beigetreten: $message")
                }

                "lobby-update" -> {
                    val lobbyId = jsonObject["lobbyId"] as? String
                    val players = jsonObject["players"] as? List<String> ?: emptyList()
                    val status = jsonObject["status"] as? String
                    val updateMessage = jsonObject["message"] as? String

                    // Aktualisiere die aktuelle Lobby
                    _currentLobby.value?.let { currentLobby ->
                        if (currentLobby.lobbyId == lobbyId) {
                            val updatedLobby = currentLobby.copy(
                                players = players,
                                status = status?.let { LobbyStatus.valueOf(it) } ?: currentLobby.status
                            )
                            _currentLobby.value = updatedLobby
                        }
                    }

                    Log.d("LobbyService", "Lobby-Update: $updateMessage")
                }

                "game-start" -> {
                    val lobbyId = jsonObject["lobbyId"] as? String ?: ""
                    val gameTime = jsonObject["gameTime"] as? String ?: ""
                    val whitePlayer = jsonObject["whitePlayer"] as? String ?: ""
                    val blackPlayer = jsonObject["blackPlayer"] as? String ?: ""
                    val lobbyChannel = jsonObject["lobbyChannel"] as? String ?: ""

                    val gameStart = GameStartMessage(
                        lobbyId = lobbyId,
                        gameTime = gameTime,
                        whitePlayer = whitePlayer,
                        blackPlayer = blackPlayer,
                        lobbyChannel = lobbyChannel
                    )

                    _gameStarted.value = gameStart
                    _isWaitingForMatch.value = false
                    Log.d("LobbyService", "Spiel startet! Lobby: $lobbyId")
                }

                "lobby-created" -> {
                    val lobbyCode = jsonObject["lobbyCode"] as? String
                    val message = jsonObject["message"] as? String
                    Log.d("LobbyService", "Private Lobby erstellt: $lobbyCode - $message")
                }

                "PLAYER_READY" -> {
                    val playerId = jsonObject["playerId"] as? String
                    Log.d("LobbyService", "Spieler bereit: $playerId")
                    // Hier könntest du den Ready-Status in der UI anzeigen
                }

                "GAME_START" -> {
                    // Alternative Behandlung für direktes GAME_START
                    val players = jsonObject["players"] as? List<String> ?: emptyList()
                    val whitePlayer = jsonObject["whitePlayer"] as? String ?: ""
                    val blackPlayer = jsonObject["blackPlayer"] as? String ?: ""
                    val gameTimeObj = jsonObject["gameTime"]

                    // Extrahiere gameTime richtig
                    val gameTime = when (gameTimeObj) {
                        is String -> gameTimeObj
                        is Map<*, *> -> gameTimeObj["name"] as? String ?: "MIDDLE"
                        else -> "MIDDLE"
                    }

                    val gameStart = GameStartMessage(
                        lobbyId = _currentLobby.value?.lobbyId ?: "",
                        gameTime = gameTime,
                        whitePlayer = whitePlayer,
                        blackPlayer = blackPlayer,
                        lobbyChannel = ""
                    )

                    _gameStarted.value = gameStart
                    _isWaitingForMatch.value = false
                    Log.d("LobbyService", "Direkter Spielstart erkannt")
                }

                else -> {
                    // Versuche Legacy-Format zu parsen
                    when {
                        message.contains("\"error\"") -> {
                            val errorMsg = gson.fromJson(message, LobbyErrorMessage::class.java)
                            _lobbyError.value = errorMsg.error
                            _isWaitingForMatch.value = false
                        }
                        message.contains("\"lobbyId\"") && message.contains("\"message\"") -> {
                            val waitingMsg = gson.fromJson(message, LobbyWaitingMessage::class.java)
                            _isWaitingForMatch.value = true
                        }
                        message.contains("\"gameTime\"") && message.contains("\"whitePlayer\"") -> {
                            val gameStart = gson.fromJson(message, GameStartMessage::class.java)
                            _gameStarted.value = gameStart
                            _isWaitingForMatch.value = false
                        }
                        else -> {
                            Log.w("LobbyService", "Unbekannter Nachrichtentyp: $message")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("LobbyService", "Fehler beim Verarbeiten der WebSocket-Nachricht: $message", e)
        }
    }

    // Neue Methode für Lobby-Chat-Nachrichten
    fun sendLobbyMessage(lobbyId: String, content: String) {
        webSocketService.sendLobbyChat(content)
        Log.d("LobbyService", "Sende Chat-Nachricht in Lobby $lobbyId: $content")
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
}
