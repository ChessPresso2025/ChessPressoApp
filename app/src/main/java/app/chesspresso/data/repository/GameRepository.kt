package app.chesspresso.data.repository

import app.chesspresso.data.api.GameApi
import app.chesspresso.data.models.EventRequest
import app.chesspresso.data.models.StatsReportRequest
import app.chesspresso.data.models.StatsResponse
import app.chesspresso.model.game.GameHistoryDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameApi: GameApi
) {
    suspend fun sendEvent(type: String, payload: Map<String, Any>): Result<Unit> {
        return try {
            val request = EventRequest(type, payload)
            val response = gameApi.sendEvent(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Event sending failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportResult(result: String): Result<Unit> {
        return try {
            val request = StatsReportRequest(result)
            val response = gameApi.reportStats(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Stats reporting failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyStats(): Result<StatsResponse> {
        return try {
            val response = gameApi.getMyStats()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Stats retrieval failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameHistory(userId: String): Result<List<GameHistoryDto>> {
        return try {
            val response = gameApi.getGameHistory(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Game history retrieval failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
