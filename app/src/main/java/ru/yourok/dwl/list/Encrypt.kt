package ru.yourok.dwl.list

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


/**
 * Created by yourok on 09.11.17.
 */
class EncKey {
    var key: ByteArray? = null
    var iv: ByteArray? = null

    fun decrypt(buf: ByteArray): ByteArray {
        val skeySpec = SecretKeySpec(key, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, IvParameterSpec(iv))
        return cipher.doFinal(buf)
    }
}