package app.chesspresso.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object PermissionHelper {

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowCameraPermissionRationale(activity: FragmentActivity): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    }
}
