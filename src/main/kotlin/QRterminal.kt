import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.common.BitMatrix
import com.google.zxing.MultiFormatWriter
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.Throws
import kotlin.jvm.JvmStatic

object QRterminal {
    fun getQr(text: String?): String {
        var s = ""
        val width = 20
        val height = 20
        val qrParam = Hashtable<EncodeHintType, Any?>()
        qrParam[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
        qrParam[EncodeHintType.CHARACTER_SET] = "utf-8"
        try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, qrParam)
            s = toAscii(bitMatrix)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return s
    }

    fun toAscii(bitMatrix: BitMatrix): String {
        val sb = StringBuilder()
        for (rows in 0 until bitMatrix.height) {
            for (cols in 0 until bitMatrix.width) {
                val x = bitMatrix[rows, cols]
                if (!x) {
                    sb.append("\u001b[47m  \u001b[0m")
                } else {
                    sb.append("\u001b[40m  \u001b[0m")
                }
            }
            sb.append("\n")
        }
        return sb.toString()
    }
}