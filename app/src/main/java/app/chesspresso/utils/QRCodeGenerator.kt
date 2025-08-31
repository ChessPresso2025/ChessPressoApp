package app.chesspresso.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRCodeGenerator {

    fun generateQRCodeBitmap(
        content: String,
        width: Int = 512,
        height: Int = 512
    ): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height, hints)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateLobbyQRCode(lobbyId: String): Bitmap? {
        // Format für Lobby-QR-Code: "CHESSPRESSO_LOBBY:{lobbyId}"
        val qrContent = "CHESSPRESSO_LOBBY:$lobbyId"
        return generateQRCodeBitmap(qrContent)
    }

    fun parseLobbyQRCode(qrContent: String): String? {
        return if (qrContent.startsWith("CHESSPRESSO_LOBBY:")) {
            qrContent.substringAfter("CHESSPRESSO_LOBBY:")
        } else {
            null
        }
    }
}
