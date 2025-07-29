package app.chesspresso.auth.data

interface AuthApi {
    @Post("/auth/google")
    suspend fun login(@Body request: AuthRequest): AuthResponse
}
data class AuthRequest(
    val idToken: String)

data class AuthResponse(
    val PlayerID: String,
    val name: String)