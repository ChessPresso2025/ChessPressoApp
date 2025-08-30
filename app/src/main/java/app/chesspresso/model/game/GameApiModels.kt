package app.chesspresso.model.game

import app.chesspresso.model.PieceType
import app.chesspresso.model.SpecialMove
import app.chesspresso.model.TeamColor
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

//Messages
@Serializable
data class GameStartMessage(
    val lobbyId: String,
    val gameTime: String,
    val whitePlayer: String?,
    val blackPlayer: String?,
    val randomPlayers: Boolean
)

@Serializable
data class GameMoveMessage(
    val from: String,
    val to: String,
    val teamColor: TeamColor,
    val lobbyId: String
)

@Serializable
data class PositionRequestMessage(
    val lobbyId: String,
    val position: String
)

data class PawnPromotionMessage(
    val position: String,
    val newPiece: PieceType
)



//Responses

@Serializable
data class GameMoveResponse(
    val lobbyId: String,
    val nextPlayer: TeamColor,
    val board: Map<String, PieceInfo>,
    val isCheck: String,
    val move: MoveInfo
)
@Serializable
data class MoveInfo(
    val start: String,
    val end: String,
    val piece: PieceType,
    val specialMove: SpecialMove?,
    val captured: CapturedInfo?
)
@Serializable
data class CapturedInfo(
    val type: PieceType?,
    val color: TeamColor?,
    val position: String?
)
@Serializable
data class PieceInfo(
    val type: PieceType,
    @SerializedName("colour")
    val color: TeamColor
)

data class PossibleMovesResponse(
    @SerializedName("possible_moves")
    val possibleMoves: List<String> // Liste der möglichen Zielfelder
)
