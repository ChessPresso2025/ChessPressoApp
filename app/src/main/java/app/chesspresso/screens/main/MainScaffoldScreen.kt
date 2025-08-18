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
import androidx.compose.runtime.getValue
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
                            painter = painterResource(id = R.drawable.watermark_chess), // <- dein Logo aus drawable
                            contentDescription = "App-Logo",
                            modifier = Modifier
                                .size(32.dp) // Passe die Größe nach Wunsch an
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text("ChessPresso") // oder dein App-Name
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
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ){
            composable("home") {
                HomeScreen(
                    onPrivateGameClick = { },
                    onPublicGameClick = { }
                )
            }
            composable("stats") {
                StatsScreen()
            }
            composable("profile"){
                ProfileScreen()
            }
            composable("settings"){
                SettingsScreen()
            }
            composable("info") {
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
        }
    }
}

enum class NavigationItem(val label: String, val icon: ImageVector, val route: String) {
    Profile("Profil", Icons.Default.Person, NavRoutes.PROFILE),
    Stats("Statistik", Icons.Default.Search, NavRoutes.STATS), //durch Statistik Icon ersetzen
    Gameplay("Spielen", Icons.Default.Home, NavRoutes.HOME), //durch Schach-Icon ersetzen
    Settings("Einstellungen", Icons.Default.Settings, NavRoutes.SETTINGS),
    Info("Server-Status", Icons.Default.Info, NavRoutes.INFO)
}


object NavRoutes {
    const val PROFILE = "profile"
    const val STATS = "stats"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val INFO = "info"
}