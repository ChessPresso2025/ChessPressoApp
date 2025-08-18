package app.chesspresso.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.auth.data.AuthRepository
import app.chesspresso.auth.data.AuthResponse
import app.chesspresso.auth.data.PlayerInfo
import app.chesspresso.websocket.WebSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Prüfe beim Start, ob bereits ein eingeloggter Spieler vorhanden ist
        checkStoredAuth()
    }

    fun setErrorMessage(message: String) {
        _authState.value = AuthState.Error(message)
    }

    fun login(username: String, password: String) {
        Log.d("AuthViewModel", "Starting login for user: $username")
        performAuthAction(
            authAction = { repository.login(username, password) },
            actionType = "Login"
        )
    }

    fun register(username: String, password: String, email: String) {
        Log.d("AuthViewModel", "Starting registration for user: $username")
        performAuthAction(
            authAction = { repository.register(username, password, email) },
            actionType = "Registration"
        )
    }

    private fun performAuthAction(
        authAction: suspend () -> AuthResponse,
        actionType: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Calling repository for $actionType")
                val response = authAction()
                Log.d("AuthViewModel", "$actionType successful for user: ${response.name}")
                _authState.value = AuthState.Success(response)
                connectToWebSocket()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "$actionType failed: ${e.message}", e)
                _authState.value = AuthState.Error(mapErrorMessage(e))
            }
        }
    }

    private fun mapErrorMessage(e: Exception): String {
        return when {
            e.message?.contains("ConnectException") == true -> "Server nicht erreichbar. Ist der Backend-Server gestartet?"
            e.message?.contains("SocketTimeoutException") == true -> "Verbindung zum Server zeitüberschritten"
            e.message?.contains("UnknownHostException") == true -> "Server-Adresse nicht gefunden"
            e.message?.contains("HTTP") == true -> "Server-Fehler: ${e.message}"
            else -> e.message ?: "Unbekannter Fehler bei der Anmeldung"
        }
    }

    private fun connectToWebSocket() {
        try {
            Log.d("AuthViewModel", "Starting automatic WebSocket connection after login...")
            val playerInfo = repository.getStoredPlayerInfo()
            val playerId = playerInfo?.playerId ?: "anonymous_user"

            webSocketManager.init(
                playerId = playerId,
                onSuccess = {
                    Log.d("AuthViewModel", "WebSocket connection successful")
                },
                onFailure = { error ->
                    Log.e("AuthViewModel", "WebSocket connection failed: $error")
                },
                onDisconnect = {
                    Log.d("AuthViewModel", "WebSocket disconnected")
                }
            )
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to connect to WebSocket: ${e.message}", e)
        }
    }

    fun logout() {
        repository.clearStoredPlayerInfo()
        webSocketManager.disconnect()
        _authState.value = AuthState.Idle
    }

    private fun checkStoredAuth() {
        val playerInfo = repository.getStoredPlayerInfo()
        if (playerInfo != null) {
            // Konvertiere PlayerInfo zu AuthResponse für konsistente State-Behandlung
            val authResponse = AuthResponse(
                playerId = playerInfo.playerId,
                name = playerInfo.name,
                email = playerInfo.email ?: "",
                playedGames = playerInfo.playedGames,
                win = playerInfo.win,
                draw = playerInfo.draw,
                lose = playerInfo.lose
            )
            _authState.value = AuthState.Success(authResponse)

            // Wichtig: WebSocket-Verbindung auch beim automatischen Login herstellen
            connectToWebSocket()
        }
    }

    fun getStoredPlayerInfo(): PlayerInfo? {
        return repository.getStoredPlayerInfo()
    }

    fun getStoredUsername(): String? {
        return repository.getStoredUsername()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}