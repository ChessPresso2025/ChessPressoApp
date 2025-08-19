package app.chesspresso.model.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.chesspresso.R
import app.chesspresso.model.PieceType
import app.chesspresso.model.TeamColor

class Field(
    val name: String,
    var isLightSquare: Boolean = false,
    var isCheck: Boolean = false,
    var isCheckmate: Boolean = false,
    var isValidMove: Boolean = false
) {
    var piece: Piece? = null

    @Composable
    fun FieldContent(
        modifier: Modifier = Modifier
    ) {
        val backgroundColor = when {
            isCheckmate || isCheck -> Color.Red.copy(alpha = if (isCheckmate) 0.7f else 0.4f)
            isLightSquare -> Color(0xFFEEEED2) // Helles Feld
            else -> Color(0xFF769656) // Dunkles Feld
        }

        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            piece?.let { piece ->
                val resourceId = when (piece.pieceType) {
                    PieceType.PAWN -> if (piece.color == TeamColor.WHITE) R.drawable.pawn_white else R.drawable.pawn_black
                    PieceType.ROOK -> if (piece.color == TeamColor.WHITE) R.drawable.rook_white else R.drawable.rook_black
                    PieceType.KNIGHT -> if (piece.color == TeamColor.WHITE) R.drawable.knight_white else R.drawable.knight_black
                    PieceType.BISHOP -> if (piece.color == TeamColor.WHITE) R.drawable.bishop_white else R.drawable.bishop_black
                    PieceType.QUEEN -> if (piece.color == TeamColor.WHITE) R.drawable.queen_white else R.drawable.queen_black
                    PieceType.KING -> if (piece.color == TeamColor.WHITE) R.drawable.king_white else R.drawable.king_black
                }

                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "${piece.color.name.lowercase()} ${piece.pieceType.name.lowercase()}",
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .aspectRatio(1f)
                )
            }

            // Marker für gültige Züge
            if (isValidMove) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }
    }
}