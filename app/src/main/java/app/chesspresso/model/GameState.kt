package app.chesspresso.model

import app.chesspresso.model.board.*

class GameState {
    private lateinit var team : TeamColor
    private lateinit var move: Move
    private val board : Board = Board()
    //evtl String mit Board-Daten für Backend-Kommunikation zur Verfügung stellen
}