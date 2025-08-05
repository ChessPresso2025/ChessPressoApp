package app.chesspresso.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

object PlayerIdManager {
    private const val PREFS_NAME = "chesspresso_prefs"
    private const val PLAYER_ID_KEY = "player_id"
    
    fun getOrCreatePlayerId(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var playerId = prefs.getString(PLAYER_ID_KEY, null)
        
        if (playerId == null) {
            playerId = UUID.randomUUID().toString()
            prefs.edit().putString(PLAYER_ID_KEY, playerId).apply()
        }
        
        return playerId
    }
}