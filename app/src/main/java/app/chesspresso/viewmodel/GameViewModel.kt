package app.chesspresso.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.data.models.StatsResponse
import app.chesspresso.data.repository.GameRepository
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

    fun sendTestEvent() {
        viewModelScope.launch {
            val payload = mapOf(
                "sdk" to "android",
                "ts" to System.currentTimeMillis(),
                "test" to true
            )

            gameRepository.sendEvent("CLIENT_TEST", payload)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Test Event gesendet"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Event senden fehlgeschlagen"
                    )
                }
        }
    }

    fun sendEvent(type: String, payload: Map<String, Any>) {
        viewModelScope.launch {
            gameRepository.sendEvent(type, payload)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Event '$type' gesendet"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "Event senden fehlgeschlagen"
                    )
                }
        }
    }

    fun reportWin() {
        reportResult("WIN")
    }

    fun reportLoss() {
        reportResult("LOSS")
    }

    fun reportDraw() {
        reportResult("DRAW")
    }

    private fun reportResult(result: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            gameRepository.reportResult(result)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "$result reportiert"
                    )
                    // Stats nach dem Reporten aktualisieren
                    loadStats()
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Ergebnis reporten fehlgeschlagen"
                    )
                }
        }
    }

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

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}

data class GameUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val stats: StatsResponse? = null
)
