package app.chesspresso.model.board

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                val field = Field("$col$row")
                // Setze die Farbe der Felder
                field.isLightSquare = (row + col.code) % 2 == 0

                // Platziere die Figuren
                when(row) {
                    8 -> { // Schwarze Hauptfiguren
                        field.piece = when(col) {
                            'A', 'H' -> Piece(PieceType.ROOK, TeamColor.BLACK)
                            'B', 'G' -> Piece(PieceType.KNIGHT, TeamColor.BLACK)
                            'C', 'F' -> Piece(PieceType.BISHOP, TeamColor.BLACK)
                            'D' -> Piece(PieceType.QUEEN, TeamColor.BLACK)
                            'E' -> Piece(PieceType.KING, TeamColor.BLACK)
                            else -> null
                        }
                    }
                    7 -> { // Schwarze Bauern
                        field.piece = Piece(PieceType.PAWN, TeamColor.BLACK)
                    }
                    2 -> { // Weiße Bauern
                        field.piece = Piece(PieceType.PAWN, TeamColor.WHITE)
                    }
                    1 -> { // Weiße Hauptfiguren
                        field.piece = when(col) {
                            'A', 'H' -> Piece(PieceType.ROOK, TeamColor.WHITE)
                            'B', 'G' -> Piece(PieceType.KNIGHT, TeamColor.WHITE)
                            'C', 'F' -> Piece(PieceType.BISHOP, TeamColor.WHITE)
                            'D' -> Piece(PieceType.QUEEN, TeamColor.WHITE)
                            'E' -> Piece(PieceType.KING, TeamColor.WHITE)
                            else -> null
                        }
                    }
                }
                list.add(field)
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

    @Composable
    fun BoardContent(
        modifier: Modifier = Modifier
    ) {
        Column(
            modifier = modifier.aspectRatio(1f)
        ) {
            // Das Brett wird in 8 Reihen aufgeteilt
            for (row in 0..7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Jede Reihe hat 8 Felder
                    for (col in 0..7) {
                        val index = row * 8 + col
                        val field = board[index]

                        // Bestimme die Farbe des Feldes (hell/dunkel)
                        val isLightSquare = (row + col) % 2 == 0
                        field.isLightSquare = isLightSquare

                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            field.FieldContent(
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}