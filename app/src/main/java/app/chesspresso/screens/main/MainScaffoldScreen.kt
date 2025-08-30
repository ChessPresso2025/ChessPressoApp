package app.chesspresso.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.chesspresso.R
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.model.game.GameMoveResponse
import app.chesspresso.screens.game.ChessGameScreen
import app.chesspresso.screens.game.GameOverScreen
import app.chesspresso.screens.lobby.LobbyWaitingScreen
import app.chesspresso.screens.lobby.PrivateLobbyScreen
import app.chesspresso.screens.lobby.QuickMatchScreen
import app.chesspresso.viewmodel.ChessGameViewModel
import app.chesspresso.websocket.WebSocketViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldScreen(
    authViewModel: AuthViewModel,
    webSocketViewModel: WebSocketViewModel,
    outerNavController: NavHostController
) {
    val innerNavController = rememberNavController()
    val currentRoute by innerNavController.currentBackStackEntryAsState()
    val selectedRoute = currentRoute?.destination?.route ?: "home"

    // Drawer-Logik hinzufügen
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Prüfe, ob wir uns in einem Lobby-Screen befinden
    val isLobbyScreen =
        selectedRoute == NavRoutes.QUICK_MATCH || selectedRoute == NavRoutes.PRIVATE_LOBBY
    // Prüfe, ob wir im Spiel-Screen sind
    val isGameScreen = selectedRoute.startsWith("game/")

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f), // Optional: Abdunklung beim Öffnen
        drawerContent = {
            if (isGameScreen) {
                val chessGameViewModel: ChessGameViewModel = hiltViewModel()
                val currentGameState by chessGameViewModel.currentGameState.collectAsState()
                val myColor by chessGameViewModel.myColor.collectAsState()
                val initialGameData by chessGameViewModel.initialGameData.collectAsState()
                val lobbyId = initialGameData?.lobbyId
                ModalDrawerSheet(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        GameDrawerContent(
                            currentGameState = currentGameState,
                            onResign = {
                                if (myColor != null && lobbyId != null) {
                                    chessGameViewModel.resignGame(myColor!!, lobbyId)
                                }
                            },
                            onOfferDraw = { /* TODO: Remis-Logik */ }
                        )
                    }
                }
            } else {
                ModalDrawerSheet(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Menü", modifier = Modifier)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        when {
                            isGameScreen -> {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menü"
                                    )
                                }
                            }
                            isLobbyScreen -> {
                                IconButton(onClick = { innerNavController.navigateUp() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Zurück"
                                    )
                                }
                            }
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.watermark_chess),
                                contentDescription = "App-Logo",
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text("ChessPresso")
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationItem.entries.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = item.route == selectedRoute,
                            onClick = {
                                if (selectedRoute != item.route) {
                                    innerNavController.navigate(item.route) {
                                        popUpTo("home") { inclusive = false }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = innerNavController,
                startDestination = NavRoutes.HOME,
                modifier = Modifier.padding(padding)
            ) {
                composable(NavRoutes.HOME) {
                    HomeScreen(
                        onPrivateGameClick = { innerNavController.navigate(NavRoutes.PRIVATE_LOBBY) },
                        onPublicGameClick = { innerNavController.navigate(NavRoutes.QUICK_MATCH) }
                    )
                }

                // Neue Lobby-Screens
                composable(NavRoutes.QUICK_MATCH) {
                    QuickMatchScreen(
                        onGameStart = { lobbyId ->
                            // TODO: Navigation zum Spiel-Screen
                            innerNavController.navigate("game/$lobbyId")
                        }
                    )
                }

                composable(NavRoutes.PRIVATE_LOBBY) {
                    PrivateLobbyScreen(
                        onLobbyCreated = { lobbyCode ->
                            innerNavController.navigate("lobby_waiting/$lobbyCode/true")
                        },
                        onLobbyJoined = { lobbyCode ->
                            innerNavController.navigate("lobby_waiting/$lobbyCode/false")
                        }
                    )
                }

                composable("lobby_waiting/{lobbyCode}/{isCreator}") { backStackEntry ->
                    val lobbyCode = backStackEntry.arguments?.getString("lobbyCode") ?: ""
                    val isCreator = backStackEntry.arguments?.getString("isCreator")
                        ?.toBoolean() ?: false
                    LobbyWaitingScreen(
                        isCreator = isCreator,
                        lobbyCode = lobbyCode,
                        onBackClick = {
                            // Explizit zum Home-Screen navigieren und alles andere löschen
                            innerNavController.navigate(NavRoutes.HOME) {
                                popUpTo(NavRoutes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onGameStart = { lobbyId ->
                            // TODO: Navigation zum Spiel-Screen
                            innerNavController.navigate("game/$lobbyId")
                        }
                    )
                }

                // Bestehende Screens
                composable(NavRoutes.STATS) {
                    StatsScreen()
                }
                composable(NavRoutes.PROFILE) {
                    ProfileScreen(
                        authViewModel = authViewModel,
                        outerNavController = outerNavController)
                }
                composable(NavRoutes.SETTINGS) {
                    SettingsScreen()
                }
                composable(NavRoutes.INFO) {
                    InfoScreen(
                        authViewModel = authViewModel,
                        onLogout = {
                            authViewModel.logout()
                            outerNavController.navigate("welcome") {
                                popUpTo(0) // Löscht den Backstack
                            }
                        }
                    )
                }

                // Spiel-Screen
                composable("game/{lobbyId}") { backStackEntry ->
                    val lobbyId = backStackEntry.arguments?.getString("lobbyId") ?: ""
                    val chessGameViewModel: ChessGameViewModel = hiltViewModel()
                    val gameStartResponse by chessGameViewModel.initialGameData.collectAsState()
                    val playerId = chessGameViewModel.webSocketService.playerId ?: ""
                    if (gameStartResponse != null) {
                        ChessGameScreen(
                            gameStartResponse = gameStartResponse!!,
                            viewModel = chessGameViewModel,
                            playerId = playerId,
                            onGameEnd = { gameEndResponse, playerId ->
                                scope.launch {
                                    drawerState.close()
                                    val gameEndJson = com.google.gson.Gson().toJson(gameEndResponse)
                                    innerNavController.navigate("gameOverScreen/${gameEndJson}/$playerId") {
                                        popUpTo("chessGameScreen") { inclusive = true }
                                    }
                                }
                            }
                        )
                    } else {
                        // Ladeanzeige oder Platzhalter, bis die Spieldaten geladen sind
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

                // GameOverScreen mit Übergabe des GameEndResponse als JSON-String und playerId
                composable("gameOverScreen/{gameEndJson}/{playerId}") { backStackEntry ->
                    val gameEndJson = backStackEntry.arguments?.getString("gameEndJson") ?: ""
                    val playerId = backStackEntry.arguments?.getString("playerId") ?: ""
                    val gameEndResponse = try {
                        com.google.gson.Gson().fromJson(gameEndJson, app.chesspresso.model.lobby.GameEndResponse::class.java)
                    } catch (e: Exception) { null }
                    if (gameEndResponse != null) {
                        GameOverScreen(gameEndResponse, playerId)
                    } else {
                        // Fehleranzeige, falls Deserialisierung fehlschlägt
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Fehler beim Laden des Spielergebnisses.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameDrawerContent(
    currentGameState: GameMoveResponse?,
    onResign: () -> Unit = {},
    onOfferDraw: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Spielverlauf",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display game state information
        currentGameState?.let { gameState ->
            if (gameState.isCheck != "") {
                Text(
                    "Schach!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Button(
            onClick = onResign,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Aufgeben")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onOfferDraw,
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

enum class NavigationItem(val label: String, val icon: ImageVector, val route: String) {
    Profile("Profil", Icons.Default.Person, NavRoutes.PROFILE),
    Stats("Statistik", Icons.Default.Search, NavRoutes.STATS), //durch Statistik Icon ersetzen
    Gameplay("Spielen", Icons.Default.Home, NavRoutes.HOME), //durch Schach-Icon ersetzen
    Settings("Optionen", Icons.Default.Settings, NavRoutes.SETTINGS),
    Info("Status", Icons.Default.Info, NavRoutes.INFO)
}


object NavRoutes {
    const val HOME = "home"
    const val PROFILE = "profile"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val INFO = "info"
    const val QUICK_MATCH = "quick_match"
    const val PRIVATE_LOBBY = "private_lobby"
}