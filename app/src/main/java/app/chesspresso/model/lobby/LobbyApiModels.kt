package app.chesspresso.model.lobby

// Request Models
data class QuickJoinRequest(
    val gameDuration: GameDuration
)

data class JoinPrivateLobbyRequest(
    val lobbyCode: String
)

data class LeaveLobbyRequest(
    val lobbyId: String
)

data class ConfigureLobbyRequest(
    val lobbyCode: String,
    val gameDuration: GameDuration,
    val whitePlayer: String? = null,
    val blackPlayer: String? = null,
    val randomColors: Boolean = false
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
