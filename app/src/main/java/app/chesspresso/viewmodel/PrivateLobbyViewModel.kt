package app.chesspresso.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.model.lobby.ConfigureLobbyMessage
import app.chesspresso.model.lobby.GameTime
import app.chesspresso.model.lobby.Lobby
import app.chesspresso.service.LobbyService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivateLobbyViewModel @Inject constructor(
    private val lobbyService: LobbyService
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivateLobbyUiState())
    val uiState: StateFlow<PrivateLobbyUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<String?>(null)
    val navigationEvent: StateFlow<String?> = _navigationEvent.asStateFlow()

    val currentLobby = lobbyService.currentLobby
    val lobbyError = lobbyService.lobbyError
    val gameStarted = lobbyService.gameStarted

    fun createPrivateLobby() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            lobbyService.createPrivateLobby()
                .onSuccess { lobbyCode ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        createdLobbyCode = lobbyCode,
                        isLobbyCreated = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun joinPrivateLobby(lobbyCode: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            lobbyService.joinPrivateLobby(lobbyCode)
                .onSuccess { code ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        joinedLobbyCode = code,
                        isLobbyJoined = true
                    )
                    // Lobby-Info laden
                    loadLobbyInfo(code)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
        }
    }

    fun updateJoinCode(code: String) {
        _uiState.value = _uiState.value.copy(joinCode = code.uppercase())
    }

    private fun loadLobbyInfo(lobbyCode: String) {
        viewModelScope.launch {
            lobbyService.getLobbyInfo(lobbyCode)
        }
    }

    fun leaveLobby() {
        viewModelScope.launch {
            val lobbyCode = _uiState.value.createdLobbyCode ?: _uiState.value.joinedLobbyCode
            Log.d("PrivateLobbyViewModel", "Verlasse Lobby: $lobbyCode")
            lobbyCode?.let { code ->
                lobbyService.leaveLobby(code)
                resetState()
                _navigationEvent.value = "home"
            }
        }
    }

    fun clearError() {
        lobbyService.clearError()
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearGameStart() {
        lobbyService.clearGameStart()
    }

    private fun resetState() {
        _uiState.value = PrivateLobbyUiState()
    }

    fun configureAndStartGame(
        lobbyCode: String,
        gameTime: GameTime,
        whitePlayer: String? = null,
        blackPlayer: String? = null,
        randomColors: Boolean = false
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)


            val configMessage = ConfigureLobbyMessage(
                lobbyCode = lobbyCode,
                gameTime = gameTime,
                whitePlayer = whitePlayer,
                blackPlayer = blackPlayer,
                randomColors = randomColors
            )

            // Für jetzt loggen wir die Konfiguration
            Log.d("PrivateLobbyViewModel", "Konfiguriere Spiel: $configMessage")

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun refreshLobbyInfo(lobbyCode: String? = null) {
        viewModelScope.launch {
            val codeToUse = lobbyCode ?: _uiState.value.createdLobbyCode ?: _uiState.value.joinedLobbyCode
            codeToUse?.let { code ->
                lobbyService.getLobbyInfo(code)
                    .onSuccess { lobby ->
                        // Stelle sicher, dass der Lobby-Code im uiState gespeichert ist
                        if (_uiState.value.createdLobbyCode == null && _uiState.value.joinedLobbyCode == null) {
                            _uiState.value = _uiState.value.copy(
                                joinedLobbyCode = code
                            )
                            Log.d("PrivateLobbyViewModel", "LobbyCode im uiState aktualisiert: $code")
                        }
                    }
                    .onFailure { exception ->
                        resetState()
                        _navigationEvent.value = "home"
                        Log.e("PrivateLobbyViewModel", "Fehler beim Laden der Lobby-Info", exception)
                    }
            }
        }
    }

    fun setReady(lobbyId: String, ready: Boolean) {
        viewModelScope.launch {
            lobbyService.setPlayerReady(lobbyId, ready)
        }
    }

    fun onNavigated() {
        _navigationEvent.value = null
    }
}

data class PrivateLobbyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinCode: String = "",
    val createdLobbyCode: String? = null,
    val joinedLobbyCode: String? = null,
    val isLobbyCreated: Boolean = false,
    val isLobbyJoined: Boolean = false
)
