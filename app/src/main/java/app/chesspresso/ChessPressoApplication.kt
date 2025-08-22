package app.chesspresso

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import app.chesspresso.websocket.StompWebSocketService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ChessPressoApplication : Application(), DefaultLifecycleObserver {

    @Inject
    lateinit var webSocketService: StompWebSocketService

    companion object {
        private const val TAG = "ChessPressoApp"
    }

    override fun onCreate() {
        super<Application>.onCreate()
        Log.d(TAG, "Application created")

        // Registriere Lifecycle Observer für App-Events
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        Log.d(TAG, "App is going to background")

        // Sende App-Closing-Nachricht wenn App in den Hintergrund geht
        sendAppClosingMessage("app-background")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onDestroy(owner)
        Log.d(TAG, "App process is being destroyed")
        
        // Sende finale App-Closing-Nachricht bei Process-Zerstörung
        sendAppClosingMessage("app-destroyed")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "Application is terminating")

        // Sende finale App-Closing-Nachricht bei App-Termination (wird selten aufgerufen)
        sendAppClosingMessage("app-terminated")
    }

    private fun sendAppClosingMessage(reason: String) {
        try {
            webSocketService.sendAppClosingMessageWithReason(reason)
            Log.d(TAG, "App closing message sent successfully with reason: $reason")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send app closing message: ${e.message}")
        }
    }
}
