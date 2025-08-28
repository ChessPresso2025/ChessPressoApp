package app.chesspresso.model.board

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.chesspresso.model.TeamColor
import app.chesspresso.model.game.PieceInfo
import app.chesspresso.model.game.PositionRequestMessage

class Board {
    val board: List<Field> = boardInit().toMutableList()

    fun boardInit(): List<Field> {
        val list = mutableListOf<Field>()
        val cols = 'A'..'H'
        val rows = 1..8

        for (row in rows.reversed()) {
            for (col in cols) {
                val field = Field("$col$row")
                // Setze die Farbe der Felder
                field.isLightSquare = (row + col.code) % 2 == 0
                list.add(field)
            }
        }
        return list
    }

    fun getField(name: String): Field? {
        return board.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    @Composable
    fun BoardContent(
        modifier: Modifier = Modifier,
        nextPlayer: TeamColor = TeamColor.WHITE,
        isCheck: String = "",
        isCheckmate: String = "",
        boardState: Map<String, PieceInfo?> = emptyMap(),
        lobbyId: String = "",
        onPositionRequest: (PositionRequestMessage) -> Unit = {},
        isFlipped: Boolean = false
    ) {
        var selectedField by remember { mutableStateOf<String?>(null) }
        var validMoves by remember { mutableStateOf<Set<String>>(emptySet()) }

        var currentIndex : String? = boardState.keys.firstOrNull()
        if(currentIndex.isNullOrEmpty()){
            Log.d("BoardContent", "boardState is empty")
        } else {
            Log.d("BoardContent", "boardState has entries, first key: $currentIndex")
        }

        // Funktion zum Zurücksetzen der Auswahl
        fun resetSelection() {
            selectedField = null
            validMoves = emptySet()
            // Reset validMove states only (isSelected is now handled by Compose state)
            board.forEach { field ->
                field.isValidMove = false
            }
        }

        // Funktion für Feldklicks
        fun handleFieldClick(fieldName: String) {
            val field = getField(fieldName) ?: return

            when {
                // Wenn bereits ein Feld ausgewählt ist und wir auf ein anderes klicken
                selectedField != null && selectedField != fieldName -> {
                    if (validMoves.contains(fieldName)) {
                        // Zug ausführen - später implementiert
                        Log.d("Board", "Zug von $selectedField nach $fieldName")
                        // Hier könnte später eine GameMoveMessage erstellt werden
                        resetSelection()
                    } else {
                        // Neue Auswahl oder Abwählen
                        resetSelection()
                        if (boardState[fieldName] != null) {
                            // Neues Feld auswählen
                            selectedField = fieldName

                            // PositionRequestMessage erstellen und senden
                            val positionRequest = PositionRequestMessage(
                                lobbyId = lobbyId,
                                position = fieldName
                            )
                            onPositionRequest(positionRequest)
                            Log.d("Board", "PositionRequest gesendet: $positionRequest")
                        }
                    }
                }
                // Wenn das gleiche Feld nochmal geklickt wird
                selectedField == fieldName -> {
                    resetSelection()
                }
                // Wenn noch nichts ausgewählt ist
                selectedField == null -> {
                    if (boardState[fieldName] != null) {
                        selectedField = fieldName

                        // PositionRequestMessage erstellen und senden
                        val positionRequest = PositionRequestMessage(
                            lobbyId = lobbyId,
                            position = fieldName
                        )
                        onPositionRequest(positionRequest)
                        Log.d("Board", "PositionRequest gesendet: $positionRequest")
                    }
                }
            }
        }

        Column(
            modifier = modifier.aspectRatio(1f)
        ) {
            val rowRange = if (isFlipped) 7 downTo 0 else 0..7
            val colRange = if (isFlipped) 7 downTo 0 else 0..7
            for (row in rowRange) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in colRange) {
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
                                modifier = Modifier.fillMaxSize(),
                                isCheck = isCheck == field.name,
                                isCheckmate = isCheckmate == field.name,
                                pieceInfo = boardState.getValue(field.name),
                                isFieldSelected = selectedField == field.name, // Verwende Compose State
                                onFieldClick = { handleFieldClick(field.name) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Funktion zum Setzen der gültigen Züge (wird später vom Server Response aufgerufen)
    fun setValidMoves(moves: Set<String>) {
        board.forEach { field ->
            field.isValidMove = moves.contains(field.name)
        }
    }
}