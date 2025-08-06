package app.chesspresso.model.board

import app.chesspresso.model.PieceType
import app.chesspresso.model.TeamColor

class Board {
    val board : List<Field> = boardInit().toMutableList()

    fun boardInit() : List<Field> {
        val list = mutableListOf<Field>()
        val cols = 'A'..'H'
        val rows = 1..8

        for(row in rows.reversed()){
            for(col in cols){
                list.add(Field("$col$row"))
            }
        }
        return list
    }

    init {
        setStartPosition()
    }

    private fun setStartPosition(){
        board.forEach { it.piece = null }
        val startMap: Map<String, Piece> = buildMap {

            //white back row
            put("A1", Piece(PieceType.ROOK, TeamColor.WHITE))
            put("B1", Piece(PieceType.KNIGHT, TeamColor.WHITE))
            put("C1", Piece(PieceType.BISHOP, TeamColor.WHITE))
            put("D1", Piece(PieceType.QUEEN, TeamColor.WHITE))
            put("E1", Piece(PieceType.KING, TeamColor.WHITE))
            put("F1", Piece(PieceType.BISHOP, TeamColor.WHITE))
            put("G1", Piece(PieceType.KNIGHT, TeamColor.WHITE))
            put("H1", Piece(PieceType.ROOK, TeamColor.WHITE))

            // white pawns (2))
            for (c in 'A'..'H') {
                put("${c}2", Piece(PieceType.PAWN, TeamColor.WHITE))
            }

            // black pawns (7)
            for (c in 'A'..'H') {
                put("${c}7", Piece(PieceType.PAWN, TeamColor.BLACK))
            }

            // black back row
            put("A8", Piece(PieceType.ROOK, TeamColor.BLACK))
            put("B8", Piece(PieceType.KNIGHT, TeamColor.BLACK))
            put("C8", Piece(PieceType.BISHOP, TeamColor.BLACK))
            put("D8", Piece(PieceType.QUEEN, TeamColor.BLACK))
            put("E8", Piece(PieceType.KING, TeamColor.BLACK))
            put("F8", Piece(PieceType.BISHOP, TeamColor.BLACK))
            put("G8", Piece(PieceType.KNIGHT, TeamColor.BLACK))
            put("H8", Piece(PieceType.ROOK, TeamColor.BLACK))
        }

        for ((square, piece) in startMap) {
            getField(square)?.piece = piece
        }

    }

    fun getField(name: String): Field? {
        return board.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

}