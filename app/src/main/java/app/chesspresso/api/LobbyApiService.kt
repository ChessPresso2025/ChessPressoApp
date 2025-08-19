package app.chesspresso.api

import app.chesspresso.model.lobby.*
import retrofit2.Response
import retrofit2.http.*

interface LobbyApiService {

    @POST("lobby/quick-join")
    suspend fun joinQuickMatch(@Body request: QuickJoinRequest): Response<QuickJoinResponse>

    @POST("lobby/private/create")
    suspend fun createPrivateLobby(): Response<CreatePrivateLobbyResponse>

    @POST("lobby/private/join")
    suspend fun joinPrivateLobby(@Body request: JoinPrivateLobbyRequest): Response<JoinPrivateLobbyResponse>

    @POST("lobby/leave")
    suspend fun leaveLobby(@Body request: LeaveLobbyRequest): Response<Map<String, Any>>

    @GET("lobby/{lobbyId}")
    suspend fun getLobbyInfo(@Path("lobbyId") lobbyId: String): Response<LobbyInfoResponse>
}
