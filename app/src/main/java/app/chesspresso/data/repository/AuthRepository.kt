package app.chesspresso.data.repository

import app.chesspresso.data.api.AuthApi
import app.chesspresso.data.models.LoginRequest
import app.chesspresso.data.models.RegisterRequest
import app.chesspresso.data.models.TokenResponse
import app.chesspresso.data.storage.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) {
    suspend fun register(username: String, email: String, password: String): Result<TokenResponse> {
        return try {
            val request = RegisterRequest(username, email, password)
            val response = authApi.register(request)

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenStorage.saveToken(tokenResponse.accessToken)
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(login: String, password: String): Result<TokenResponse> {
        return try {
            val request = LoginRequest(login, password)
            val response = authApi.login(request)

            if (response.isSuccessful && response.body() != null) {
                val tokenResponse = response.body()!!
                tokenStorage.saveToken(tokenResponse.accessToken)
                Result.success(tokenResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenStorage.clearToken()
    }

    fun getToken(): Flow<String?> {
        return tokenStorage.getToken()
    }

    suspend fun isLoggedIn(): Boolean {
        return try {
            tokenStorage.getToken().first() != null
        } catch (e: Exception) {
            false
        }
    }
}
