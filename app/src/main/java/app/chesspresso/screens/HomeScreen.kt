package app.chesspresso.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onPrivateGameClick: () -> Unit,
               onPublicGameClick: () -> Unit,
               onNavigate: (NavigationItem) -> Unit)
{
    val selectedItem = NavigationItem.Gameplay

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = app.chesspresso.R.drawable.watermark_chess), // <- dein Logo aus drawable
                            contentDescription = "App-Logo",
                            modifier = Modifier
                                .size(32.dp) // Passe die Größe nach Wunsch an
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text("ChessPresso") // oder dein App-Name
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationItem.entries.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = item == selectedItem,
                        onClick = { onNavigate(item) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = onPrivateGameClick, modifier = Modifier.fillMaxWidth()) {
                Text("Privates Spiel starten")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPublicGameClick, modifier = Modifier.fillMaxWidth()) {
                Text("Öffentliches Spiel starten")
            }
        }
    }
}

enum class NavigationItem(val label: String, val icon: ImageVector) {
    Profile("Profil", Icons.Default.Person),
    Stats("Statistik", Icons.Default.Search), //durch Statistik Icon ersetzen
    Gameplay("Spielen", Icons.Default.Home), //durch Schach-Icon ersetzen
    Settings("Einstellungen", Icons.Default.Settings)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onPrivateGameClick = {},
        onPublicGameClick = {},
        onNavigate = {}
    )
}