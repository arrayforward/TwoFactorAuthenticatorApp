package com.otp.authenticator.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        setHints(mapOf(com.google.zxing.DecodeHintType.POSSIBLE_FORMATS to listOf(
            com.google.zxing.BarcodeFormat.QR_CODE
        )))
    }

    private var lastScanned: String? = null
    private var lastScanTime = 0L

    override fun analyze(image: ImageProxy) {
        if (image.format != android.graphics.ImageFormat.YUV_420_888) {
            image.close()
            return
        }

        val planes = image.planes
        if (planes.size < 3) {
            image.close()
            return
        }

        val yPlane = planes[0]
        val buffer: ByteBuffer = yPlane.buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val source = PlanarYUVLuminanceSource(
            data,
            image.width,
            image.height,
            0, 0,
            image.width,
            image.height,
            false
        )

        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decode(bitmap)
            val text = result.text
            val now = System.currentTimeMillis()
            if (text != lastScanned || now - lastScanTime > 3000) {
                lastScanned = text
                lastScanTime = now
                onQrCodeScanned(text)
            }
        } catch (_: Exception) {
        } finally {
            image.close()
        }
    }
}
