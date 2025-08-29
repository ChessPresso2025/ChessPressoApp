package app.chesspresso.data.api

import retrofit2.Response
import retrofit2.http.GET

interface StatsApi {
    @GET("/stats/me")
    suspend fun getMyStats(): Response<StatsResponse>
}
