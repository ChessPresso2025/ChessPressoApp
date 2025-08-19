package app.chesspresso.screens.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.chesspresso.model.board.Board
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChessGameScreen() {
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
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
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
                            playerName = "Spieler 1",
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
                            playerName = "Spieler 2",
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
                        .padding(horizontal = 8.dp)
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
    ChessGameScreen()
}
