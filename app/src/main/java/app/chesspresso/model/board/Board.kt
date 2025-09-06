package app.chesspresso.model.board

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        myColor: TeamColor? = null,
        isCheck: String = "",
        isCheckmate: String = "",
        boardState: Map<String, PieceInfo?> = emptyMap(),
        lobbyId: String = "",
        onPositionRequest: (PositionRequestMessage) -> Unit = {},
        isFlipped: Boolean = false,
        possibleMoves: List<String> = emptyList(),
        onGameMove: (from: String, to: String) -> Unit = { _, _ -> },
        fieldHighlights: Map<String, app.chesspresso.viewmodel.ChessGameViewModel.FieldHighlight> = emptyMap() // NEU
    ) {
        var selectedField by remember { mutableStateOf<String?>(null) }
        var validMoves by remember { mutableStateOf(possibleMoves.toSet()) }

        // Funktion zum Zurücksetzen der Auswahl
        fun resetSelection() {
            // Reset validMove states only (isSelected is now handled by Compose state)
            board.forEach { field ->
                field.isValidMove = false
            }
        }

        // Funktion für Feldklicks
        fun handleFieldClick(fieldName: String) {
            //Nur erlauben, wenn der Spieler am Zug ist
            if (myColor == null || myColor != nextPlayer) {
                Log.d("BoardContent", "Nicht am Zug: myColor=$myColor, nextPlayer=$nextPlayer")
                return
            }

            val field = getField(fieldName) ?: return

            when {
                // Wenn bereits ein Feld ausgewählt ist und wir auf ein anderes klicken
                selectedField != null && selectedField != fieldName -> {
                    val piece = boardState[fieldName]
                    // Nur erlauben, wenn das Zielfeld leer ist oder eine gegnerische Figur enthält
                    if (validMoves.contains(fieldName) && (piece == null || piece.color != myColor)) {
                        // Zug ausführen
                        Log.d("Board", "Zug von $selectedField nach $fieldName")
                        onGameMove(selectedField!!, fieldName) // NEU: Callback aufrufen
                        resetSelection()
                        selectedField = null // Auswahl nach Zug zurücksetzen
                    } else {
                        // Neue Auswahl oder Abwählen
                        resetSelection()
                        val newPiece = boardState[fieldName]
                        if (newPiece != null && newPiece.color == myColor) {
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
                    selectedField = null
                    validMoves = emptySet() // possibleMoves zurücksetzen
                }
                // Wenn noch nichts ausgewählt ist
                selectedField == null -> {
                    val piece = boardState[fieldName]
                    if (piece != null && piece.color == myColor) {
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

        // Synchronisiere validMoves mit possibleMoves
        LaunchedEffect(possibleMoves) {
            validMoves = possibleMoves.toSet()
        }

        // Labels und Reihenfolge je nach Drehung
        val columnLabels = if (isFlipped) listOf("H", "G", "F", "E", "D", "C", "B", "A") else listOf("A", "B", "C", "D", "E", "F", "G", "H")
        val rowLabels = if (isFlipped) (1..8).toList() else (8 downTo 1).toList()
        val numberFontSize = 14.sp
        val labelFontSize = 20.sp
        val labelFontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        val labelColor = MaterialTheme.colorScheme.primary // Alternativ: Color.White, je nach Theme
        val rowRange = if (isFlipped) 7 downTo 0 else 0..7
        val colRange = if (isFlipped) 7 downTo 0 else 0..7

        Column(
            modifier = modifier.aspectRatio(1f)
        ) {
            // Obere Buchstaben-Beschriftung
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center) {
                Spacer(modifier = Modifier.weight(0.2f))
                for (label in columnLabels) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = labelFontSize,
                            fontWeight = labelFontWeight,
                            color = labelColor,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(0.2f))
            }
            // Das Brett mit Zahlen-Beschriftung links und rechts
            for ((rowIdx, row) in rowRange.withIndex()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Linke Zahlen-Beschriftung mit noch mehr Abstand zum Brett
                    Box(
                        modifier = Modifier.weight(0.35f).padding(end = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowLabels[rowIdx].toString(),
                            fontSize = labelFontSize,
                            fontWeight = labelFontWeight,
                            color = labelColor
                        )
                    }
                    // Schachbrettfelder
                    for (col in colRange) {
                        val index = row * 8 + col
                        val field = board[index]
                        val isLightSquare = (row + col) % 2 == 0
                        field.isLightSquare = isLightSquare
                        val highlight = fieldHighlights[field.name] ?: app.chesspresso.viewmodel.ChessGameViewModel.FieldHighlight.NONE
                        val isDark = isSystemInDarkTheme()
                        val lightSquareColor = if (isDark) app.chesspresso.ui.theme.CoffeeBrownLight else app.chesspresso.ui.theme.CoffeeCremeMid
                        val darkSquareColor = if (isDark) app.chesspresso.ui.theme.CoffeeBrownContrast else app.chesspresso.ui.theme.CoffeeBrownSoft
                        val backgroundColor = when (highlight) {
                            app.chesspresso.viewmodel.ChessGameViewModel.FieldHighlight.CHECKMATE_KING -> Color(colorResource(id = app.chesspresso.R.color.checkmate_king).value)
                            app.chesspresso.viewmodel.ChessGameViewModel.FieldHighlight.CHECKMATE_ATTACKER, 
                            app.chesspresso.viewmodel.ChessGameViewModel.FieldHighlight.CHECK_KING -> Color(colorResource(id = app.chesspresso.R.color.checkmate_attacker).value)
                            else -> if (isLightSquare) lightSquareColor else darkSquareColor
                        }
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            field.FieldContent(
                                modifier = Modifier.fillMaxSize().clickable { handleFieldClick(field.name) }.background(backgroundColor),
                                isCheck = isCheck == field.name,
                                isCheckmate = isCheckmate == field.name,
                                pieceInfo = boardState.getValue(field.name),
                                isFieldSelected = selectedField == field.name,
                                isValidMove = validMoves.contains(field.name),
                                onFieldClick = { handleFieldClick(field.name) }
                            )
                        }
                    }
                    Box(
                        modifier = Modifier.weight(0.35f).padding(start = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = rowLabels[rowIdx].toString(),
                            fontSize = labelFontSize,
                            fontWeight = labelFontWeight,
                            color = labelColor
                        )
                    }
                }
            }
            // Untere Buchstaben-Beschriftung
            Row(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.weight(0.2f))
                for (label in columnLabels) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = labelFontSize,
                            fontWeight = labelFontWeight,
                            color = labelColor,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(0.2f))
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