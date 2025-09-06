package app.chesspresso.model.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import app.chesspresso.model.game.PieceInfo
import app.chesspresso.ui.theme.LocalAppDarkTheme

class Field(
    val name: String,
    var isLightSquare: Boolean = false,
    var isValidMove: Boolean = false
) {

    @Composable
    fun FieldContent(
        modifier: Modifier = Modifier,
        isCheck: Boolean,
        isCheckmate: Boolean,
        pieceInfo: PieceInfo?,
        isFieldSelected: Boolean = false,
        isValidMove: Boolean = false,
        onFieldClick: () -> Unit = {}
    ) {
        val isDark = LocalAppDarkTheme.current
        val lightSquareColor = app.chesspresso.ui.theme.CoffeeCremeMid
        val darkSquareColor = app.chesspresso.ui.theme.CoffeeBrownLight
        val backgroundColor = when {
            isCheckmate -> app.chesspresso.ui.theme.CoffeeRedMate.copy(alpha = 0.7f)
            isCheck -> app.chesspresso.ui.theme.CoffeeRedCheck.copy(alpha = 0.7f)
            isFieldSelected -> app.chesspresso.ui.theme.CoffeeOrange.copy(alpha = 0.35f)
            isValidMove -> app.chesspresso.ui.theme.CoffeeGreen.copy(alpha = 0.3f)
            isLightSquare -> lightSquareColor
            else -> darkSquareColor
        }

        Box(
            modifier = modifier
                .aspectRatio(1f)
                .background(backgroundColor)
                .clickable { onFieldClick() },
            contentAlignment = Alignment.Center
        ) {
            pieceInfo?.let { piece ->
                var resourceId: Int? = null
                 // Bestimme die Bildressource basierend auf dem PieceType und TeamColor
                if (piece.type != PieceType.NULL && piece.color != TeamColor.NULL) {
                     resourceId = when (piece.type) {
                        PieceType.PAWN -> if (piece.color == TeamColor.WHITE) R.drawable.pawn_white else R.drawable.pawn_black
                        PieceType.ROOK -> if (piece.color == TeamColor.WHITE) R.drawable.rook_white else R.drawable.rook_black
                        PieceType.KNIGHT -> if (piece.color == TeamColor.WHITE) R.drawable.knight_white else R.drawable.knight_black
                        PieceType.BISHOP -> if (piece.color == TeamColor.WHITE) R.drawable.bishop_white else R.drawable.bishop_black
                        PieceType.QUEEN -> if (piece.color == TeamColor.WHITE) R.drawable.queen_white else R.drawable.queen_black
                        PieceType.KING -> if (piece.color == TeamColor.WHITE) R.drawable.king_white else R.drawable.king_black
                        PieceType.NULL -> null
                    }
                    if(resourceId != null){
                        Image(
                            painter = painterResource(id = resourceId),
                            contentDescription = null, //"${piece.color.name.lowercase()} ${piece.type.name.lowercase()}",
                            modifier = Modifier
                                .fillMaxSize(0.8f)
                                .aspectRatio(1f)
                        )
                    }


                }
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