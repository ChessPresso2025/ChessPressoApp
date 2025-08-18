package app.chesspresso.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.chesspresso.R
import app.chesspresso.auth.presentation.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffoldScreen(authViewModel: AuthViewModel){
    val innerNavController = rememberNavController()
    val currentRoute by innerNavController.currentBackStackEntryAsState()
    val selectedRoute = currentRoute?.destination?.route ?: "home"

    Scaffold(
        topBar = {
            TopAppBar(
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
                            if(selectedRoute != item.route) {
                                innerNavController.navigate(item.route){
                                    popUpTo("home") {inclusive = false}
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
                    onPrivateGameClick = { innerNavController.navigate(NavRoutes.PRIVATE_GAME_CHOICE) },
                    onPublicGameClick = { innerNavController.navigate(NavRoutes.PUBLIC_GAME) }
                )
            }
            composable(NavRoutes.PRIVATE_GAME_CHOICE) {
                app.chesspresso.screens.dialogs.PrivateGameChoiceScreen(
                    onCreateClick = { innerNavController.navigate(NavRoutes.CREATE_PRIVATE_GAME) },
                    onJoinClick = { innerNavController.navigate(NavRoutes.JOIN_PRIVATE_GAME) },
                    onDismiss = { innerNavController.navigate(NavRoutes.HOME) }
                )
            }
            composable(NavRoutes.CREATE_PRIVATE_GAME) {
                app.chesspresso.screens.dialogs.CreatePrivateGameScreen(
                    onDismiss = { innerNavController.navigate(NavRoutes.HOME) },
                    onCreateGame = { duration, color ->
                        // TODO: Implementiere die Logik zum Erstellen des Spiels
                        innerNavController.navigate(NavRoutes.HOME)
                    }
                )
            }
            composable(NavRoutes.JOIN_PRIVATE_GAME) {
                app.chesspresso.screens.dialogs.JoinPrivateGameScreen(
                    onJoin = { /* TODO: Implementiere Beitreten-Logik */ },
                    onDismiss = { innerNavController.navigate(NavRoutes.HOME) }
                )
            }
            composable(NavRoutes.STATS) {
                StatsScreen()
            }
            composable(NavRoutes.PROFILE) {
                ProfileScreen()
            }
            composable(NavRoutes.SETTINGS) {
                SettingsScreen()
            }
            composable(NavRoutes.INFO) {
                InfoScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.logout()
                        // Hier wäre Navigation zum welcome Screen nötig, aber innerNavController
                        // kann nur innerhalb des MainScaffolds navigieren
                        // Stattdessen sollte die App eine Callback-Funktion für Logout verwenden
                    }
                )
            }
            composable(NavRoutes.PUBLIC_GAME) {
                app.chesspresso.screens.dialogs.CreatePublicGameScreen(
                    onDismiss = { innerNavController.navigate(NavRoutes.HOME) },
                    onCreateGame = { duration ->
                        // TODO: Implementiere die Logik zum Erstellen des öffentlichen Spiels
                        innerNavController.navigate(NavRoutes.HOME)
                    }
                )
            }
        }
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
    const val PRIVATE_GAME_CHOICE = "privateGameChoice"
    const val CREATE_PRIVATE_GAME = "createPrivateGame"
    const val JOIN_PRIVATE_GAME = "joinPrivateGame"
    const val PUBLIC_GAME = "publicGame"
}