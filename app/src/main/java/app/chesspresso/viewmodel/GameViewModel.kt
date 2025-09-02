package app.chesspresso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.data.models.StatsResponse
import app.chesspresso.data.repository.GameRepository
import app.chesspresso.model.game.GameHistoryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun loadStats() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            gameRepository.getMyStats()
                .onSuccess { stats ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        stats = stats
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Stats laden fehlgeschlagen"
                    )
                }
        }
    }

    fun loadGameHistory() {
        _uiState.value = _uiState.value.copy(isHistoryLoading = true, historyErrorMessage = null)
        viewModelScope.launch {
            gameRepository.getGameHistory()
                .onSuccess { history ->
                    _uiState.value = _uiState.value.copy(
                        isHistoryLoading = false,
                        gameHistory = history
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isHistoryLoading = false,
                        historyErrorMessage = exception.message ?: "Fehler beim Laden der Spielhistorie"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearHistoryMessages() {
        _uiState.value = _uiState.value.copy(
            historyErrorMessage = null
        )
    }

    fun reset() {
        _uiState.value = GameUiState()
    }
}

data class GameUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val stats: StatsResponse? = null,
    val isHistoryLoading: Boolean = false,
    val historyErrorMessage: String? = null,
    val gameHistory: List<GameHistoryDto>? = null
)
