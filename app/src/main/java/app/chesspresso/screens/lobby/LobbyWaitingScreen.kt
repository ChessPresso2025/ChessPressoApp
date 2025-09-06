package app.chesspresso.screens.lobby

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.chesspresso.model.lobby.GameTime
import app.chesspresso.ui.components.LobbyCreatorControls
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.viewmodel.PrivateLobbyViewModel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.Color

@Composable
fun LobbyWaitingScreen(
    isCreator: Boolean,
    lobbyCode: String,
    onBackClick: () -> Unit,
    onGameStart: (String) -> Unit,
    chessGameViewModel: app.chesspresso.viewmodel.ChessGameViewModel,
    viewModel: PrivateLobbyViewModel = hiltViewModel()
) {
    val currentLobby by viewModel.currentLobby.collectAsStateWithLifecycle()
    val gameStarted by viewModel.gameStarted.collectAsStateWithLifecycle()
    val error by viewModel.lobbyError.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navigationEvent by viewModel.navigationEvent.collectAsStateWithLifecycle()

    var selectedGameTime by remember { mutableStateOf(GameTime.MIDDLE) }
    var selectedWhitePlayer by remember { mutableStateOf("") }
    var randomColors by remember { mutableStateOf(true) }

    // Farbauswahl-State auf Composable-Ebene
    var colorChoice by remember { mutableStateOf("random") } // Werte: "black", "white", "random"



    // Navigation nach Home wenn Lobby verlassen wurde
    LaunchedEffect(navigationEvent) {
        if (navigationEvent == "home") {
            onBackClick()
            viewModel.onNavigated()
        }
    }

    // Automatische Navigation bei Spielstart
    LaunchedEffect(gameStarted) {
        gameStarted?.let { gameStart ->
            onGameStart(gameStart.lobbyId)
            viewModel.clearGameStart()
        }
    }

    // Lobby-Info beim Laden des Screens abrufen
    LaunchedEffect(lobbyCode) {
        viewModel.refreshLobbyInfo(lobbyCode)
    }

    // Regelmäßige Aktualisierung der Lobby-Info alle 3 Sekunden
    LaunchedEffect(lobbyCode) {
        while (true) {
            kotlinx.coroutines.delay(3000) // 3 Sekunden warten
            viewModel.refreshLobbyInfo(lobbyCode)
        }
    }

    // Spieler-Namen aktualisieren wenn Lobby geladen wird
    LaunchedEffect(currentLobby) {
        currentLobby?.let { lobby ->
            if (lobby.players.isNotEmpty()) {
                selectedWhitePlayer = lobby.players.first()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CoffeeHeadlineText(
            text = "Deine Lobby: $lobbyCode",

        )

        // nur wenn nur ein Spieler in der Lobby ist
        if (currentLobby?.players?.size != 2) {
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoffeeText(
                        text = "Teile diesen Code mit deinem Freund",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // QR-Code für Lobby-Ersteller anzeigen (nur wenn noch Platz frei ist)
        if (isCreator && currentLobby?.players?.size != 2) {
            LobbyCreatorControls(
                lobbyId = lobbyCode
            )
        }

        // Spieler-Status
        CoffeeCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CoffeeText(
                    text = "Spieler (${currentLobby?.players?.size ?: 1}/2):"
                )

                currentLobby?.let { lobby ->
                    lobby.players.forEachIndexed { index, player ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            CoffeeText(
                                text = "${player}${if (player == lobby.creator) " (Ersteller)" else ""}",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (currentLobby?.players?.size != 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            CoffeeText(
                                text = "Warte auf zweiten Spieler...",
                                modifier = Modifier.padding(start = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        CoffeeHeadlineText(
            text = "Spiel-Einstellungen",
            fontSizeSp = 24
        )

        // Spieleinstellungen Card (nur für Ersteller)
        currentLobby?.let { lobby ->
            if (isCreator) {
                CoffeeCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        CoffeeText(
                            text = "Spielzeit:"
                        )
                        GameTimeSelectionRow(
                            selectedGameTime = selectedGameTime,
                            onGameTimeSelected = { selectedGameTime = it },
                            allowedTimes = GameTime.entries.toList()
                        )
                    }
                }

                // Farbauswahl Card (nur wenn beide Spieler da sind)
                if (lobby.players.size == 2) {
                    CoffeeCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CoffeeText(
                                text = "Farbauswahl:"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val iconSize: Dp = 56.dp
                                val cardSize: Dp = 80.dp
                                val borderWidth = 3.dp
                                val defaultBorderColor = MaterialTheme.colorScheme.primary
                                val selectedBorderColor = MaterialTheme.colorScheme.secondary
                                val cardShape = RoundedCornerShape(16.dp)

                                // Helper für Card
                                fun cardBorder(selected: Boolean) = BorderStroke(
                                    borderWidth,
                                    if (selected) selectedBorderColor else defaultBorderColor
                                )


                                // Weiß-Icon (Bauer)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .size(cardSize)
                                        .clickable {
                                            colorChoice = "white"
                                            randomColors = false},
                                    shape = cardShape,
                                    border = cardBorder(colorChoice == "white"),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(id = app.chesspresso.R.drawable.pawn_white),
                                            contentDescription = "Weiß",
                                            tint = null,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                }
                                // Würfel-Icon (Zufall)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .size(cardSize)
                                        .clickable {
                                            colorChoice = "random"
                                            randomColors = true },
                                    shape = cardShape,
                                    border = cardBorder(colorChoice == "random"),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Casino,
                                            contentDescription = "Zufällig",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                }
                                // Schwarz-Icon (Bauer)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                        .size(cardSize)
                                        .clickable {
                                            colorChoice = "black"
                                            randomColors = false },
                                    shape = cardShape,
                                    border = cardBorder(colorChoice == "black"),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            painter = painterResource(id = app.chesspresso.R.drawable.pawn_black),
                                            contentDescription = "Schwarz",
                                            tint = null,
                                            modifier = Modifier.size(iconSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Spiel starten Button
                    CoffeeButton(
                        onClick = {
                            val whitePlayerFinal = when (colorChoice) {
                                "random" -> null
                                "white" -> lobby.creator
                                "black" -> lobby.players.find { it != lobby.creator }
                                else -> null
                            }
                            val blackPlayerFinal =
                                if (randomColors) null else lobby.players.find { it != whitePlayerFinal }

                            viewModel.configureAndStartGame(
                                lobbyCode = lobbyCode,
                                gameTime = selectedGameTime,
                                whitePlayer = whitePlayerFinal,
                                blackPlayer = blackPlayerFinal,
                                randomColors = randomColors
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        content = {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Spiel starten")
                        }
                    )
                }
            } else if (lobby.players.size == 2) {
                // Warte-Status für den zweiten Spieler
                CoffeeCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        CoffeeText(
                            text = "Warte auf Spiel-Start..."
                        )
                        CoffeeText(
                            text = "Der Lobby-Ersteller richtet das Spiel ein.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Fehleranzeige
        error?.let { errorMessage ->
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                CoffeeText(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Lobby verlassen Button
        CoffeeButton(
            onClick = {
                viewModel.leaveLobby()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            content = {
                Text("Lobby verlassen")
            }
        )
    }
}
