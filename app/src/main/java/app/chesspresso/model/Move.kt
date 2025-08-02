package app.chesspresso.model

data class Move(val start : Position, val destination : Position, val piece: PieceType, val specialMove: SpecialMove) {

}