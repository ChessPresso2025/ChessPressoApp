package app.chesspresso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.model.lobby.GameDuration
import app.chesspresso.service.LobbyService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickMatchViewModel @Inject constructor(
    private val lobbyService: LobbyService
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickMatchUiState())
    val uiState: StateFlow<QuickMatchUiState> = _uiState.asStateFlow()

    val isWaitingForMatch = lobbyService.isWaitingForMatch
    val lobbyError = lobbyService.lobbyError
    val gameStarted = lobbyService.gameStarted

    fun joinQuickMatch(gameDuration: GameDuration) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            lobbyService.joinQuickMatch(gameDuration)
                .onSuccess { lobbyId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lobbyId = lobbyId,
                        selectedGameDuration = gameDuration
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

    fun cancelSearch() {
        viewModelScope.launch {
            _uiState.value.lobbyId?.let { lobbyId ->
                lobbyService.leaveLobby(lobbyId)
                _uiState.value = QuickMatchUiState()
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
}

data class QuickMatchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lobbyId: String? = null,
    val selectedGameDuration: GameDuration? = null
)
