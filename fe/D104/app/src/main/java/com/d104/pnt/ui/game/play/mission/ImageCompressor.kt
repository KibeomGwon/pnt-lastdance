package com.d104.pnt.ui.game.play.mission

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream

/**
 * 이미지 메타데이터 정보
 */
data class ImageInfo(
    val width: Int,
    val height: Int,
    val fileSizeKB: Long,
    val quality: Int
)

private fun getImageRotation(file: File): Int {
    return try {
        val exif = ExifInterface(file.absolutePath)
        when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    } catch (e: Exception) {
        0
    }
}

/**
 * 비트맵 회전 함수
 */
private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    if (degrees == 0) return bitmap

    val matrix = Matrix().apply {
        postRotate(degrees.toFloat())
    }

    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}

/**
 * 이미지 파일을 압축하는 함수
 */
fun compressImage(
    originalFile: File,
    maxWidth: Int = 1280,
    maxHeight: Int = 720,
    quality: Int = 80
): File {
    val rotation = getImageRotation(originalFile)

    var originalBitmap = BitmapFactory.decodeFile(originalFile.absolutePath)

    if (rotation != 0) {
        val rotatedBitmap = rotateBitmap(originalBitmap, rotation)
        originalBitmap.recycle()
        originalBitmap = rotatedBitmap
    }

    val originalWidth = originalBitmap.width
    val originalHeight = originalBitmap.height

    val scale = minOf(
        maxWidth.toFloat() / originalWidth,
        maxHeight.toFloat() / originalHeight,
        1.0f
    )

    val newWidth = (originalWidth * scale).toInt()
    val newHeight = (originalHeight * scale).toInt()

    val resizedBitmap = Bitmap.createScaledBitmap(
        originalBitmap,
        newWidth,
        newHeight,
        true
    )

    val compressedFile = File(
        originalFile.parent,
        "compressed_${originalFile.name}"
    )

    FileOutputStream(compressedFile).use { out ->
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
    }

    try {
        val srcExif = ExifInterface(originalFile.absolutePath)
        val destExif = ExifInterface(compressedFile.absolutePath)

        destExif.setAttribute(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL.toString()
        )
        destExif.saveAttributes()
    } catch (e: Exception) {

    }

    // 메모리 해제
    originalBitmap.recycle()
    resizedBitmap.recycle()

    return compressedFile
}

/**
 * 이미지 파일의 메타데이터 가져오는 함수
 */
fun getImageInfo(file: File, quality: Int = 80): ImageInfo {
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(file.absolutePath, options)

    return ImageInfo(
        width = options.outWidth,
        height = options.outHeight,
        fileSizeKB = file.length() / 1024,
        quality = quality
    )
}