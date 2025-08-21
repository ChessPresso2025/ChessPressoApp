package app.chesspresso.auth.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.auth.data.AuthRepository
import app.chesspresso.auth.data.AuthResponse
import app.chesspresso.auth.data.PlayerInfo
import app.chesspresso.websocket.StompWebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val webSocketService: StompWebSocketService
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
                // WebSocket-Verbindung wird bereits im AuthRepository aufgebaut
                Log.d("AuthViewModel", "WebSocket connection handled by AuthRepository")
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

    fun logout() {
        viewModelScope.launch {
            // Sende App-Closing-Nachricht vor dem Logout
            try {
                webSocketService.sendAppClosingMessageSync()
                Log.d("AuthViewModel", "App closing message sent before logout")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Failed to send app closing message before logout: ${e.message}")
            }
            
            repository.logout() // Verwendet jetzt die neue logout() Methode die auch WebSocket trennt
            _authState.value = AuthState.Idle
            Log.d("AuthViewModel", "User logged out successfully")
        }
    }

    private fun checkStoredAuth() {
        viewModelScope.launch {
            val playerInfo = repository.getStoredPlayerInfo()
            if (playerInfo != null) {
                Log.d("AuthViewModel", "Found stored auth for user: ${playerInfo.name}")

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

                // Wichtig: STOMP WebSocket-Verbindung für automatische Anmeldung herstellen
                try {
                    webSocketService.connect(playerInfo.name)
                    Log.d("AuthViewModel", "STOMP WebSocket connection initiated for auto-login user: ${playerInfo.name}")
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Failed to establish STOMP WebSocket connection for auto-login: ${e.message}")
                }
            } else {
                Log.d("AuthViewModel", "No stored authentication found")
            }
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