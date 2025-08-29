package app.chesspresso.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH

// Datenklasse für die Anfrage
 data class ChangeUsernameRequest(val newUsername: String)

// Datenklasse für die Passwortänderung
 data class ChangePasswordRequest(
     val oldPassword: String,
     val newPassword: String
 )

interface UserApi {
    @PATCH("/user/username")
    suspend fun changeUsername(@Body request: ChangeUsernameRequest): Response<Unit>

    @PATCH("/user/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>
}
