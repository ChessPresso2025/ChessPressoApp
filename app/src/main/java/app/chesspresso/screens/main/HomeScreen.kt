package app.chesspresso.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeText

@Composable
fun HomeScreen(
    onPrivateGameClick: () -> Unit,
    onPublicGameClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Großes, zentrales Wasserzeichen im Hintergrund
        Image(
            painter = painterResource(id = app.chesspresso.R.drawable.watermark_chess),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.Center)
                .aspectRatio(1f),
            alpha = 0.06f
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            // Begrüßung und Slogan zentriert
            CoffeeHeadlineText(
                text = "Willkommen bei ChessPresso!",
                fontSizeSp = 26,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            CoffeeText(
                text = "Starte eine neue Partie oder spiele ein privates Spiel mit einem Freund.",
                fontSizeSp = 18,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            CoffeeCard(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CoffeeButton(
                            onClick = onPublicGameClick,
                            modifier = Modifier.fillMaxWidth(),
                            content = {
                                Text("Quick Match")
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CoffeeButton(
                            onClick = onPrivateGameClick,
                            modifier = Modifier.fillMaxWidth(),
                            content = {
                                Text("Private Lobby")
                            }
                        )
                    }
                },
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}