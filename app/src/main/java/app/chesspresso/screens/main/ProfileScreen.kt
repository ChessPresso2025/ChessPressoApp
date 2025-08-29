package app.chesspresso.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
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
import app.chesspresso.auth.presentation.AuthViewModel
import androidx.navigation.NavHostController

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel,
    outerNavController: NavHostController
) {
    val uiState by viewModel.statsState.collectAsState()
    val usernameChangeState by viewModel.usernameChangeState.collectAsState()
    val (newUsername, setNewUsername) = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LogoutAndNavigateToLogin -> {
                    viewModel.resetStatsState()
                    authViewModel.logout()
                    outerNavController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
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
                is StatsUiState.Idle -> { /* nichts anzeigen */ }
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
                    showDialog.value = true
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

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Achtung") },
            text = { Text("Um den Benutzernamen zu ändern, musst du dich neu anmelden. Fortfahren?") },
            confirmButton = {
                Button(onClick = {
                    showDialog.value = false
                    viewModel.changeUsername(newUsername)
                }) { Text("Ja") }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false }) { Text("Abbrechen") }
            }
        )
    }
}
