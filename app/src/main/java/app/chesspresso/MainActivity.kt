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
import app.chesspresso.auth.presemtation.LoginScreen
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
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(navController, authViewModel)
            }

            //Hauptteil mit Scaffold-View
            composable("main") {
                MainScaffoldScreen(authViewModel) //verwaltet eigene interne BottomNav
            }
        }

        // Automatische Navigation bei erfolgreicher Anmeldung
        LaunchedEffect(authState) {
            when (authState) {
                is AuthState.Success -> {
                    if (navController.currentDestination?.route != "main_app") {
                        navController.navigate("main_app") {
                            popUpTo("main") { inclusive = true }
                            popUpTo("login") { inclusive = true }
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
