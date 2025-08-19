package app.chesspresso.model.lobby

// WebSocket Message Models
data class LobbyMessage(
    val content: String,
    val sender: String? = null,
    val timestamp: String? = null,
    val messageType: String = "CHAT" // CHAT, SYSTEM, etc.
)

data class GameMoveMessage(
    val from: String,
    val to: String,
    val piece: String,
    val playerId: String? = null,
    val timestamp: String? = null,
    val moveNotation: String? = null
)

data class PlayerReadyMessage(
    val lobbyId: String,
    val ready: Boolean
)

data class ConfigureLobbyMessage(
    val lobbyCode: String,
    val gameDuration: GameDuration,
    val whitePlayer: String? = null,
    val blackPlayer: String? = null,
    val randomColors: Boolean = false
)

// WebSocket Response Messages
data class LobbyWaitingMessage(
    val lobbyId: String,
    val message: String
)

data class LobbyCreatedMessage(
    val lobbyCode: String,
    val message: String
)

data class LobbyErrorMessage(
    val error: String
)

data class LobbyUpdateMessage(
    val lobbyId: String,
    val players: List<String>,
    val status: String,
    val message: String
)

data class GameStartMessage(
    val lobbyId: String,
    val gameTime: String,
    val whitePlayer: String,
    val blackPlayer: String,
    val lobbyChannel: String
)
