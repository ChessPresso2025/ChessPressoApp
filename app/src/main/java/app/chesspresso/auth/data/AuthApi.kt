package app.chesspresso.auth.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/google")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}

data class AuthRequest(
    val idToken: String
)

data class AuthResponse(
    val playerId: String,
    val name: String,
    val playedGames: Int,
    val win: Int,
    val draw: Int,
    val lose: Int,
    val email: String
)
