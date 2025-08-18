package app.chesspresso.auth.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
    
    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
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
