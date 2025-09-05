package app.chesspresso.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.ui.theme.CoffeeTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.lint.kotlin.metadata.Visibility

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isRegistering by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // FocusRequester für Autofokus auf Benutzername
    val usernameFocusRequester = remember { FocusRequester() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentIsRegistering = rememberUpdatedState(isRegistering)

    // Autofokus beim ersten Anzeigen (und wenn von Registrierung zurück zu Login gewechselt wird)
    LaunchedEffect(lifecycleOwner, currentIsRegistering.value) {
        if (!currentIsRegistering.value) {
            // Nur im Login-Modus
            usernameFocusRequester.requestFocus()
        }
    }

    // Vorausgefüllter Benutzername, falls vorhanden
    val storedUsername = remember { authViewModel.getStoredUsername() }
    if (username.isEmpty() && storedUsername != null) {
        username = storedUsername
    }

    // Navigation nach erfolgreichem Login
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Snackbar für Fehler und Erfolg
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }
            is AuthState.Success -> {
                snackbarHostState.showSnackbar("Willkommen ${state.response.name}!")
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CoffeeCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                CoffeeHeadlineText(
                    text = if (isRegistering) "Registrierung" else "Anmeldung",
                    fontSizeSp = 24
                )

                CoffeeTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Benutzername",
                    modifier = Modifier.fillMaxWidth().focusRequester(usernameFocusRequester),
                    singleLine = true,
                    enabled = authState !is AuthState.Loading,
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Person, contentDescription = "Benutzername") }
                )

                if (isRegistering) {
                    CoffeeTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "E-Mail",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = authState !is AuthState.Loading,
                        leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Email, contentDescription = "E-Mail") }
                    )
                }

                CoffeeTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Passwort",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = authState !is AuthState.Loading,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Filled.Lock, contentDescription = "Passwort") },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        val desc = if (passwordVisible) "Passwort verbergen" else "Passwort anzeigen"
                        androidx.compose.material3.IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            androidx.compose.material3.Icon(image, contentDescription = desc)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                CoffeeButton(
                    onClick = {
                        if (isRegistering) {
                            if (username.isNotBlank() && password.isNotBlank() && email.isNotBlank()) {
                                authViewModel.register(username.trim(), password, email.trim())
                            } else {
                                authViewModel.setErrorMessage("Alle Felder müssen ausgefüllt werden")
                            }
                        } else {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(username.trim(), password)
                            } else {
                                authViewModel.setErrorMessage("Benutzername und Passwort müssen ausgefüllt werden")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading,
                    content = {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(text = if (isRegistering) "Registrieren" else "Anmelden")
                    }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    CoffeeText(
                        text = if (isRegistering) "Bereits ein Konto?" else "Noch kein Konto?"
                    )

                    TextButton(
                        onClick = {
                            isRegistering = !isRegistering
                            email = ""
                        },
                        enabled = authState !is AuthState.Loading,
                        content = {
                            Text(text = if (isRegistering) "Anmelden" else "Registrieren", fontSize = 20.sp)
                        }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}