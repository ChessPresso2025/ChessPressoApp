package app.chesspresso.utils

import android.content.Context
import android.content.SharedPreferences
import app.chesspresso.auth.data.AuthRepository
import app.chesspresso.auth.data.PlayerInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerIdManager @Inject constructor(
    private val authRepository: AuthRepository
) {

    fun getPlayerId(): String? {
        return authRepository.getStoredPlayerInfo()?.playerId
    }

    fun getPlayerInfo(): PlayerInfo? {
        return authRepository.getStoredPlayerInfo()
    }

    fun isPlayerLoggedIn(): Boolean {
        return getPlayerId() != null
    }

    fun getPlayerName(): String? {
        return authRepository.getStoredPlayerInfo()?.name
    }
}