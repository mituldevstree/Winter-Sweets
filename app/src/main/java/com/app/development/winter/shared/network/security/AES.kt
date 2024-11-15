package com.app.development.winter.shared.network.security

import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.jvm.Throws

object AES {
    @Throws(NullPointerException::class)
    fun encode(string: String, Key: String): String? {
        try {
            val skeySpec = getKey(Key)
            val cipher =
                Cipher.getInstance("AES/ECB/PKCS7Padding")
            val stringToEncode =
                string.toByteArray(StandardCharsets.UTF_8)
            // encryption pass
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec)
            return com.app.development.winter.shared.network.security.JavaUtil.bytesToHex(cipher.doFinal(stringToEncode))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @Throws(
        UnsupportedEncodingException::class,
        NoSuchAlgorithmException::class
    )
    fun getKey(Key: String): SecretKeySpec {
        val passwordBytes =
            Key.toByteArray(StandardCharsets.UTF_8)
        return SecretKeySpec(
            MessageDigest.getInstance("MD5").digest(passwordBytes), "AES"
        )
    }


    @Throws(java.lang.Exception::class)
    fun decrypt(hexString: String): String {
        val dcipher: Cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")

        dcipher.init(Cipher.DECRYPT_MODE, getKey("appWorldKey"))

        val toByte = toByte(hexString)

        val clearbyte: ByteArray = dcipher.doFinal(toByte)

        return String(clearbyte)
    }

    private fun toByte(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for (i in 0 until len) result[i] =
            Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).toByte()
        return result
    }
}