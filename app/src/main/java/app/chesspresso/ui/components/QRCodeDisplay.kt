package app.chesspresso.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.utils.QRCodeGenerator

@Composable
fun QRCodeDisplay(
    lobbyId: String,
    modifier: Modifier = Modifier
) {
    var qrBitmap by remember(lobbyId) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(lobbyId) {
        qrBitmap = QRCodeGenerator.generateLobbyQRCode(lobbyId)
    }

    CoffeeCard(
        modifier = modifier.fillMaxWidth(),
        content = {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CoffeeText(
                    text = "Scanne diesen QR-Code, um der Lobby beizutreten:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QR-Code für Lobby $lobbyId",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                CoffeeText(text = "Lobby-ID: $lobbyId")
            }
        },
    )
}
