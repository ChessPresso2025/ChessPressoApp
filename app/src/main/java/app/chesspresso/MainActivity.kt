package app.chesspresso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import app.chesspresso.ui.theme.ChessPressoAppTheme
import app.chesspresso.screens.LoginScreen
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.screens.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessPressoAppTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen(){
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "main_screen"
        ){
            composable("main_screen") {
                WelcomeScreen(
                    onLoginClick = { navController.navigate("login_screen") }
                )
            }
            composable("login_screen") {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(authViewModel)
            }
            composable("home_screen") {
                HomeScreen(
                    onPrivateGameClick = {},
                    onPublicGameClick = {},
                    onNavigate = {}
                )
            }
            //andere Seiten werden hier geaddet
        }
    }
}
