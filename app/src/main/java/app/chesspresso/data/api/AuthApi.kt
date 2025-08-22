package app.chesspresso.data.api

import app.chesspresso.data.models.LoginRequest
import app.chesspresso.data.models.RegisterRequest
import app.chesspresso.data.models.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<TokenResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>
}
