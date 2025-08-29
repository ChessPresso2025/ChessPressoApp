package app.chesspresso.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.data.api.StatsResponse
import app.chesspresso.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(val stats: StatsResponse) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {
    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val statsState: StateFlow<StatsUiState> = _statsState

    fun loadStats() {
        _statsState.value = StatsUiState.Loading
        viewModelScope.launch {
            try {
                val stats = statsRepository.getMyStats()
                _statsState.value = StatsUiState.Success(stats)
            } catch (e: Exception) {
                _statsState.value = StatsUiState.Error(e.localizedMessage ?: "Unbekannter Fehler")
            }
        }
    }
}

