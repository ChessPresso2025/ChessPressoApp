package app.chesspresso.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.chesspresso.ui.theme.CoffeeButton

@Composable
fun HomeScreen(
    onPrivateGameClick: () -> Unit,
    onPublicGameClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
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
}