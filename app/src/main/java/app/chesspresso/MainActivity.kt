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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import app.chesspresso.ui.theme.ChessPressoAppTheme
import app.chesspresso.ui.theme.Creme1
import app.chesspresso.ui.theme.Creme2
import app.chesspresso.ui.theme.DarkBrown1
import app.chesspresso.ui.theme.MidBrown2

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

    fun onLoginClick(){
        //login
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
                HomeScreen({onLoginClick()}, {onRegisterClick()})
            }

            //andere Seiten werden hier geaddet
        }
    }

    @Composable
    fun HomeScreen(
        onLoginClick: () -> Unit,
        onRegisterClick: () -> Unit
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
