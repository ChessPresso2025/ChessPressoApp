package app.chesspresso.auth.presemtation

import android.provider.ContactsContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.tooling.preview.Preview
import com.google.android.gms.auth.api.identity.Identity


@Composable
fun LoginScreen(viewModel: AuthViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // Launcher für Google Sign-In (siehe vorherige Nachricht)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val oneTapClient = Identity.getSignInClient(context)
        val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
        credential.googleIdToken?.let { idToken ->
            viewModel.loginWithGoogle(idToken)
        }
    }

    // UI
    Column {
        Button(onClick = {
            // beginSignIn aufrufen → launcher.launch(...) (siehe vorher)
        }) {
            Text("Mit Google anmelden")
        }

        when (val state = authState) {
            is AuthState.Loading -> Text("Anmeldung läuft...")
            is AuthState.Success -> Text("Willkommen ${state.response.name}!")
            is AuthState.Error -> Text("Fehler: ${state.message}")
            AuthState.Idle -> {}
        }
    }
}