package app.chesspresso.screens.game

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.chesspresso.model.PieceType
import app.chesspresso.model.TeamColor
import app.chesspresso.model.board.Board
import app.chesspresso.model.game.PieceInfo
import app.chesspresso.model.lobby.GameStartResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen(gameStartResponse: GameStartResponse) {
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val board = remember { Board() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Spielverlauf",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Aufgeben Logik */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aufgeben")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { /* Remis Logik */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remis anbieten")
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = DividerDefaults.color
                    )
                    // Hier später die Liste der Züge
                    Text("Hier werden die Züge angezeigt...")
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Schachpartie") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spieler und Uhren
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Spieler 1
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PlayerClock(
                            playerName = gameStartResponse.whitePlayer,
                            remainingTime = "10:00",
                            isActive = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Geschlagene Figuren von Spieler 1
                        CapturedPieces()
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Spieler 2
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        PlayerClock(
                            playerName = gameStartResponse.blackPlayer,
                            remainingTime = "10:00",
                            isActive = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Geschlagene Figuren von Spieler 2
                        CapturedPieces()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Schachbrett
                board.BoardContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    boardState = gameStartResponse.board,
                    lobbyId = gameStartResponse.lobbyId,
                    onPositionRequest = { positionRequest ->
                        // Hier wird die PositionRequestMessage behandelt
                        // Später kann diese an ein ViewModel oder Service weitergegeben werden
                        Log.d("ChessGameScreen", "Position angeklickt: ${positionRequest.position}")
                        // TODO: An Server senden über ViewModel/Service
                    }
                )
            }
        }
    }
}

@Composable
fun PlayerClock(
    playerName: String,
    remainingTime: String,
    isActive: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playerName,
                style = MaterialTheme.typography.titleMedium,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = remainingTime,
                style = MaterialTheme.typography.headlineLarge,
                color = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun CapturedPieces() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Geschlagene Figuren",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChessGameScreenPreview() {
    // Beispiel GameStartResponse mit initialem Schachbrett-Setup
    val sampleGameStartResponse = GameStartResponse(
        success = true,
        lobbyId = "LOBBY1",
        gameTime = "10:00",
        whitePlayer = "Max Mustermann",
        blackPlayer = "Anna Schmidt",
        lobbyChannel = "game-channel-12345",
        board = mapOf(
            // Weiße Figuren (untere Reihen)
            "a1" to PieceInfo(PieceType.ROOK, TeamColor.WHITE),
            "b1" to PieceInfo(PieceType.KNIGHT, TeamColor.WHITE),
            "c1" to PieceInfo(PieceType.BISHOP, TeamColor.WHITE),
            "d1" to PieceInfo(PieceType.QUEEN, TeamColor.WHITE),
            "e1" to PieceInfo(PieceType.KING, TeamColor.WHITE),
            "f1" to PieceInfo(PieceType.BISHOP, TeamColor.WHITE),
            "g1" to PieceInfo(PieceType.KNIGHT, TeamColor.WHITE),
            "h1" to PieceInfo(PieceType.ROOK, TeamColor.WHITE),
            "a2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),
            "b2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),
            "c2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),
            "d2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),
            "e2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),
            "f2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),
            "g2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),
            "h2" to PieceInfo(PieceType.PAWN, TeamColor.WHITE),

            // Leere Felder (Reihe 3)
            "a3" to null,
            "b3" to null,
            "c3" to null,
            "d3" to null,
            "e3" to null,
            "f3" to null,
            "g3" to null,
            "h3" to null,

            // Leere Felder (Reihe 4)
            "a4" to null,
            "b4" to null,
            "c4" to null,
            "d4" to null,
            "e4" to null,
            "f4" to null,
            "g4" to null,
            "h4" to null,

            // Leere Felder (Reihe 5)
            "a5" to null,
            "b5" to null,
            "c5" to null,
            "d5" to null,
            "e5" to null,
            "f5" to null,
            "g5" to null,
            "h5" to null,

            // Leere Felder (Reihe 6)
            "a6" to null,
            "b6" to null,
            "c6" to null,
            "d6" to null,
            "e6" to null,
            "f6" to null,
            "g6" to null,
            "h6" to null,

            // Schwarze Figuren (obere Reihen)
            "a7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "b7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "c7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "d7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "e7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "f7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "g7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "h7" to PieceInfo(PieceType.PAWN, TeamColor.BLACK),
            "a8" to PieceInfo(PieceType.ROOK, TeamColor.BLACK),
            "b8" to PieceInfo(PieceType.KNIGHT, TeamColor.BLACK),
            "c8" to PieceInfo(PieceType.BISHOP, TeamColor.BLACK),
            "d8" to PieceInfo(PieceType.QUEEN, TeamColor.BLACK),
            "e8" to PieceInfo(PieceType.KING, TeamColor.BLACK),
            "f8" to PieceInfo(PieceType.BISHOP, TeamColor.BLACK),
            "g8" to PieceInfo(PieceType.KNIGHT, TeamColor.BLACK),
            "h8" to PieceInfo(PieceType.ROOK, TeamColor.BLACK)
        ) as Map<String, PieceInfo>,
        error = null
    )

    ChessGameScreen(gameStartResponse = sampleGameStartResponse)
}
