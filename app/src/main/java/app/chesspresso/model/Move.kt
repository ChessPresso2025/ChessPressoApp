package app.chesspresso.model

data class Move(
    val start: String,
    val destination: String,
    val piece: PieceType,
    val specialMove: SpecialMove
) {

}