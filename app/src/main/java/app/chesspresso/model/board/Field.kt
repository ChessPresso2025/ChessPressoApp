package app.chesspresso.model.board

import app.chesspresso.model.board.Piece

class Field(val name : String) {
    var piece : Piece? = null
    //evtl noch Farbe auf Spielbrett hinzufügen
}