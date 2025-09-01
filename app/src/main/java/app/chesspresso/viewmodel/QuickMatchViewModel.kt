package app.chesspresso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.model.lobby.GameTime
import app.chesspresso.service.LobbyService
import app.chesspresso.websocket.StompWebSocketService
import app.chesspresso.model.lobby.RematchOffer
import app.chesspresso.model.lobby.RematchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickMatchViewModel @Inject constructor(
    private val lobbyService: LobbyService,
    private val webSocketService: StompWebSocketService
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickMatchUiState())
    val uiState: StateFlow<QuickMatchUiState> = _uiState.asStateFlow()

    private val _rematchDialogState = MutableStateFlow<RematchDialogState>(RematchDialogState.None)
    val rematchDialogState: StateFlow<RematchDialogState> = _rematchDialogState.asStateFlow()

    val isWaitingForMatch = lobbyService.isWaitingForMatch
    val lobbyError = lobbyService.lobbyError
    val gameStarted = lobbyService.gameStarted

    init {
        viewModelScope.launch {
            webSocketService.rematchOfferEvent.collectLatest { offer ->
                if (offer != null && offer.lobbyId == _uiState.value.lobbyId) {
                    _rematchDialogState.value = RematchDialogState.OfferReceived(offer)
                }
            }
        }
        viewModelScope.launch {
            webSocketService.rematchResultEvent.collectLatest { result ->
                if (result != null && result.lobbyId == _uiState.value.lobbyId) {
                    if (result.result == "accepted" && result.newlobbyid != null) {
                        _rematchDialogState.value = RematchDialogState.Accepted
                        webSocketService.resetGameFlows()
                        webSocketService.subscribeToLobby(result.newlobbyid)
                    }
                    else _rematchDialogState.value = RematchDialogState.Declined
                }
            }
        }
    }

    fun joinQuickMatch(gameTime: GameTime) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            lobbyService.joinQuickMatch(gameTime)
                .onSuccess { lobbyId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lobbyId = lobbyId,
                        selectedGameTime = gameTime
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

    fun reset() {
        cancelSearch()
        lobbyService.forceResetWaitingState()
        _uiState.value = QuickMatchUiState()
    }

    fun requestRematch() {
        _rematchDialogState.value = RematchDialogState.WaitingForResponse
        _uiState.value.lobbyId?.let { lobbyId ->
            webSocketService.sendRematchRequest(lobbyId)
        }
    }

    fun respondRematch(accept: Boolean) {
        _uiState.value.lobbyId?.let { lobbyId ->
            webSocketService.sendRematchResponse(lobbyId, if (accept) "accepted" else "declined")
            _rematchDialogState.value = RematchDialogState.WaitingForResult
        }
    }

    fun clearRematchDialog() {
        _rematchDialogState.value = RematchDialogState.None
    }
}

sealed class RematchDialogState {
    object None : RematchDialogState()
    object WaitingForResponse : RematchDialogState()
    data class OfferReceived(val offer: RematchOffer) : RematchDialogState()
    object WaitingForResult : RematchDialogState()
    object Accepted : RematchDialogState()
    object Declined : RematchDialogState()
}

data class QuickMatchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lobbyId: String? = null,
    val selectedGameTime: GameTime? = null
)
