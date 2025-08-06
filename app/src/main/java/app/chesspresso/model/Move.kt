package app.chesspresso.model

import app.chesspresso.model.board.Board

data class Move(val start : Board, val destination : Board, val piece: PieceType, val specialMove: SpecialMove) {

}