package app.chesspresso.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToWelcome: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> onNavigateToMain()
            is AuthState.Error, AuthState.Idle -> {
                delay(1200)
                onNavigateToWelcome()
            }
            AuthState.Loading -> { /* Splash bleibt */ }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = app.chesspresso.R.drawable.watermark_chess),
            contentDescription = "App Logo",
            modifier = Modifier.size(180.dp)
        )
    }
}
