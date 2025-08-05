package app.chesspresso.auth.data

import android.content.Context
import android.util.Log

class AuthRepository(private val api: AuthApi, private val context: Context) {

    suspend fun sendTokenToServer(idToken: String): AuthResponse {
        Log.d("AuthRepository", "Sending token to server, token length: ${idToken.length}")
        Log.d("AuthRepository", "Token preview: ${idToken.take(50)}...")

        try {
            val response = api.login(AuthRequest(idToken))
            Log.d("AuthRepository", "Server response received successfully")
            Log.d("AuthRepository", "Player ID: ${response.playerId}")
            Log.d("AuthRepository", "Player Name: ${response.name}")

            // Lokale Speicherung aller Player-Daten
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

            Log.d("AuthRepository", "Player data stored locally")
            return response
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error sending token to server: ${e.message}", e)
            Log.e("AuthRepository", "Exception type: ${e.javaClass.simpleName}")

            // Re-throw mit mehr Details
            throw Exception("Server-Kommunikation fehlgeschlagen: ${e.message}", e)
        }
    }

    suspend fun sendAlternativeTokenToServer(accountId: String, email: String): AuthResponse {
        Log.d("AuthRepository", "Sending alternative auth to server")
        Log.d("AuthRepository", "Account ID: $accountId")
        Log.d("AuthRepository", "Email: $email")

        try {
            // Erstelle ein alternatives Token aus Account-Daten
            val alternativeToken = "google_account_${accountId}_${email}"
            Log.d("AuthRepository", "Alternative token created: ${alternativeToken.take(50)}...")

            val response = api.login(AuthRequest(alternativeToken))
            Log.d("AuthRepository", "Server response received successfully")
            Log.d("AuthRepository", "Player ID: ${response.playerId}")
            Log.d("AuthRepository", "Player Name: ${response.name}")

            // Lokale Speicherung aller Player-Daten
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

            Log.d("AuthRepository", "Player data stored locally")
            return response
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error sending alternative token to server: ${e.message}", e)
            Log.e("AuthRepository", "Exception type: ${e.javaClass.simpleName}")

            // Re-throw mit mehr Details
            throw Exception("Server-Kommunikation fehlgeschlagen: ${e.message}", e)
        }
    }

    fun getStoredPlayerInfo(): PlayerInfo? {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        val playerId = prefs.getString("playerId", null)
        val playerName = prefs.getString("playerName", null)

        return if (playerId != null && playerName != null) {
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
