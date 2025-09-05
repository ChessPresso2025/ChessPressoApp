package app.chesspresso.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.chesspresso.R
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeText

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CoffeeHeadlineText(
                text = "Willkommen bei \nChessPresso",
                textAlign = TextAlign.Center,
                fontSizeSp = 36
            )
            Spacer(modifier = Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .size(400.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(380.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.watermark_chess),
                        contentDescription = "ChessPresso Logo",
                        modifier = Modifier.size(340.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                    )
                }
            }

            CoffeeText(
                text = "Schach für Kaffeeliebhaber",
                modifier = Modifier.padding(32.dp),
                fontSizeSp = 24
            )

            // Login Button
            CoffeeButton(
                onClick = onLoginClick,
                content = {
                    Text("Anmelden")
                }
            )
        }
    }
}