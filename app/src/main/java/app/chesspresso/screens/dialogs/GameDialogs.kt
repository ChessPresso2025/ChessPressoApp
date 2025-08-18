package app.chesspresso.screens.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
private fun DialogContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        )
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
        ) {
            Box {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Schließen")
                }
                content()
            }
        }
    }
}

@Composable
fun PrivateGameChoiceScreen(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "Privates Spiel",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth()) {
                Text("Privates Spiel erstellen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onJoinClick, modifier = Modifier.fillMaxWidth()) {
                Text("Privatem Spiel beitreten")
            }
        }
    }
}

@Composable
fun JoinPrivateGameScreen(
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Privatem Spiel beitreten", modifier = Modifier.padding(bottom = 16.dp))
                var lobbyId = remember { mutableStateOf("") }
                TextField(
                    value = lobbyId.value,
                    onValueChange = { if (it.length <= 6) lobbyId.value = it },
                    label = { Text("Lobby ID (6 Stellen)") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = lobbyId.value.length == 6
                ) {
                    Text("Beitreten")
                }
            }
        }
    }
}

@Composable
fun PublicGameScreen(
    onDismiss: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Öffentliches Spiel",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text("Warten auf Spieler...")
        }
    }
}
