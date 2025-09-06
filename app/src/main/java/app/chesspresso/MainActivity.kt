package app.chesspresso

import android.os.Bundle
import android.util.Log
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
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.screens.LoginScreen
import app.chesspresso.screens.WelcomeScreen
import app.chesspresso.screens.main.MainScaffoldScreen
import app.chesspresso.screens.SplashScreen
import app.chesspresso.ui.theme.ChessPressoAppTheme
import app.chesspresso.websocket.StompWebSocketService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var webSocketService: StompWebSocketService

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Log.d(TAG, "MainActivity created")

        setContent {
            ChessPressoAppTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "MainActivity is being destroyed")

        // Sende App-Closing-Nachricht wenn MainActivity zerstört wird
        try {
            webSocketService.sendAppClosingMessageWithReason("activity-destroyed")
            Log.d(TAG, "App closing message sent from MainActivity.onDestroy()")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send app closing message from MainActivity: ${e.message}")
        }

        super.onDestroy()
    }

    override fun finish() {
        Log.d(TAG, "MainActivity.finish() called")

        // Sende App-Closing-Nachricht wenn App explizit beendet wird
        try {
            webSocketService.sendAppClosingMessageWithReason("app-finished")
            Log.d(TAG, "App closing message sent from MainActivity.finish()")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send app closing message from finish(): ${e.message}")
        }

        super.finish()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity paused")
        webSocketService.sendAppClosingMessageWithReason("activity-paused")
    }

    @Composable
    fun AppNavigation(navController: NavHostController) {
        val authViewModel: AuthViewModel = hiltViewModel()
        val authState by authViewModel.authState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            composable("splash") {
                SplashScreen(
                    authViewModel = authViewModel,
                    onNavigateToMain = { navController.navigate("main") { popUpTo("splash") { inclusive = true } } },
                    onNavigateToWelcome = { navController.navigate("welcome") { popUpTo("splash") { inclusive = true } } }
                )
            }
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
                MainScaffoldScreen(
                    authViewModel = authViewModel,
                    outerNavController = navController
                ) //verwaltet eigene interne BottomNav
            }
        }

        // Automatische Navigation bei erfolgreicher Anmeldung
        LaunchedEffect(authState) {
            // Debug-Logging
            val stateDesc = when (authState) {
                is AuthState.Success -> "Success: ${(authState as AuthState.Success).response.name}"
                is AuthState.Error -> "Error: ${(authState as AuthState.Error).message}"
                is AuthState.Loading -> "Loading"
                AuthState.Idle -> "Idle"
            }
            Log.d("MainActivity", "AuthState changed: $stateDesc")

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
