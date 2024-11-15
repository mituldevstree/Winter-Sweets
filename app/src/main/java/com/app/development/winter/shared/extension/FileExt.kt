package com.app.development.winter.shared.extension

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.min

fun File.deleteIfExists() {
    if (exists()) {
        delete()
    }
}

fun File.rotateImageFromExternalStorage(): Bitmap {
    val bitmap = BitmapFactory.decodeFile(path)
    val matrix = Matrix()
    matrix.postRotate(getImageRotation(inputStream()))
    val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    val os = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 50, os)
    val array = os.toByteArray()
    return BitmapFactory.decodeByteArray(array, 0, array.size)
}

fun File.tryCropImageToSquare(): Bitmap? {
    return try {
        cropImageToSquare()
    } catch (e: Exception) {
        null
    }
}

private fun File.cropImageToSquare(): Bitmap {
    val bitmap = rotateImageFromExternalStorage()
    val bitmapRatio = bitmap.height / bitmap.width.toFloat()
    return if (bitmapRatio == 1f) {
        bitmap
    } else {
        val minSize = min(bitmap.height, bitmap.width)
        if (bitmap.height > bitmap.width) {
            Bitmap.createBitmap(
                bitmap,
                0,
                bitmap.height / 2 - bitmap.width / 2,
                minSize,
                minSize
            )
        } else {
            Bitmap.createBitmap(
                bitmap,
                bitmap.width / 2 - bitmap.height / 2,
                0,
                minSize,
                minSize
            )
        }
    }
}
