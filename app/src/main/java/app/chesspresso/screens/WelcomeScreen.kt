package app.chesspresso.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.chesspresso.R
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeText

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()

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

            CoffeeText(
                text = "ChessPresso"
            )

            CoffeeText(
                text = "Schach für Koffeinabhängige",
                modifier = Modifier.padding(32.dp)
            )

            // Login Button
            CoffeeButton(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth().padding(vertical = 8.dp),
                content = {
                    Text("Anmelden")
                }
            )
        }
    }
}