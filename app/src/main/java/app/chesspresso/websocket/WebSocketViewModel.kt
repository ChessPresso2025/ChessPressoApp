package app.chesspresso.websocket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.websocket.StompWebSocketService.ConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebSocketViewModel @Inject constructor(
    private val webSocketService: StompWebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(WebSocketUiState())
    val uiState: StateFlow<WebSocketUiState> = _uiState.asStateFlow()

    init {
        observeWebSocketState()
    }

    private fun observeWebSocketState() {
        viewModelScope.launch {
            webSocketService.connectionState.collect { connectionState ->
                _uiState.value = _uiState.value.copy(
                    connectionState = connectionState,
                    isConnected = connectionState == ConnectionState.CONNECTED
                )
            }
        }

        viewModelScope.launch {
            webSocketService.onlinePlayers.collect { players ->
                _uiState.value = _uiState.value.copy(
                    onlinePlayers = players,
                    onlinePlayerCount = players.size
                )
            }
        }

        viewModelScope.launch {
            webSocketService.connectionMessages.collect { messages ->
                _uiState.value = _uiState.value.copy(
                    recentMessages = messages.takeLast(10) // Zeige nur die letzten 10 Nachrichten
                )
            }
        }
    }

    fun requestOnlinePlayers() {
        webSocketService.requestOnlinePlayers()
    }

    fun disconnect() {
        webSocketService.disconnect()
    }

    fun reconnect(username: String) {
        viewModelScope.launch {
            webSocketService.connect(username)
        }
    }

    fun getConnectionStatusText(): String {
        return when (_uiState.value.connectionState) {
            ConnectionState.DISCONNECTED -> "Getrennt"
            ConnectionState.CONNECTING -> "Verbinde..."
            ConnectionState.CONNECTED -> "Verbunden"
            ConnectionState.RECONNECTING -> "Wiederverbindung..."
        }
    }

    fun getConnectionStatusColor(): Long {
        return when (_uiState.value.connectionState) {
            ConnectionState.DISCONNECTED -> 0xFFFF5252 // Rot
            ConnectionState.CONNECTING -> 0xFFFFC107 // Gelb
            ConnectionState.CONNECTED -> 0xFF4CAF50 // Grün
            ConnectionState.RECONNECTING -> 0xFFFF9800 // Orange
        }
    }
}

data class WebSocketUiState(
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
    val isConnected: Boolean = false,
    val onlinePlayers: Set<String> = emptySet(),
    val onlinePlayerCount: Int = 0,
    val recentMessages: List<String> = emptyList()
)
