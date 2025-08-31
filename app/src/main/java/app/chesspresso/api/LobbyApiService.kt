package app.chesspresso.api

import app.chesspresso.model.game.GameStartMessage
import app.chesspresso.model.lobby.CreatePrivateLobbyResponse
import app.chesspresso.model.lobby.GameStartResponse
import app.chesspresso.model.lobby.JoinPrivateLobbyRequest
import app.chesspresso.model.lobby.JoinPrivateLobbyResponse
import app.chesspresso.model.lobby.LeaveLobbyRequest
import app.chesspresso.model.lobby.LobbyInfoResponse
import app.chesspresso.model.lobby.QuickJoinRequest
import app.chesspresso.model.lobby.QuickJoinResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LobbyApiService {

    @POST("lobby/quick-join")
    suspend fun joinQuickMatch(@Body request: QuickJoinRequest): Response<QuickJoinResponse>

    @POST("lobby/private/create")
    suspend fun createPrivateLobby(): Response<CreatePrivateLobbyResponse>

    @POST("lobby/private/join")
    suspend fun joinPrivateLobby(@Body request: JoinPrivateLobbyRequest): Response<JoinPrivateLobbyResponse>

    @POST("lobby/leave")
    suspend fun leaveLobby(@Body request: LeaveLobbyRequest): Response<Map<String, Any>>

    @POST("lobby/{lobbyid}/start")
    suspend fun startGame(@Path("lobbyid") request: GameStartMessage): Response<GameStartResponse>

    @GET("lobby/{lobbyId}")
    suspend fun getLobbyInfo(@Path("lobbyId") lobbyId: String): Response<LobbyInfoResponse>
}
