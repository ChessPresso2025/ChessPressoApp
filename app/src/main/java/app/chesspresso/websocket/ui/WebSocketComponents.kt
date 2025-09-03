package app.chesspresso.websocket.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.websocket.WebSocketViewModel

@Composable
fun WebSocketStatusIndicator(
    modifier: Modifier = Modifier,
    viewModel: WebSocketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Row(
        modifier = modifier
            .background(
                color = Color(viewModel.getConnectionStatusColor()),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Status-Indikator
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White)
        )

        CoffeeText(
            text = viewModel.getConnectionStatusText()
        )

        if (uiState.isConnected) {
            CoffeeText(
                text = "${uiState.onlinePlayerCount} online"
            )
        }
    }
}

@Composable
fun OnlinePlayersCard(
    modifier: Modifier = Modifier,
    viewModel: WebSocketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CoffeeCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoffeeText(
                    text = "Online Spieler (${uiState.onlinePlayerCount})",
                    style = MaterialTheme.typography.titleMedium
                )

                IconButton(
                    onClick = { viewModel.requestOnlinePlayers() },
                    enabled = uiState.isConnected
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Aktualisieren"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!uiState.isConnected) {
                CoffeeText(
                    text = "Nicht verbunden",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (uiState.onlinePlayers.isEmpty()) {
                CoffeeText(
                    text = "Keine Online-Spieler",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.onlinePlayers.toList()) { player ->
                        OnlinePlayerItem(playerName = player)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnlinePlayerItem(
    playerName: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )

        CoffeeText(
            text = playerName,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // Online-Indikator
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50))
        )
    }
}

@Composable
fun WebSocketDebugInfo(
    modifier: Modifier = Modifier,
    viewModel: WebSocketViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CoffeeCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            CoffeeText(
                text = "WebSocket Debug Info",
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            CoffeeText(
                text = "Status: ${viewModel.getConnectionStatusText()}",
                style = MaterialTheme.typography.bodySmall
            )

            CoffeeText(
                text = "Online Spieler: ${uiState.onlinePlayerCount}",
                style = MaterialTheme.typography.bodySmall
            )

            if (uiState.recentMessages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                CoffeeText(
                    text = "Letzte Nachrichten:",
                    style = MaterialTheme.typography.bodySmall
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 100.dp)
                ) {
                    items(uiState.recentMessages.takeLast(5)) { message ->
                        CoffeeText(
                            text = message.take(100) + if (message.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
