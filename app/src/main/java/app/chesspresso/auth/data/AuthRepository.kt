package app.chesspresso.auth.data

import android.content.Context
import android.util.Log
import app.chesspresso.data.storage.TokenStorage
import app.chesspresso.websocket.StompWebSocketService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import app.chesspresso.data.api.AuthApi as JwtAuthApi
import app.chesspresso.data.models.LoginRequest as JwtLoginRequest
import app.chesspresso.data.models.RegisterRequest as JwtRegisterRequest
import androidx.core.content.edit

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val jwtApi: JwtAuthApi,
    private val tokenStorage: TokenStorage,
    private val context: Context,
    private val webSocketService: StompWebSocketService
) {

    suspend fun login(username: String, password: String): AuthResponse {
        Log.d("AuthRepository", "Attempting JWT login for user: $username")

        // Zuerst JWT-Login versuchen
        try {
            val jwtRequest = JwtLoginRequest(username, password)
            val jwtResponse = jwtApi.login(jwtRequest)

            if (jwtResponse.isSuccessful && jwtResponse.body() != null) {
                val tokenResponse = jwtResponse.body()!!
                tokenStorage.saveToken(tokenResponse.accessToken)
                Log.d("AuthRepository", "JWT login successful, token saved")

                // Erstelle AuthResponse basierend auf JWT-Response (ohne altes System zu verwenden)
                val authResponse = AuthResponse(
                    playerId = "jwt_user_" + System.currentTimeMillis(), // Temporäre ID bis echte Stats verfügbar sind
                    name = tokenResponse.username ?: username,
                    email = "", // Wird später über API geholt
                    playedGames = 0,
                    win = 0,
                    draw = 0,
                    lose = 0
                )

                storePlayerData(authResponse)
                storeCredentials(authResponse.name)
                Log.d("AuthRepository", "JWT Player data stored locally")

                // Nach erfolgreicher Authentifizierung WebSocket-Verbindung aufbauen
                try {
                    webSocketService.connect(authResponse.name)
                    Log.d(
                        "AuthRepository",
                        "WebSocket connection initiated for user: ${authResponse.name}"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "AuthRepository",
                        "Failed to establish WebSocket connection: ${e.message}"
                    )
                    // WebSocket-Fehler soll Login nicht fehlschlagen lassen
                }

                return authResponse
            } else {
                throw Exception("JWT Login failed: ${jwtResponse.message()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "JWT login failed, falling back to old system: ${e.message}")
            // Fallback zum alten System nur wenn JWT komplett fehlschlägt
            val fallbackResponse = processAuthRequest(
                authAction = { api.login(LoginRequest(username, password)) },
                actionType = "login"
            )

            // Auch bei Fallback WebSocket-Verbindung versuchen
            try {
                webSocketService.connect(fallbackResponse.name)
                Log.d(
                    "AuthRepository",
                    "WebSocket connection initiated for fallback user: ${fallbackResponse.name}"
                )
            } catch (wsError: Exception) {
                Log.e(
                    "AuthRepository",
                    "Failed to establish WebSocket connection for fallback: ${wsError.message}"
                )
            }

            return fallbackResponse
        }
    }

    suspend fun register(username: String, password: String, email: String): AuthResponse {
        Log.d("AuthRepository", "Attempting JWT registration for user: $username")

        // Zuerst JWT-Registrierung versuchen
        try {
            val jwtRequest = JwtRegisterRequest(username, email, password)
            val jwtResponse = jwtApi.register(jwtRequest)

            if (jwtResponse.isSuccessful && jwtResponse.body() != null) {
                val tokenResponse = jwtResponse.body()!!
                tokenStorage.saveToken(tokenResponse.accessToken)
                Log.d("AuthRepository", "JWT registration successful, token saved")

                // Erstelle AuthResponse basierend auf JWT-Response (ohne altes System zu verwenden)
                val authResponse = AuthResponse(
                    playerId = "jwt_user_" + System.currentTimeMillis(), // Temporäre ID bis echte Stats verfügbar sind
                    name = tokenResponse.username ?: username,
                    email = email,
                    playedGames = 0,
                    win = 0,
                    draw = 0,
                    lose = 0
                )

                storePlayerData(authResponse)
                storeCredentials(authResponse.name)
                Log.d("AuthRepository", "JWT Player data stored locally")

                // Nach erfolgreicher Registrierung WebSocket-Verbindung aufbauen
                try {
                    webSocketService.connect(authResponse.name)
                    Log.d(
                        "AuthRepository",
                        "WebSocket connection initiated for registered user: ${authResponse.name}"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "AuthRepository",
                        "Failed to establish WebSocket connection: ${e.message}"
                    )
                    // WebSocket-Fehler soll Registrierung nicht fehlschlagen lassen
                }

                return authResponse
            } else {
                throw Exception("JWT Registration failed: ${jwtResponse.message()}")
            }
        } catch (e: Exception) {
            Log.e(
                "AuthRepository",
                "JWT registration failed, falling back to old system: ${e.message}"
            )
            // Fallback zum alten System nur wenn JWT komplett fehlschlägt
            val fallbackResponse = processAuthRequest(
                authAction = { api.register(RegisterRequest(username, password, email)) },
                actionType = "registration"
            )

            // Auch bei Fallback WebSocket-Verbindung versuchen
            try {
                webSocketService.connect(fallbackResponse.name)
                Log.d(
                    "AuthRepository",
                    "WebSocket connection initiated for fallback registered user: ${fallbackResponse.name}"
                )
            } catch (wsError: Exception) {
                Log.e(
                    "AuthRepository",
                    "Failed to establish WebSocket connection for fallback: ${wsError.message}"
                )
            }

            return fallbackResponse
        }
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
        prefs.edit {
            putString("playerId", response.playerId)
                .putString("playerName", response.name)
                .putString("playerEmail", response.email)
                .putInt("playedGames", response.playedGames)
                .putInt("win", response.win)
                .putInt("draw", response.draw)
                .putInt("lose", response.lose)
        }
    }

    private fun storeCredentials(username: String) {
        val prefs = context.getSharedPreferences("chessapp", Context.MODE_PRIVATE)
        prefs.edit {
            putString("storedUsername", username)
                .putBoolean("isLoggedIn", true)
        }
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
        prefs.edit { clear() }

        // Auch JWT Token löschen
        kotlinx.coroutines.runBlocking {
            tokenStorage.clearToken()
        }
    }

    suspend fun getJwtToken(): String? {
        return tokenStorage.getToken().first()
    }

    suspend fun isLoggedInWithJwt(): Boolean {
        return getJwtToken() != null
    }

    fun logout() {
        Log.d("AuthRepository", "Logging out user")

        // WebSocket-Verbindung trennen
        webSocketService.disconnect()

        // Lokale Daten löschen
        clearStoredPlayerInfo()

        Log.d("AuthRepository", "User logged out successfully")
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
