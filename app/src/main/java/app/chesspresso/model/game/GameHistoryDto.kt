package app.chesspresso.model.game

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class MoveDto(
    @Contextual val id: UUID,
    val moveNumber: Int,
    val moveNotation: String,
    val createdAt: String // OffsetDateTime als ISO-String
)

@Serializable
data class GameHistoryDto(
    @Contextual val id: UUID,
    val startedAt: String, // OffsetDateTime als ISO-String
    val endedAt: String?,
    val result: String?,
    val moves: List<MoveDto>
)
