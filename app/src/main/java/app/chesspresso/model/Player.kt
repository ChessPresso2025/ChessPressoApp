package app.chesspresso.model

class Player(
    playerID: String, username: String
) {
    private var playedGames: Int = 0
    private var win: Int = 0
    private var lose: Int = 0
    private var draw: Int = 0

    public fun winPercent(): Double {
        if (playedGames != 0) return win / playedGames * 100.toDouble()
        return 0.0;
    }

    public fun losePercent(): Double {
        if (playedGames != 0) return lose / playedGames * 100.toDouble()
        return 0.0;
    }

    public fun drawPercent(): Double {
        if (playedGames != 0) return draw / playedGames * 100.toDouble()
        return 0.0;
    }
}