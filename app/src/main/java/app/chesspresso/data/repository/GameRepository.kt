package app.chesspresso.data.repository

import app.chesspresso.data.api.GameApi
import app.chesspresso.data.models.StatsResponse
import app.chesspresso.model.game.GameHistoryDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val gameApi: GameApi
) {

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

    suspend fun getGameHistory(): Result<List<GameHistoryDto>> {
        return try {
            val response = gameApi.getGameHistory()
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
