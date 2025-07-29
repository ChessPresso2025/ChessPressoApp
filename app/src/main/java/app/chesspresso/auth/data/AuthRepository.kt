package app.chesspresso.auth.data

import android.content.Context

class AuthRepository(private val api: AuthApi, private val context: Context) {

    suspend fun sendTokenToServer(idToken: String): AuthResponse {
        val response = api.login(AuthRequest(idToken))

        // Lokale Speicherung
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("playerId", response.PlayerID)
            .putString("playerName", response.name)
            .apply()

        return response
    }

    fun getStoredPlayerInfo(): Pair<String?, String?> {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        return Pair(
            prefs.getString("playerId", null),
            prefs.getString("playerName", null)
        )
    }
}