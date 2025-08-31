package app.chesspresso.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.chesspresso.data.api.ChangeUsernameRequest
import app.chesspresso.data.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val userApi: UserApi
) : ViewModel() {
    // --- UserProfile State ---
    private val _userProfileState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val userProfileState: StateFlow<UserProfileUiState> = _userProfileState.asStateFlow()

    private val _usernameChangeState = MutableStateFlow<UsernameChangeState>(UsernameChangeState.Idle)
    val usernameChangeState: StateFlow<UsernameChangeState> = _usernameChangeState.asStateFlow()

    private val _passwordChangeState = MutableStateFlow<PasswordChangeState>(PasswordChangeState.Idle)
    val passwordChangeState: StateFlow<PasswordChangeState> = _passwordChangeState.asStateFlow()

    private val _eventChannel = Channel<ProfileEvent>(Channel.BUFFERED)
    val events = _eventChannel.receiveAsFlow()

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

    fun loadUserProfile() {
        viewModelScope.launch {
            _userProfileState.value = UserProfileUiState.Loading
            try {
                val profile = userApi.getProfile()
                _userProfileState.value = UserProfileUiState.Success(profile)
            } catch (e: Exception) {
                _userProfileState.value = UserProfileUiState.Error(e.localizedMessage ?: "Unbekannter Fehler")
            }
        }
    }
}

sealed class ProfileEvent {
    object LogoutAndNavigateToLogin : ProfileEvent()
}

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    data class Success(val profile: app.chesspresso.data.api.UserProfileResponse) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
}
