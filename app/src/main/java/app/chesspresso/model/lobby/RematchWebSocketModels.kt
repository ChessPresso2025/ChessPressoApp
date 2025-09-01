package app.chesspresso.model.lobby

import kotlinx.serialization.Serializable

@Serializable
data class RematchRequest(
    val type: String = "rematch-request",
    val lobbyId: String,
    val playerId: String
)

@Serializable
data class RematchOffer(
    val type: String = "rematch-offer",
    val lobbyId: String,
    val fromPlayerId: String,
    val toPlayerId: String
)

@Serializable
data class RematchResponse(
    val type: String = "rematch-response",
    val lobbyId: String,
    val playerId: String,
    val response: String // "accepted" oder "declined"
)

@Serializable
data class RematchResult(
    val type: String = "rematch-result",
    val lobbyId: String,
    val result: String // "accepted" oder "declined"
)
