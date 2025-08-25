package app.chesspresso.websocket

import android.util.Log
import app.chesspresso.ChessPressoApplication
import app.chesspresso.data.storage.TokenStorage
import app.chesspresso.di.AppModule.provideLobbyService
import app.chesspresso.service.LobbyListener
import app.chesspresso.service.LobbyService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import okhttp3.*
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
    private var playerId: String? = null
    private var currentLobbyId: String? = null

    private var lobbyListener: LobbyListener? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _onlinePlayers = MutableStateFlow<Set<String>>(emptySet())
    val onlinePlayers: StateFlow<Set<String>> = _onlinePlayers.asStateFlow()

    private val _connectionMessages = MutableStateFlow<List<String>>(emptyList())
    val connectionMessages: StateFlow<List<String>> = _connectionMessages.asStateFlow()

    // Lobby-spezifische Flows
    private val _lobbyMessages = MutableStateFlow<List<String>>(emptyList())
    val lobbyMessages: StateFlow<List<String>> = _lobbyMessages.asStateFlow()

    // Callback für Lobby-Message-Handling
    private var lobbyMessageHandler: ((String) -> Unit)? = null

    enum class ConnectionState {
        DISCONNECTED, CONNECTING, CONNECTED, RECONNECTING
    }

    fun setLobbyListener(listener: LobbyListener){
        this.lobbyListener = listener
    }

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket connection opened")
            _connectionState.value = ConnectionState.CONNECTED

            // Sende STOMP CONNECT Frame
            sendStompConnect()

            // Starte Heartbeat
            startHeartbeat()

            // Subscribe zu Topics
            subscribeToTopics()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d(TAG, "Received message: $text")
            handleStompMessage(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket connection failed: ${t.message}")
            _connectionState.value = ConnectionState.DISCONNECTED
            stopHeartbeat()
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.i(TAG, "WebSocket connection closed: $reason")
            _connectionState.value = ConnectionState.DISCONNECTED
            stopHeartbeat()
        }
    }

    suspend fun connect(username: String) {
        if (_connectionState.value == ConnectionState.CONNECTED) {
            Log.d(TAG, "Already connected")
            return
        }

        playerId = username
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
            playerId?.let { append("login:$it\n") }
            append("\n")
            append("\u0000")
        }

        webSocket?.send(connectFrame)
        Log.d(TAG, "Sent STOMP CONNECT frame")
    }

    private fun subscribeToTopics() {
        playerId?.let { id ->
            // Subscribe zu persönlichen Nachrichten
            val subscribeFrame1 = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-1\n")
                append("destination:/user/$id/queue/status\n")
                append("\n")
                append("\u0000")
            }
            webSocket?.send(subscribeFrame1)

            // Subscribe zu öffentlichen Player-Updates
            val subscribeFrame2 = buildString {
                append("SUBSCRIBE\n")
                append("id:sub-2\n")
                append("destination:/topic/players\n")
                append("\n")
                append("\u0000")
            }
            webSocket?.send(subscribeFrame2)

            Log.d(TAG, "Subscribed to topics")
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
        playerId?.let { id ->
            val heartbeatFrame = buildString {
                append("SEND\n")
                append("destination:/app/heartbeat\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"heartbeat","playerId":"$id"}""")
                append("\u0000")
            }

            webSocket?.send(heartbeatFrame)
            Log.d(TAG, "Sent heartbeat for player: $id")
        }
    }

    private fun handleStompMessage(message: String) {
        try {
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
                body = body.replace("\u0000", "")

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
                "connection-status" -> {
                    val onlinePlayersArray = json.optJSONArray("onlinePlayers")
                    val players = mutableSetOf<String>()

                    onlinePlayersArray?.let { array ->
                        for (i in 0 until array.length()) {
                            players.add(array.getString(i))
                        }
                    }

                    _onlinePlayers.value = players
                    Log.d(TAG, "Updated online players: $players")
                }
                "players-update" -> {
                    val onlinePlayersArray = json.optJSONArray("onlinePlayers")
                    val players = mutableSetOf<String>()

                    onlinePlayersArray?.let { array ->
                        for (i in 0 until array.length()) {
                            players.add(array.getString(i))
                        }
                    }

                    _onlinePlayers.value = players
                    Log.d(TAG, "Players update received: $players")
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
            }

            // Füge Nachricht zur Liste hinzu
            val currentMessages = _connectionMessages.value.toMutableList()
            currentMessages.add(body)
            if (currentMessages.size > 50) { // Begrenze auf 50 Nachrichten
                currentMessages.removeAt(0)
            }
            _connectionMessages.value = currentMessages

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message body: ${e.message}")
        }
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
            playerId?.let { id ->
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
            append("\u0000")
        }

        webSocket?.send(disconnectFrame)
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null

        _connectionState.value = ConnectionState.DISCONNECTED
        _onlinePlayers.value = emptySet()
    }

    private fun sendAppClosingMessage() {
        playerId?.let { id ->
            val appClosingFrame = buildString {
                append("SEND\n")
                append("destination:/app/disconnect\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"app-closing","playerId":"$id","reason":"app-shutdown"}""")
                append("\u0000")
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

    fun sendAppClosingMessageWithReason(reason: String) {
        if(lobbyListener == null){
            Log.w(TAG, "LobbyListener is not set. Cannot leave lobby on app closing.")
        }else{
            // Neue Methode mit spezifischem Grund
            CoroutineScope(Dispatchers.IO).launch {
                val currentLobby = lobbyListener!!.currentLobby.value
                Log.d("StompWebSocket", "checking if in a lobby: current Lobby: $currentLobby")
                currentLobby?.let {
                    lobbyListener!!.leaveLobby(it.lobbyId)
                        .onSuccess {
                            Log.d("StompWebSocket", "Successfully left lobby ${currentLobby.lobbyId} during app closing")
                        }
                        .onFailure { exception ->
                            Log.e("StompWebSocket", "Failed to leave lobby during app closing", exception)
                        }
                }
            }
        }
        playerId?.let { id ->
            val appClosingFrame = buildString {
                append("SEND\n")
                append("destination:/app/disconnect\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"app-closing","playerId":"$id","reason":"$reason","timestamp":"${System.currentTimeMillis()}"}""")
                append("\u0000")
            }

            webSocket?.send(appClosingFrame)
            Log.d(TAG, "Sent app closing message for player: $id with reason: $reason")

            // Kurz warten damit Nachricht gesendet werden kann
            Thread.sleep(150)
        }
    }

    fun requestOnlinePlayers() {
        playerId?.let { id ->
            val requestFrame = buildString {
                append("SEND\n")
                append("destination:/app/players\n")
                append("content-type:application/json\n")
                append("\n")
                append("""{"type":"request","playerId":"$id"}""")
                append("\u0000")
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
            append("\u0000")
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
                append("\u0000")
            }

            Log.d(TAG, "Sending STOMP unsubscribe frame: ${unsubscribeFrame.replace("\u0000", "[NULL]")}")
            webSocket?.send(unsubscribeFrame)
            Log.d(TAG, "Unsubscribed from lobby: $lobbyId")
        }
        currentLobbyId = null
    }

    fun sendLobbyChat(message: String) {
        currentLobbyId?.let { lobbyId ->
            playerId?.let { id ->
                val chatFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/lobby/chat\n")
                    append("content-type:application/json\n")
                    append("\n")
                    append("""{"type":"chat","playerId":"$id","lobbyId":"$lobbyId","message":"$message"}""")
                    append("\u0000")
                }

                webSocket?.send(chatFrame)
                Log.d(TAG, "Sent lobby chat message: $message")
            }
        }
    }

    fun sendPlayerReady(ready: Boolean) {
        currentLobbyId?.let { lobbyId ->
            playerId?.let { id ->
                val readyFrame = buildString {
                    append("SEND\n")
                    append("destination:/app/lobby/ready\n")
                    append("content-type:application/json\n")
                    append("\n")
                    append("""{"type":"player-ready","playerId":"$id","lobbyId":"$lobbyId","ready":$ready}""")
                    append("\u0000")
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
}
