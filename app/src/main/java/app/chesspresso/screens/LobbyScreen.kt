package app.chesspresso.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//public game
@Composable
fun PublicLobbyScreen() {
    Card(
        modifier = Modifier
            .padding(32.dp)
            .fillMaxSize()
    ) {
        Text("Öffentlichem Spiel beitreten ")
    }
}

//private game
@Composable
fun PrivateLobbyScreen() {
    Card(
        modifier = Modifier
            .padding(32.dp)
            .fillMaxSize()
    ) {
        Button(
            modifier = Modifier.padding(32.dp),
            onClick = {}
        ) {
            Text("Privates Spiel erstellen")
        }

        Button(
            modifier = Modifier.padding(32.dp),
            onClick = {

            }
        ) {
            Text("Privatem Spiel beitreten")
        }
    }
}

@Composable
fun JoinPrivateGameScreen() {
    Card(
        modifier = Modifier
            .padding(32.dp)
            .fillMaxSize()
    ) {

    }
}
