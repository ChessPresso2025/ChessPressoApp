package app.chesspresso.auth.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/google")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}

data class AuthRequest(
    val idToken: String)

data class AuthResponse(
    val PlayerID: String,
    val name: String)