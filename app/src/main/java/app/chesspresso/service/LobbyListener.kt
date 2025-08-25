package app.chesspresso.service

import app.chesspresso.model.lobby.Lobby
import kotlinx.coroutines.flow.StateFlow

interface LobbyListener {
    val currentLobby: StateFlow<Lobby?>
    suspend fun leaveLobby(lobbyId: String): Result<Unit>
}