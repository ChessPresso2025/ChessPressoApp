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
    val usernameChangeState by viewModel.usernameChangeState.collectAsState()
    val passwordChangeState by viewModel.passwordChangeState.collectAsState()
    val (newUsername, setNewUsername) = remember { mutableStateOf("") }
    val (oldPassword, setOldPassword) = remember { mutableStateOf("") }
    val (newPassword, setNewPassword) = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val showPasswordDialog = remember { mutableStateOf(false) }

    // Profildaten beim ersten Anzeigen laden
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    // Nach erfolgreicher Änderung Eingabefeld leeren und Status zurücksetzen
    LaunchedEffect(usernameChangeState) {
        if (usernameChangeState is UsernameChangeState.Success) {
            setNewUsername("")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LogoutAndNavigateToLogin -> {
                    // resetStatsState entfernt, da Stats nicht mehr verwendet werden
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
            // Name und E-Mail aus UserProfileState anzeigen
            val userProfileState = viewModel.userProfileState.collectAsState().value
            when (userProfileState) {
                is UserProfileUiState.Loading -> Text("Lade Profildaten...")
                is UserProfileUiState.Error -> Text("Fehler: " + userProfileState.message)
                is UserProfileUiState.Success -> {
                    val profile = userProfileState.profile
                    Text("Name: ${profile.username}")
                    Text("E-Mail: ${profile.email}")
                }
            }
            // StatsUiState und uiState entfernt, da Stats nicht mehr verwendet werden
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
            // --- Passwort ändern UI ---
            OutlinedTextField(
                value = oldPassword,
                onValueChange = setOldPassword,
                label = { Text("Altes Passwort") },
                modifier = Modifier.padding(top = 32.dp),
                singleLine = true
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = setNewPassword,
                label = { Text("Neues Passwort") },
                modifier = Modifier.padding(top = 8.dp),
                singleLine = true
            )
            Button(
                onClick = {
                    showPasswordDialog.value = true
                },
                enabled = passwordChangeState !is PasswordChangeState.Loading && oldPassword.length >= 4 && newPassword.length in 4..64,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Passwort ändern")
            }
            when (passwordChangeState) {
                is PasswordChangeState.Loading -> Text("Ändere Passwort...")
                is PasswordChangeState.Success -> Text("Passwort erfolgreich geändert!", color = Color.Green)
                is PasswordChangeState.Error -> Text((passwordChangeState as PasswordChangeState.Error).message, color = Color.Red)
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

    if (showPasswordDialog.value) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog.value = false },
            title = { Text("Achtung") },
            text = { Text("Um das Passwort zu ändern, musst du dich neu anmelden. Fortfahren?") },
            confirmButton = {
                Button(onClick = {
                    showPasswordDialog.value = false
                    viewModel.changePassword(oldPassword, newPassword)
                }) { Text("Ja") }
            },
            dismissButton = {
                Button(onClick = { showPasswordDialog.value = false }) { Text("Abbrechen") }
            }
        )
    }
}
