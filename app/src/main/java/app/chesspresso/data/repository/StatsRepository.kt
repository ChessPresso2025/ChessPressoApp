package app.chesspresso.data.repository

import app.chesspresso.data.api.StatsApi
import app.chesspresso.data.api.StatsResponse
import javax.inject.Inject

class StatsRepository @Inject constructor(
    private val statsApi: StatsApi
) {
    suspend fun getMyStats(): StatsResponse {
        val response = statsApi.getMyStats()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return body
            } else {
                throw Exception("Leere Antwort vom Server.")
            }
        } else {
            throw Exception("Fehler beim Laden der Stats: ${response.code()} ${response.message()}")
        }
    }
}
