package app.chesspresso.websocket

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

object WebSocketManager : WebSocketListener(){
    private const val SOCKET_URL = "ws://10.0.2.2:8080" // Emulator IP
    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var isWebSocketConnected = false

    private var playerId: String =""
    private var currentLobbyId: String? = null

    // Callback-Funktionen für Verbindungsstatus
    private var onConnectionSuccess: (() -> Unit)? = null
    private var onConnectionFailure: ((String) -> Unit)? = null
    private var onDisconnected: (() -> Unit)? = null

    // Callback für Nachrichten
    private var onMessageReceived: ((String) -> Unit)? = null

    fun isConnected(): Boolean = isWebSocketConnected && webSocket != null

    fun setMessageHandler(handler: (String) -> Unit) {
        onMessageReceived = handler
    }

    fun init (playerId: String,
              onSuccess: (() -> Unit)? = null,
              onFailure: ((String) -> Unit)? = null,
              onDisconnect: (() -> Unit)? = null,
              onMessage: ((String) -> Unit)? = null) {

        // Verhindere mehrfache Initialisierung
        if (isConnected()) {
            Log.d("WebSocket", "WebSocket bereits verbunden - überspringe Initialisierung")
            onMessage?.let { setMessageHandler(it) }
            onSuccess?.invoke()
            return
        }

        this.playerId = playerId
        this.onConnectionSuccess = onSuccess
        this.onConnectionFailure = onFailure
        this.onDisconnected = onDisconnect
        this.onMessageReceived = onMessage
        connect()
    }

    private fun connect() {
        val request = Request.Builder().url(SOCKET_URL).build()
        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, this)
    }

    fun setLobby(lobbyId: String?) {
        currentLobbyId = lobbyId
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Verbindung erfolgreich hergestellt")
        isWebSocketConnected = true
        onConnectionSuccess?.invoke()
        startHeartbeat()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "Nachricht vom Server: $text")
        onMessageReceived?.invoke(text)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "Verbindung fehlgeschlagen: ${t.message}")
        isWebSocketConnected = false
        onConnectionFailure?.invoke(t.message ?: "Unbekannter Fehler")
        stopHeartbeat()
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i("WebSocket", "Verbindung geschlossen: $reason")
        isWebSocketConnected = false
        onDisconnected?.invoke()
        stopHeartbeat()
    }

    private fun startHeartbeat() {
        heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val lobby = currentLobbyId ?: "NoLobby"
                val message = buildHeartbeatMessage(playerId, lobby)
                webSocket?.send(message)
                delay(30_000)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
    }

    fun disconnect() {
        stopHeartbeat()
        isWebSocketConnected = false
        webSocket?.close(1000, "App geschlossen")
        onDisconnected?.invoke()
    }

    private fun buildHeartbeatMessage(playerId: String, lobbyId: String): String {
        return """
            {
              "type": "heartbeat",
              "playerId": "$playerId",
              "lobbyId": "$lobbyId"
            }
        """.trimIndent()
    }

}