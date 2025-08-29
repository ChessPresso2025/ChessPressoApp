package app.chesspresso.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.chesspresso.screens.main.UsernameChangeState

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.statsState.collectAsState()
    val usernameChangeState by viewModel.usernameChangeState.collectAsState()
    val (newUsername, setNewUsername) = remember { mutableStateOf("") }

    // Stats beim ersten Anzeigen laden
    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    // Nach erfolgreicher Änderung Eingabefeld leeren und Status zurücksetzen
    LaunchedEffect(usernameChangeState) {
        if (usernameChangeState is UsernameChangeState.Success) {
            setNewUsername("")
            // Optional: Stats neu laden, falls Username angezeigt wird
            // viewModel.loadStats()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when (uiState) {
                is StatsUiState.Loading -> Text("Lade Statistiken...")
                is StatsUiState.Error -> Text("Fehler: " + (uiState as StatsUiState.Error).message)
                is StatsUiState.Success -> {
                    val stats = (uiState as StatsUiState.Success).stats
                    Text("Siege: ${stats.wins}\nNiederlagen: ${stats.losses}\nRemis: ${stats.draws}\nGesamt: ${stats.wins + stats.losses + stats.draws}")
                }
            }
            // --- Username ändern UI ---
            OutlinedTextField(
                value = newUsername,
                onValueChange = setNewUsername,
                label = { Text("Neuer Benutzername") },
                modifier = Modifier.padding(top = 32.dp)
            )
            Button(
                onClick = {
                    viewModel.changeUsername(newUsername)
                },
                enabled = usernameChangeState !is UsernameChangeState.Loading && newUsername.length in 3..32,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Benutzernamen ändern")
            }
            when (usernameChangeState) {
                is UsernameChangeState.Loading -> Text("Ändere Benutzernamen...")
                is UsernameChangeState.Success -> Text("Benutzername erfolgreich geändert!", color = Color.Green)
                is UsernameChangeState.Error -> Text((usernameChangeState as UsernameChangeState.Error).message, color = Color.Red)
                else -> {}
            }
        }
    }
}
