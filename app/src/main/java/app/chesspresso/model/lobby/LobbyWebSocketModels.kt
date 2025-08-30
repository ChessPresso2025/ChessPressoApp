package app.chesspresso.model.lobby

import app.chesspresso.model.game.PieceInfo

// WebSocket Message Models
data class LobbyMessage(
    val content: String,
    val sender: String? = null,
    val timestamp: String? = null,
    val messageType: String = "CHAT" // CHAT, SYSTEM, etc.
)



data class PlayerReadyMessage(
    val lobbyId: String,
    val ready: Boolean
)

data class ConfigureLobbyMessage(
    val lobbyCode: String,
    val gameTime: GameTime,
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

data class RemisAcceptRequest(
    val lobbyId: String,
    val player: String //der den Remis vorgeschlagen bekommt
)

data class GameEndResponse(
    val winner: String,
    val loser: String,
    val draw: Boolean,
    val lobbyId: String,
    val type: String = "game-end"
)

data class GameStartResponse(
    val success: Boolean,
    val lobbyId: String,
    val gameTime: GameTime,
    val whitePlayer: String,
    val blackPlayer: String,
    val lobbyChannel: String,
    val board: Map<String, PieceInfo>,
    val error: String? = null
)
