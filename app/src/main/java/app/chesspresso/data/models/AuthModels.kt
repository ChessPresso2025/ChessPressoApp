package app.chesspresso.data.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val login: String,
    val password: String
)

data class TokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("tokenType")
    val tokenType: String? = null,
    @SerializedName("expiresIn")
    val expiresIn: Long? = null,
    @SerializedName("username")
    val username: String? = null
)

data class EventRequest(
    val type: String,
    val payload: Map<String, Any>
)

data class StatsReportRequest(
    val result: String // "WIN" | "LOSS" | "DRAW"
)

data class StatsResponse(
    val wins: Int,
    val losses: Int,
    val draws: Int
)
