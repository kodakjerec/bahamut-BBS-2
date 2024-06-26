package com.kota.Bahamut.Service

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESCrypt {
    private const val key:String = "1989060419890604"
    private const val transformation = "AES/CBC/PKCS5Padding"

    private val keySpec = SecretKeySpec(key.toByteArray(), "AES")
    private val ivParameterSpec = IvParameterSpec(key.toByteArray(), 0, 16)

    // 加密
    fun encrypt(input: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec)
        val encrypt = cipher.doFinal(input.toByteArray())
        return Base64.getEncoder().encodeToString(encrypt)
    }
    // 解密
    fun decrypt(input: String): String {
        val cipher = Cipher.getInstance(transformation)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec)
        val decrypt = cipher.doFinal(Base64.getDecoder().decode(input))
        return String(decrypt)
    }
}