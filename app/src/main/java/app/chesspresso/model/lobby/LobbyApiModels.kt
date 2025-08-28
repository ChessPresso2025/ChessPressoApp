package app.chesspresso.model.lobby

// Request Models
data class QuickJoinRequest(
    val gameTime: GameTime
)

data class JoinPrivateLobbyRequest(
    val lobbyId: String
)

data class LeaveLobbyRequest(
    val lobbyId: String
)

data class RemisMessage(
    val lobbyId: String,
    val player: String,
    val accept: Boolean
)

data class ResignMessage(
    val lobbyId: String,
    val player: String
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
    val lobbyId: String? = null,
    val message: String? = null,
    val error: String? = null
)

data class JoinPrivateLobbyResponse(
    val success: Boolean,
    val lobbyId: String? = null,
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
