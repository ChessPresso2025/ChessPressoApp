package app.chesspresso.model

enum class GameDuration( val minutes: Int, val description: String? = null) {
    SHORT(5, "kurzes Spiel"), // 5 minutes
    MEDIUM(10, "mittleres Spiel"), // 10 minutes
    LONG(15, "langes Spiel"), // 15 minutes
    UNLIMITED(0, "unbegrenzt") // no time limit, for private games
}