package app.chesspresso.model.lobby

import kotlinx.serialization.Serializable
import app.chesspresso.model.EndType
import app.chesspresso.model.TeamColor

// Request Models
data class QuickJoinRequest(
    val gameTime: GameTime
)

data class JoinPrivateLobbyRequest(
    val lobbyCode: String
)

data class LeaveLobbyRequest(
    val lobbyId: String
)

@Serializable
data class RemisMessage(
    val lobbyId: String,
    val requester: TeamColor,
    val responder: TeamColor? = null,
    val accept: Boolean
)

@Serializable
data class GameEndMessage(
    val lobbyId: String,
    val player: String,
    val endType: EndType
)

// Response Models
data class QuickJoinResponse(
    val success: Boolean,
    val lobbyId: String? = null,
    val message: String? = null,
    val gameTime: String? = null,
    val error: String? = null
)

data class CreatePrivateLobbyResponse(
    val success: Boolean,
    val lobbyCode: String? = null,
    val message: String? = null,
    val error: String? = null
)

data class JoinPrivateLobbyResponse(
    val success: Boolean,
    val lobbyCode: String? = null,
    val message: String? = null,
    val error: String? = null
)

data class LobbyInfoResponse(
    val lobbyId: String,
    val lobbyType: String,
    val players: List<String>,
    val status: String,
    val gameTime: String?,
    val isGameStarted: Boolean,
    val creator: String
)
