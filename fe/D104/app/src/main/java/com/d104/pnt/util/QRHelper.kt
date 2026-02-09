package com.d104.pnt.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

fun generateQrCode(content: String, size: Int = 256): Bitmap? {
    return try {
        val barcodeEncoder = BarcodeEncoder()
        barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, size, size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}