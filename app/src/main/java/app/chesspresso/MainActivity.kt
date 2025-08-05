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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import app.chesspresso.websocket.WebSocketManager
import app.chesspresso.utils.PlayerIdManager

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

    fun onRegisterClick(){
        //register
    }

    @Composable
    fun MainScreen(){
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "main_screen"
        ){
            composable("main_screen") {
                HomeScreen(
                    onLoginClick = { navController.navigate("login_screen") }, 
                    onRegisterClick = { onRegisterClick() }
                )
            }

            composable("login_screen") {
                val authViewModel: AuthViewModel = hiltViewModel()
                LoginScreen(authViewModel)
            }

            //andere Seiten werden hier geaddet
        }
    }

    @Composable
    fun HomeScreen(
        onLoginClick: () -> Unit,
        onRegisterClick: () -> Unit
    ) {
        var isConnected by remember { mutableStateOf(false) }
        var connectionStatus by remember { mutableStateOf("Nicht verbunden") }
        var isConnecting by remember { mutableStateOf(false) }
        
        val authViewModel: AuthViewModel = hiltViewModel()

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

                // Server Verbindungs Status
                Text(
                    text = "Server Status: $connectionStatus",
                    fontSize = 14.sp,
                    color = when {
                        isConnecting -> Color.Yellow
                        isConnected -> Color.Green
                        else -> Color.Red
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Server Verbindungs Button
                Button(
                    onClick = {
                        if (!isConnected && !isConnecting) {
                            isConnecting = true
                            connectionStatus = "Verbinde..."

                            val playerId = authViewModel.getStoredPlayerInfo()?.playerId ?: "anonymous_user"
                            WebSocketManager.init(
                                playerId = playerId,
                                onSuccess = {
                                    isConnected = true
                                    isConnecting = false
                                    connectionStatus = "Verbunden"
                                },
                                onFailure = { error ->
                                    isConnected = false
                                    isConnecting = false
                                    connectionStatus = "Fehler: $error"
                                },
                                onDisconnect = {
                                    isConnected = false
                                    isConnecting = false
                                    connectionStatus = "Verbindung getrennt"
                                }
                            )
                        } else if (isConnected) {
                            WebSocketManager.disconnect()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = !isConnecting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isConnecting -> MaterialTheme.colorScheme.secondary
                            isConnected -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isConnecting -> "Verbinde..."
                            isConnected -> "Verbindung trennen"
                            else -> "Mit Server verbinden"
                        },
                        color = Color.White
                    )
                }

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Einloggen",
                        color = Color.White
                    )
                }

                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text(
                        text = "Registrieren",
                        color = Color.White
                    )
                }
            }
        }
    }
}
