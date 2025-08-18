package app.chesspresso.auth.presemtation

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.navigation.NavController
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // Launcher für echte Google Sign-In mit ID Token
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Google Sign-In result: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(Exception::class.java)

                // Verwende den echten ID Token anstatt Mock-Daten
                val idToken = account?.idToken
                if (idToken != null) {
                    Log.d("LoginScreen", "Google Sign-In successful with ID token, sending to server...")
                    Log.d("LoginScreen", "ID Token length: ${idToken.length}")
                    viewModel.loginWithGoogle(idToken)
                } else {
                    Log.e("LoginScreen", "Google ID Token is null")
                    viewModel.setErrorMessage("Google ID Token konnte nicht abgerufen werden")
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Google Sign-In error: ${e.message}", e)
                viewModel.setErrorMessage("Google Sign-In Fehler: ${e.message}")
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.w("LoginScreen", "Google Sign-In was cancelled by user")
            viewModel.setErrorMessage("Anmeldung wurde abgebrochen")
        } else {
            Log.w("LoginScreen", "Google Sign-In failed with result code: ${result.resultCode}")
            viewModel.setErrorMessage("Google Sign-In fehlgeschlagen")
        }
    }

    // UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Button(
                onClick = {
                    Log.d("LoginScreen", "Starting Google Sign-In with ID Token request...")

                    try {
                        // Konfiguration für echte ID Token mit Web Client ID
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(BuildConfig.WEB_CLIENT_ID) // Wichtig: Verwende Web Client ID für ID Token
                            .requestEmail()
                            .requestProfile()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(context, gso)

                        googleSignInClient.signOut().addOnCompleteListener {
                            Log.d("LoginScreen", "Previous sign-out completed, launching sign-in")
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Error starting Google Sign-In: ${e.message}", e)
                        viewModel.setErrorMessage("Fehler beim Starten der Google-Anmeldung: ${e.message}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                Text("Mit Google anmelden")
            }



            when (val state = authState) {
                is AuthState.Loading -> Text("Anmeldung läuft...")
                is AuthState.Success -> Text("Willkommen ${state.response.name}!")
                is AuthState.Error -> Text("Fehler: ${state.message}")
                AuthState.Idle -> Text("Bereit zum Anmelden")
            }
        }
    }
}