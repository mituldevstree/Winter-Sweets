package com.app.development.winter.utility

import android.graphics.Bitmap
import android.util.Base64
import java.io.*

object Base64Util {
    fun encodeBase64(array: ByteArray): String = Base64.encodeToString(array, Base64.DEFAULT)
    fun decodeBase64(array: ByteArray): ByteArray? = Base64.decode(array, Base64.DEFAULT)
    fun decodeBase64(encoded: String?): ByteArray? = Base64.decode(encoded, Base64.DEFAULT)

    fun convertImage(bitmap: Bitmap): String? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
    fun getBase64StringViaInputStream(localFile: File?): String? {
        return try {
            var bytesRead: Int
            val buffer = ByteArray(8192)
            val inputStream: InputStream = FileInputStream(localFile)
            val output = ByteArrayOutputStream()
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
            Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    }
}