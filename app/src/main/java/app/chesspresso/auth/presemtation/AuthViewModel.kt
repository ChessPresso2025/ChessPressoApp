package app.chesspresso.auth.presemtation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import app.chesspresso.auth.data.AuthRepository
import app.chesspresso.auth.data.AuthResponse
import app.chesspresso.auth.data.PlayerInfo
import app.chesspresso.websocket.WebSocketManager
import android.util.Log

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

    fun loginWithGoogle(idToken: String) {
        Log.d("AuthViewModel", "Starting Google login with token length: ${idToken.length}")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Calling repository to send token to server")
                val response = repository.sendTokenToServer(idToken)
                Log.d("AuthViewModel", "Login successful for user: ${response.name}")
                _authState.value = AuthState.Success(response)
                // Automatische WebSocket-Verbindung nach erfolgreicher Anmeldung
                connectToWebSocket()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("ConnectException") == true -> "Server nicht erreichbar. Ist der Backend-Server gestartet?"
                    e.message?.contains("SocketTimeoutException") == true -> "Verbindung zum Server zeitüberschritten"
                    e.message?.contains("UnknownHostException") == true -> "Server-Adresse nicht gefunden"
                    e.message?.contains("HTTP") == true -> "Server-Fehler: ${e.message}"
                    else -> e.message ?: "Unbekannter Fehler bei der Anmeldung"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    fun loginWithGoogleAlternative(accountId: String, email: String) {
        Log.d("AuthViewModel", "Starting alternative Google login")
        Log.d("AuthViewModel", "Account ID: $accountId")
        Log.d("AuthViewModel", "Email: $email")
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                Log.d("AuthViewModel", "Calling repository to send alternative auth to server")
                val response = repository.sendAlternativeTokenToServer(accountId, email)
                Log.d("AuthViewModel", "Alternative login successful for user: ${response.name}")
                _authState.value = AuthState.Success(response)
                // Automatische WebSocket-Verbindung nach erfolgreicher Anmeldung
                connectToWebSocket()
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Alternative login failed: ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("ConnectException") == true -> "Server nicht erreichbar. Ist der Backend-Server gestartet?"
                    e.message?.contains("SocketTimeoutException") == true -> "Verbindung zum Server zeitüberschritten"
                    e.message?.contains("UnknownHostException") == true -> "Server-Adresse nicht gefunden"
                    e.message?.contains("HTTP") == true -> "Server-Fehler: ${e.message}"
                    else -> e.message ?: "Unbekannter Fehler bei der Anmeldung"
                }
                _authState.value = AuthState.Error(errorMessage)
            }
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
        }
    }

    fun getStoredPlayerInfo(): PlayerInfo? {
        return repository.getStoredPlayerInfo()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}