package app.chesspresso.websocket

import android.util.Log
import app.chesspresso.data.storage.TokenStorage
import app.chesspresso.model.game.GameMoveMessage
import app.chesspresso.service.LobbyListener
import app.chesspresso.model.game.GameMoveResponse
import app.chesspresso.model.game.GameStartMessage
import app.chesspresso.model.game.PieceInfo
import app.chesspresso.model.game.PositionRequestMessage
import app.chesspresso.model.lobby.GameStartResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompWebSocketService @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    companion object {
        private const val TAG = "StompWebSocket"
        private const val WS_URL = "ws://10.0.2.2:8080/ws"
        private const val HEARTBEAT_INTERVAL = 30000L // 30 Sekunden
        private const val RECONNECT_DELAY = 3000L // 3 Sekunden
    }

    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var _playerId: String? = null
    val playerId: String?
        get() = _playerId
    private var currentLobbyId: String? = null

    private var lobbyListener: LobbyListener? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _connectionMessages = MutableStateFlow<List<String>>(emptyList())
    val connectionMessages: StateFlow<List<String>> = _connectionMessages.asStateFlow()

    // Lobby-spezifische Flows
    private val _lobbyMessages = MutableStateFlow<List<String>>(emptyList())
    val lobbyMessages: StateFlow<List<String>> = _lobbyMessages.asStateFlow()

    // Spiel-spezifische Flows
    private val _gameMoveUpdates = MutableStateFlow<GameMoveResponse?>(null)
    val gameMoveUpdates: StateFlow<GameMoveResponse?> = _gameMoveUpdates.asStateFlow()

    // Callback für Lobby-Message-Handling
    private var lobbyMessageHandler: ((String) -> Unit)? = null
    val MESSAGE_END = "\u0000"

    private val json = Json { ignoreUnknownKeys = true }
    private val gson = Gson()
    private val _gameStartedEvent = MutableStateFlow<GameStartResponse?>(null)
    val gameStartedEvent: StateFlow<GameStartResponse?> = _gameStartedEvent.asStateFlow()
    private val _possibleMoves = MutableStateFlow<List<String>>(emptyList())
    val possibleMoves: StateFlow<List<String>> = _possibleMoves.asStateFlow()

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
    }

    enum class ServerStatus {
        OFFLINE, ONLINE, BUSY, MAINTENANCE, UNKNOWN
    }

    fun setLobbyListener(listener: LobbyListener) {
        this.lobbyListener = listener
    }

    private val _serverStatus = MutableStateFlow(ServerStatus.UNKNOWN)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _lastMessageTimestamp = MutableStateFlow(0L)
    private var serverStatusCheckJob: Job? = null

    // Timeout für Server-Status-Überprüfung (10 Sekunden)
    private val SERVER_STATUS_TIMEOUT = 10_000L

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connection opened")
            _connectionState.value = ConnectionState.CONNECTED

            // Sende STOMP CONNECT Frame
            sendStompConnect()

            // Starte Heartbeat
            startHeartbeat()

            // Starte Server-Status-Überprüfung
            startServerStatusCheck()

            // Subscription zu Topics erfolgt erst nach CONNECTED-Antwort vom Server
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received message: $text")
            handleStompMessage(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket connection failed: ${t.message}")
            _connectionState.value = ConnectionState.DISCONNECTED
            stopHeartbeat()
            stopServerStatusCheck()
            _serverStatus.value = ServerStatus.OFFLINE
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "WebSocket connection closed: $reason")
            _connectionState.value = ConnectionState.DISCONNECTED
            stopHeartbeat()
            stopServerStatusCheck()
            _serverStatus.value = ServerStatus.OFFLINE
        }
    }

    suspend fun connect(username: String) {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected")
            return
        }

        _playerId = username
        _connectionState.value = ConnectionState.CONNECTING

        try {
            // Token synchron abrufen
            val token = tokenStorage.getToken().first()
            Log.d(TAG, "Retrieved token for WebSocket connection: ${token?.take(20)}...")

            val client = OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build()

            val requestBuilder = Request.Builder().url(WS_URL)

            // Nur Authorization Header hinzufügen wenn Token verfügbar ist
            token?.let {
                requestBuilder.addHeader("Authorization", "Bearer $it")
                Log.d(TAG, "Added Authorization header to WebSocket request")
            } ?: Log.w(TAG, "No token available for WebSocket connection")

            val request = requestBuilder.build()
            webSocket = client.newWebSocket(request, webSocketListener)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect: ${e.message}")
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    private fun sendStompConnect() {
        val connectFrame = buildString {
            append("CONNECT\n")
            append("accept-version:1.0,1.1,2.0\n")
            append("heart-beat:5000,5000\n")
            _playerId?.let { append("login:$it\n") }
            append("\n")
            append(MESSAGE_END)
        }

        webSocket?.send(connectFrame)
        Log.d(TAG, "Sent STOMP CONNECT frame")
    }

    private fun subscribeToTopics() {
        Log.d(TAG, "Subscribing to topics")
        _playerId?.let { id ->
            // Subscribe zu persönlichen Nachrichten
            // 200 ms warten
            val subscribeFrame2 = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-2\n")
                append("destination:/topic/players\n")
                append("\n")
                append(MESSAGE_END)
            }
            webSocket?.send(subscribeFrame2)
            Log.d(TAG, "Subscribed to topics")

            CoroutineScope(Dispatchers.IO).launch {
                delay(200)
                val subscribeFrame1 = buildString {
                    append("SUBSCRIBE\n")
                    append("id:sub-1\n")
                    append("destination:/user/queue/status\n")
                    append("\n")
                    append(MESSAGE_END)
                }
                webSocket?.send(subscribeFrame1)
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                sendHeartbeat()
                delay(HEARTBEAT_INTERVAL)
            }
        }
        Log.d(TAG, "Heartbeat started")
    }

    private fun sendHeartbeat() {
        _playerId?.let { id ->
            val heartbeatFrame = buildString {
                append("SEND\n")
                append("destination:/app/heartbeat\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"heartbeat","playerId":"$id"}""")
                append(MESSAGE_END)
            }

            webSocket?.send(heartbeatFrame)
            Log.d(TAG, "Sent heartbeat for player: $id")
        }
    }

    private fun handleStompMessage(message: String) {
        try {
            // Auf CONNECTED Frame vom Server prüfen
            if (message.startsWith("CONNECTED")) {
                Log.d(TAG, "Received STOMP CONNECTED frame from server")
                // Jetzt erst die Subscriptions starten, wenn der Server die Verbindung bestätigt hat
                subscribeToTopics()
                return
            }

            if (message.startsWith("MESSAGE")) {
                val lines = message.split("\n")
                var body = ""
                var isBody = false

                for (line in lines) {
                    if (isBody) {
                        body += line
                    } else if (line.isEmpty()) {
                        isBody = true
                    }
                }

                // Entferne Null-Terminator
                body = body.replace(MESSAGE_END, "")

                if (body.isNotEmpty()) {
                    handleMessageBody(body)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling STOMP message: ${e.message}")
        }
    }

    private fun handleMessageBody(body: String) {
        try {
            val json = JSONObject(body)
            val type = json.optString("type")

            when (type) {
                "status-update" -> {
                    val status = json.optString("status")
                    Log.d(TAG, "Status update received: $status")

                    // Server-Status verarbeiten
                    updateServerStatus(status)
                }
                "lobby-message" -> {
                    // Lobby-spezifische Nachrichten verarbeiten
                    val lobbyId = json.optString("lobbyId")
                    val messageContent = json.optString("message")

                    if (lobbyId == currentLobbyId) {
                        // Nur Nachrichten für den aktuellen Lobby-Kontext weiterleiten
                        _lobbyMessages.value = _lobbyMessages.value + messageContent
                        Log.d(TAG, "Lobby message received: $messageContent")
                        // Optional: Direktes Handling der Nachricht über den Handler
                        lobbyMessageHandler?.invoke(messageContent)
                    }
                }

                "gameStarted" -> {
                    val boardMap: Map<String, PieceInfo>? = if (json.isNull("board")) {
                        null
                    } else {
                        val boardJson = json.getJSONObject("board").toString()
                        val boardType = object : TypeToken<Map<String, PieceInfo>>() {}.type
                        gson.fromJson<Map<String, PieceInfo>>(boardJson, boardType)
                    }
                    val rawSuccess = json.opt("success")
                    Log.d(TAG, "[DEBUG] gameStarted: rawSuccess=$rawSuccess, type=${rawSuccess?.javaClass?.name}")
                    val success = when (rawSuccess) {
                        is Boolean -> rawSuccess
                        is String -> rawSuccess.equals("true", ignoreCase = true)
                        is Number -> rawSuccess.toInt() != 0
                        else -> false
                    }
                    // GameTime als Enum direkt aus JSON parsen
                    val gameTimeEnum = try {
                        gson.fromJson(json.get("gameTime").toString(), app.chesspresso.model.lobby.GameTime::class.java)
                    } catch (e: Exception) {
                        app.chesspresso.model.lobby.GameTime.SHORT // Fallback auf 5 Minuten
                    }
                    val response = GameStartResponse(
                        lobbyId = json.optString("lobbyId"),
                        gameTime = gameTimeEnum,
                        whitePlayer = json.optString("whitePlayer"),
                        blackPlayer = json.optString("blackPlayer"),
                        board = boardMap ?: emptyMap(),
                        success = success,
                        lobbyChannel = json.optString("lobbyChannel"),
                        error = if (json.has("error")) json.optString("error") else null
                    )
                    _gameStartedEvent.value = response
                    Log.d(TAG, "Game started event empfangen: $response")
                }

                "moveResponse" -> {
                    // GameMoveResponse verarbeiten
                    try {
                        val gameMoveResponse = this.json.decodeFromString<GameMoveResponse>(body)
                        _gameMoveUpdates.value = gameMoveResponse
                        Log.d(TAG, "Received move response update: $gameMoveResponse")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing GameMoveResponse: ${e.message}")
                    }
                }

                "possible-moves" -> {
                    val movesArray = json.optJSONArray("possibleMoves")
                    val moves = mutableListOf<String>()
                    if (movesArray != null) {
                        for (i in 0 until movesArray.length()) {
                            moves.add(movesArray.getString(i))
                        }
                    }
                    _possibleMoves.value = moves
                    Log.d(TAG, "Possible moves empfangen: $moves")
                }

                else -> {
                    Log.w(TAG, "Unknown message type received: $type")
                }
            }

            // Füge Nachricht zur Liste hinzu
            val currentMessages = _connectionMessages.value.toMutableList()
            currentMessages.add(body)
            if (currentMessages.size > 50) { // Begrenze auf 50 Nachrichten
                currentMessages.removeAt(0)
            }
            _connectionMessages.value = currentMessages

            // Aktualisiere den Zeitstempel der letzten Nachricht
            _lastMessageTimestamp.value = System.currentTimeMillis()

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message body: ${e.message}")
        }
    }

    private fun updateServerStatus(status: String) {
        val newStatus = when (status.lowercase()) {
            "online" -> ServerStatus.ONLINE
            "offline" -> ServerStatus.OFFLINE
            "busy" -> ServerStatus.BUSY
            "maintenance" -> ServerStatus.MAINTENANCE
            else -> ServerStatus.UNKNOWN
        }

        _serverStatus.value = newStatus
        Log.d(TAG, "Server status updated to: $newStatus")
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        Log.d(TAG, "Heartbeat stopped")
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return

        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(RECONNECT_DELAY)
            _playerId?.let { id ->
                if (_connectionState.value == ConnectionState.DISCONNECTED) {
                    Log.d(TAG, "Attempting to reconnect...")
                    _connectionState.value = ConnectionState.RECONNECTING
                    connect(id)
                }
            }
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        stopHeartbeat()
        reconnectJob?.cancel()

        // Sende App-Closing-Nachricht an Server bevor Verbindung getrennt wird
        sendAppClosingMessage()

        // Kurz warten damit Nachricht gesendet werden kann
        Thread.sleep(100)

        // Sende DISCONNECT Frame
        val disconnectFrame = buildString {
            append("DISCONNECT\n")
            append("\n")
            append(MESSAGE_END)
        }

        webSocket?.send(disconnectFrame)
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null

        _connectionState.value = ConnectionState.DISCONNECTED
    }

    private fun sendAppClosingMessage() {
        leaveLobbyOnAppClosing()
        _playerId?.let { id ->
            val appClosingFrame = buildString {
                append("SEND\n")
                append("destination:/app/disconnect\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"app-closing","playerId":"$id","reason":"app-shutdown"}""")
                append(MESSAGE_END)
            }

            webSocket?.send(appClosingFrame)
            Log.d(TAG, "Sent app closing message for player: $id")
        }
    }

    fun sendAppClosingMessageSync() {
        // Synchrone Version für App-Shutdown
        if (_connectionState.value == ConnectionState.CONNECTED) {
            sendAppClosingMessage()
            Thread.sleep(200) // Etwas länger warten für synchronen Aufruf
        }
    }

    fun leaveLobbyOnAppClosing() {
        if (lobbyListener == null) {
            Log.w(TAG, "LobbyListener is not set. Cannot leave lobby on app closing.")
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val currentLobby = lobbyListener!!.currentLobby.value
                Log.d("StompWebSocket", "checking if in a lobby: current Lobby: $currentLobby")
                currentLobby?.let {
                    lobbyListener!!.leaveLobby(it.lobbyId)
                        .onSuccess {
                            Log.d(
                                "StompWebSocket",
                                "Successfully left lobby ${currentLobby.lobbyId} during app closing"
                            )
                        }
                        .onFailure { exception ->
                            Log.e(
                                "StompWebSocket",
                                "Failed to leave lobby during app closing",
                                exception
                            )
                        }
                }
            }
        }
    }

    fun sendAppClosingMessageWithReason(reason: String) {
        leaveLobbyOnAppClosing()
        _playerId?.let { id ->
            val appClosingFrame = buildString {
                append("SEND\n")
                append("destination:/app/disconnect\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"app-closing","playerId":"$id","reason":"$reason","timestamp":"${System.currentTimeMillis()}"}""")
                append(MESSAGE_END)
            }

            webSocket?.send(appClosingFrame)
            Log.d(TAG, "Sent app closing message for player: $id with reason: $reason")

            // Kurz warten damit Nachricht gesendet werden kann
            Thread.sleep(150)
        }
    }

    fun requestOnlinePlayers() {
        _playerId?.let { id ->
            val requestFrame = buildString {
                append("SEND\n")
                append("destination:/app/players\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"request","playerId":"$id"}""")
                append(MESSAGE_END)
            }

            webSocket?.send(requestFrame)
            Log.d(TAG, "Requested online players list")
        }
    }

    fun setLobbyMessageHandler(handler: (String) -> Unit) {
        lobbyMessageHandler = handler
    }

    fun isConnected(): Boolean = _connectionState.value == ConnectionState.CONNECTED

    // Lobby-spezifische Funktionen

    fun subscribeToLobby(lobbyId: String) {
        currentLobbyId = lobbyId

        // Subscribe zu lobby-spezifischen Topics
        val subscribeLobbyFrame = buildString {
            append("SUBSCRIBE\n")
            append("id:sub-lobby-$lobbyId\n")
            append("destination:/topic/lobby/$lobbyId\n")
            append("\n")
            append(MESSAGE_END)
        }

        webSocket?.send(subscribeLobbyFrame)
        Log.d(TAG, "Subscribed to lobby updates: $lobbyId")
    }

    fun unsubscribeFromLobby() {
        currentLobbyId?.let { lobbyId ->
            val unsubscribeFrame = buildString {
                append("UNSUBSCRIBE\n")
                append("id:sub-lobby-$lobbyId\n")
                append("\n")
                append(MESSAGE_END)
            }

            Log.d(
                TAG,
                "Sending STOMP unsubscribe frame: ${
                    unsubscribeFrame.replace(
                        MESSAGE_END,
                        "[NULL]"
                    )
                }"
            )
            webSocket?.send(unsubscribeFrame)
            Log.d(TAG, "Unsubscribed from lobby: $lobbyId")
        }
        currentLobbyId = null
    }

    fun sendLobbyChat(message: String) {
        currentLobbyId?.let { lobbyId ->
            _playerId?.let { id ->
                val chatFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/lobby/chat\n")
                    append("content-type:application/json\n")
                    append("\n")
                    append("""{"type":"chat","playerId":"$id","lobbyId":"$lobbyId","message":"$message"}""")
                    append(MESSAGE_END)
                }

                webSocket?.send(chatFrame)
                Log.d(TAG, "Sent lobby chat message: $message")
            }
        }
    }

    fun sendPlayerReady(ready: Boolean) {
        currentLobbyId?.let { lobbyId ->
            _playerId?.let { id ->
                val readyFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/lobby/ready\n")
                    append("content-type:application/json\n")
                    append("\n")
                    append("""{"type":"player-ready","playerId":"$id","lobbyId":"$lobbyId","ready":$ready}""")
                    append(MESSAGE_END)
                }

                webSocket?.send(readyFrame)
                Log.d(TAG, "Sent player ready status: $ready")
            }
        }
    }

    // Legacy-Methoden für Kompatibilität (werden intern umgeleitet)
    fun joinLobby(lobbyId: String) {
        subscribeToLobby(lobbyId)
    }

    fun leaveLobby() {
        unsubscribeFromLobby()
    }

    fun sendLobbyMessage(message: String) {
        sendLobbyChat(message)
    }

    fun subscribeToGame(lobbyId: String) {
        currentLobbyId = lobbyId

        // Subscribe zu Spiel-Updates für diese Lobby
        val subscribeFrame = buildString {
            append("SUBSCRIBE\n")
            append("id:game-$lobbyId\n")
            append("destination:/topic/game/$lobbyId\n")
            append("\n")
            append(MESSAGE_END)
        }
        webSocket?.send(subscribeFrame)
        Log.d(TAG, "Subscribed to game updates for lobby: $lobbyId")
        currentLobbyId?.let { lobbyId ->
            val subscribeFrameMoves = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-3\n")
                append("destination:/topic/game/$lobbyId/possible-moves\n")
                append("\n")
                append(MESSAGE_END)
            }
            webSocket?.send(subscribeFrameMoves)
            Log.d(TAG, "Subscribed to possible-moves for lobby $lobbyId")
        }
    }

    fun unsubscribeFromGame() {
        currentLobbyId?.let { lobbyId ->
            val unsubscribeFrame = buildString {
                append("UNSUBSCRIBE\n")
                append("id:game-$lobbyId\n")
                append("\n")
                append(MESSAGE_END)
            }
            webSocket?.send(unsubscribeFrame)
            Log.d(TAG, "Unsubscribed from game updates for lobby: $lobbyId")
        }
        currentLobbyId?.let { lobbyId ->
            val subscribeFrameMoves = buildString {
                append("UNSUBSCRIBE\n")
                append("id:sub-3\n")
                append("\n")
                append(MESSAGE_END)
            }
            webSocket?.send(subscribeFrameMoves)
            Log.d(TAG, "Unsubscribed from possible-moves for lobby $lobbyId")
        }

        currentLobbyId = null
    }

    private fun startServerStatusCheck() {
        serverStatusCheckJob?.cancel()
        serverStatusCheckJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                // Prüfen, ob der letzte Nachrichtenzeitpunkt zu lange her ist
                val currentTime = System.currentTimeMillis()
                val lastMessageTime = _lastMessageTimestamp.value

                if (lastMessageTime > 0 && (currentTime - lastMessageTime) > SERVER_STATUS_TIMEOUT) {
                    // Wenn länger als 10 Sekunden keine Nachricht empfangen wurde, Status auf OFFLINE setzen
                    _serverStatus.value = ServerStatus.OFFLINE
                    Log.d(
                        TAG,
                        "Server status set to OFFLINE due to timeout (no message in ${SERVER_STATUS_TIMEOUT / 1000} seconds)"
                    )
                }

                delay(1000) // Alle Sekunde prüfen
            }
        }
        Log.d(TAG, "Server status check started")
    }

    private fun stopServerStatusCheck() {
        serverStatusCheckJob?.cancel()
        serverStatusCheckJob = null
        Log.d(TAG, "Server status check stopped")
    }

    fun sendStartGame(gameStartMessage: GameStartMessage) {
        currentLobbyId?.let { lobbyId ->
            _playerId?.let { id ->
                val messageJson = json.encodeToString(gameStartMessage)
                val startGameFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/game/start\n")
                    append("content-type:application/json\n")
                    append("\n")
                    append(messageJson)
                    append(MESSAGE_END)
                }

                webSocket?.send(startGameFrame)
                Log.d(TAG, "Sent start game message: $messageJson")
            }
        }
    }

    fun sendGameMoveMessage(gameMoveMessage: GameMoveMessage) {
        currentLobbyId?.let { lobbyId ->
            _playerId?.let { id ->
                val messageJson = json.encodeToString(gameMoveMessage)
                val moveFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/game/move\n")
                    append("content-type:application/json\n")
                    append("\n")
                    append(messageJson)
                    append(MESSAGE_END)
                }

                webSocket?.send(moveFrame)
                Log.d(TAG, "Sent game move message: $messageJson")
            }
        }
    }

    fun sendPositionRequest(positionRequestMessage: PositionRequestMessage) {
        currentLobbyId?.let { lobbyId ->
            _playerId?.let { id ->
                val messageJson = json.encodeToString(positionRequestMessage)
                val positionFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/game/position-request\n")
                    append("content-type:application/json\n")
                    append("\n")
                    append(messageJson)
                    append(MESSAGE_END)
                }

                webSocket?.send(positionFrame)
                Log.d(TAG, "Sent position request message: $messageJson")
            }
        }
    }

}
