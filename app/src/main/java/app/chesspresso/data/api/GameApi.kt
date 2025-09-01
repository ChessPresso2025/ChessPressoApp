package app.chesspresso.data.api

import app.chesspresso.data.models.EventRequest
import app.chesspresso.data.models.StatsReportRequest
import app.chesspresso.data.models.StatsResponse
import app.chesspresso.model.game.GameHistoryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface GameApi {
    @POST("events")
    suspend fun sendEvent(@Body request: EventRequest): Response<Unit>

    @POST("stats/report")
    suspend fun reportStats(@Body request: StatsReportRequest): Response<Unit>

    @GET("stats/me")
    suspend fun getMyStats(): Response<StatsResponse>

    @GET("stats/history")
    suspend fun getGameHistory(): Response<List<GameHistoryDto>>
}