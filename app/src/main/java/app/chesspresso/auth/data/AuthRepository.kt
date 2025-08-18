package app.chesspresso.auth.data

import android.content.Context
import android.util.Log

class AuthRepository(private val api: AuthApi, private val context: Context) {

    suspend fun login(username: String, password: String): AuthResponse {
        Log.d("AuthRepository", "Attempting login for user: $username")
        return processAuthRequest(
            authAction = { api.login(LoginRequest(username, password)) },
            actionType = "login"
        )
    }

    suspend fun register(username: String, password: String, email: String): AuthResponse {
        Log.d("AuthRepository", "Attempting registration for user: $username")
        return processAuthRequest(
            authAction = { api.register(RegisterRequest(username, password, email)) },
            actionType = "registration"
        )
    }

    private suspend fun processAuthRequest(
        authAction: suspend () -> AuthResponse,
        actionType: String
    ): AuthResponse {
        try {
            val response = authAction()
            Log.d("AuthRepository", "Server response received successfully for $actionType")
            Log.d("AuthRepository", "Player ID: ${response.playerId}")
            Log.d("AuthRepository", "Player Name: ${response.name}")

            storePlayerData(response)
            storeCredentials(response.name) // Store username for auto-login
            Log.d("AuthRepository", "Player data stored locally")
            return response
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during $actionType: ${e.message}", e)
            Log.e("AuthRepository", "Exception type: ${e.javaClass.simpleName}")
            throw Exception("Server-Kommunikation fehlgeschlagen: ${e.message}", e)
        }
    }

    private fun storePlayerData(response: AuthResponse) {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("playerId", response.playerId)
            .putString("playerName", response.name)
            .putString("playerEmail", response.email)
            .putInt("playedGames", response.playedGames)
            .putInt("win", response.win)
            .putInt("draw", response.draw)
            .putInt("lose", response.lose)
            .apply()
    }

    private fun storeCredentials(username: String) {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("storedUsername", username)
            .putBoolean("isLoggedIn", true)
            .apply()
    }

    fun getStoredPlayerInfo(): PlayerInfo? {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        val playerId = prefs.getString("playerId", null)
        val playerName = prefs.getString("playerName", null)
        val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

        return if (playerId != null && playerName != null && isLoggedIn) {
            PlayerInfo(
                playerId = playerId,
                name = playerName,
                email = prefs.getString("playerEmail", ""),
                playedGames = prefs.getInt("playedGames", 0),
                win = prefs.getInt("win", 0),
                draw = prefs.getInt("draw", 0),
                lose = prefs.getInt("lose", 0)
            )
        } else {
            null
        }
    }

    fun getStoredUsername(): String? {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        return prefs.getString("storedUsername", null)
    }

    fun clearStoredPlayerInfo() {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

data class PlayerInfo(
    val playerId: String,
    val name: String,
    val email: String?,
    val playedGames: Int,
    val win: Int,
    val draw: Int,
    val lose: Int
)
