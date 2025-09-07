package app.chesspresso.model.lobby

import app.chesspresso.model.game.PieceInfo

// WebSocket Message Models
data class LobbyMessage(
    val content: String,
    val sender: String? = null,
    val timestamp: String? = null,
    val messageType: String = "CHAT" // CHAT, SYSTEM, etc.
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

data class LobbyCloseMessage(
    val lobbyId: String,
    val playerId: String,
    val type: String = "lobby-close"
)
