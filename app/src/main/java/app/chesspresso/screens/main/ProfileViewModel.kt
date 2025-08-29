package app.chesspresso.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.data.api.StatsResponse
import app.chesspresso.data.api.UserApi
import app.chesspresso.data.api.ChangeUsernameRequest
import app.chesspresso.data.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class StatsUiState {
    object Idle : StatsUiState()
    object Loading : StatsUiState()
    data class Success(val stats: StatsResponse) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

sealed class UsernameChangeState {
    object Idle : UsernameChangeState()
    object Loading : UsernameChangeState()
    object Success : UsernameChangeState()
    data class Error(val message: String) : UsernameChangeState()
}

sealed class PasswordChangeState {
    object Idle : PasswordChangeState()
    object Loading : PasswordChangeState()
    object Success : PasswordChangeState()
    data class Error(val message: String) : PasswordChangeState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val userApi: UserApi
) : ViewModel() {
    private val _statsState = MutableStateFlow<StatsUiState>(StatsUiState.Idle)
    val statsState: StateFlow<StatsUiState> = _statsState

    private val _usernameChangeState = MutableStateFlow<UsernameChangeState>(UsernameChangeState.Idle)
    val usernameChangeState: StateFlow<UsernameChangeState> = _usernameChangeState.asStateFlow()

    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    private val _eventChannel = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

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

    fun changeUsername(newUsername: String) {
        _usernameChangeState.value = UsernameChangeState.Loading
        viewModelScope.launch {
            try {
                val response = userApi.changeUsername(ChangeUsernameRequest(newUsername))
                if (response.isSuccessful) {
                    _usernameChangeState.value = UsernameChangeState.Success
                    _eventChannel.send(ProfileEvent.LogoutAndNavigateToLogin)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unbekannter Fehler"
                    _usernameChangeState.value = UsernameChangeState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _usernameChangeState.value = UsernameChangeState.Error(e.localizedMessage ?: "Unbekannter Fehler")
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        _passwordChangeState.value = PasswordChangeState.Loading
        viewModelScope.launch {
            try {
                val response = userApi.changePassword(
                    app.chesspresso.data.api.ChangePasswordRequest(oldPassword, newPassword)
                )
                if (response.isSuccessful) {
                    _passwordChangeState.value = PasswordChangeState.Success
                    _eventChannel.send(ProfileEvent.LogoutAndNavigateToLogin)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unbekannter Fehler"
                    _passwordChangeState.value = PasswordChangeState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _passwordChangeState.value = PasswordChangeState.Error(e.localizedMessage ?: "Unbekannter Fehler")
            }
        }
    }

    fun resetUsernameChangeState() {
        _usernameChangeState.value = UsernameChangeState.Idle
    }

    fun resetPasswordChangeState() {
        _passwordChangeState.value = PasswordChangeState.Idle
    }

    fun resetStatsState() {
        _statsState.value = StatsUiState.Idle
    }
}

sealed class ProfileEvent {
    object LogoutAndNavigateToLogin : ProfileEvent()
}
