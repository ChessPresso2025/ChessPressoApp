package app.chesspresso.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH

// Datenklasse für die Anfrage
 data class ChangeUsernameRequest(val newUsername: String)

interface UserApi {
    @PATCH("/user/username")
    suspend fun changeUsername(@Body request: ChangeUsernameRequest): Response<Unit>
}

