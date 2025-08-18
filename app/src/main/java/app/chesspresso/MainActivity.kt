package app.chesspresso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.chesspresso.screens.LoginScreen
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.screens.main.MainScaffoldScreen
import app.chesspresso.screens.WelcomeScreen
import app.chesspresso.ui.theme.ChessPressoAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessPressoAppTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }

    @Composable
    fun AppNavigation(navController: NavHostController){
        val authViewModel: AuthViewModel = hiltViewModel()
        val authState by authViewModel.authState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = "welcome"
        ){
            //Login-Teil der App
            composable("welcome") {
                WelcomeScreen(
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("login") {
                LoginScreen(navController, authViewModel)
            }

            //Hauptteil mit Scaffold-View
            composable("main") {
                MainScaffoldScreen(authViewModel) //verwaltet eigene interne BottomNav
            }
        }

        // Automatische Navigation bei erfolgreicher Anmeldung
        LaunchedEffect(authState) {
            // Debug-Logging
            val stateDesc = when(authState) {
                is AuthState.Success -> "Success: ${(authState as AuthState.Success).response.name}"
                is AuthState.Error -> "Error: ${(authState as AuthState.Error).message}"
                is AuthState.Loading -> "Loading"
                AuthState.Idle -> "Idle"
            }
            android.util.Log.d("MainActivity", "AuthState changed: $stateDesc")

            when (authState) {
                is AuthState.Success -> {
                    if (navController.currentDestination?.route != "main") {
                        navController.navigate("main") {
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                }
                else -> {
                    // Keine Aktion, wenn nicht angemeldet
                }
            }
        }
    }
}
