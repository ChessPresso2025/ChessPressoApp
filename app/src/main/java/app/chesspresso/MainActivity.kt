package app.chesspresso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import app.chesspresso.ui.theme.ChessPressoAppTheme
import app.chesspresso.ui.theme.Creme1
import app.chesspresso.ui.theme.Creme2
import app.chesspresso.ui.theme.DarkBrown1
import app.chesspresso.ui.theme.MidBrown2
import app.chesspresso.auth.presemtation.LoginScreen
import app.chesspresso.auth.presemtation.AuthViewModel
import app.chesspresso.auth.presemtation.AuthState

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
        val authViewModel: AuthViewModel = hiltViewModel()
        val authState by authViewModel.authState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = "main_screen"
        ){
            composable("main_screen") {
                HomeScreen(
                    onLoginClick = { navController.navigate("login_screen") }
                )
            }

            composable("login_screen") {
                LoginScreen(authViewModel)
            }

            composable("main_app") {
                MainAppScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate("main_screen") {
                            popUpTo("main_app") { inclusive = true }
                        }
                    }
                )
            }
        }

        // Automatische Navigation bei erfolgreicher Anmeldung
        LaunchedEffect(authState) {
            when (authState) {
                is AuthState.Success -> {
                    if (navController.currentDestination?.route != "main_app") {
                        navController.navigate("main_app") {
                            popUpTo("main_screen") { inclusive = true }
                            popUpTo("login_screen") { inclusive = true }
                        }
                    }
                }
                else -> {
                    // Keine Aktion, wenn nicht angemeldet
                }
            }
        }
    }

    @Composable
    fun MainAppScreen(
        authViewModel: AuthViewModel,
        onLogout: () -> Unit
    ) {
        val authState by authViewModel.authState.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkBrown1, MidBrown2
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (val state = authState) {
                    is AuthState.Success -> {
                        Text(
                            text = "Willkommen zurück!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Creme1,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = state.response.name,
                            fontSize = 24.sp,
                            color = Creme2,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = state.response.email,
                            fontSize = 16.sp,
                            color = Creme2,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        Text(
                            text = "Verbunden mit Server ✓",
                            fontSize = 16.sp,
                            color = Color.Green,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )



                        Button(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = "Abmelden",
                                color = Color.White
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = "Laden...",
                            fontSize = 18.sp,
                            color = Creme1
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun HomeScreen(
        onLoginClick: () -> Unit
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DarkBrown1, MidBrown2
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.watermark_chess),
                    contentDescription = "ChessPresso Logo",
                    modifier = Modifier.size(400.dp)
                )

                Text(
                    text = "ChessPresso",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Creme1
                )

                Text(
                    text = "Schach für Koffeinabhängige",
                    fontSize = 20.sp,
                    color = Creme2,
                    modifier = Modifier.padding(32.dp)
                )

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Anmelden",
                        color = Color.White
                    )
                }
            }
        }
    }
}
