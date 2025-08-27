package app.chesspresso.screens.main

import androidx.lifecycle.ViewModel
import app.chesspresso.websocket.StompWebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class InfoViewModel @Inject constructor(
    private val webSocketService: StompWebSocketService
) : ViewModel() {
    // Server-Status direkt vom WebSocketService abfragen
    val serverStatus = webSocketService.serverStatus

    // Verbindungsstatus abfragen
    val connectionState = webSocketService.connectionState

    // Funktion zum manuellen Aktualisieren des Status (optional)
    fun requestServerStatus() {
        // Falls eine spezielle Statusabfrage implementiert werden soll
        webSocketService.requestOnlinePlayers() // Dies löst indirekt eine Server-Antwort aus, was den Status aktualisiert
    }
}
