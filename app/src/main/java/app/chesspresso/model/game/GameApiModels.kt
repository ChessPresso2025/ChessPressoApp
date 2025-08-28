package app.chesspresso.model.game

import app.chesspresso.model.PieceType
import app.chesspresso.model.TeamColor

//Messages
data class GameStartMessage(
    val lobbyId: String,
    val gameTime: String,
    val whitePlayer: String?,
    val blackPlayer: String?,
    val randomPlayers: Boolean
)

data class GameMoveMessage(
    val from: String,
    val to: String,
    val teamColor: TeamColor,
    val timestamp: String? = null,
    val moveNotation: String? = null
)

data class PositionRequestMessage(
    val lobbyId: String,
    val position: String
)

data class PawnPromotionMessage(
    val position: String,
    val newPiece: PieceType
)



//Responses

//vorschlag
data class GameMoveResponse(
    val success: Boolean,
    val nextPlayer: TeamColor,
    val board: Map<String, PieceInfo>, // e.g., {"e2": "wp", "e4": "bp", ...}
    val isCheck: Boolean = false,
    val isCheckmate: Boolean = false,
    val isStalemate: Boolean = false, //evtl
    val isDraw: Boolean = false,
    val error: String? = null
)

data class PieceInfo(
    val type: PieceType,
    val color: TeamColor
)

