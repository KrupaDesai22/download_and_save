package com.example.download_and_save.service

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec

class EncryptDecrypt{
    private lateinit var encryptionIv : ByteArray
    private var TRANSFORMATION : String = "AES/GCM/NoPadding"

    private fun generateKeyStore(alias : String): SecretKey {
        val ANDROID_KEY_STORE = "AndroidKeyStore"
        val keyGenerator: KeyGenerator = KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParameterSpec);
        val secretKey: SecretKey = keyGenerator.generateKey()
        return secretKey
    }

    private fun getIV(): IvParameterSpec {
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        // 1
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
        return IvParameterSpec(iv)
    }

    @Throws(
        IOException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class
    )
    fun decryptAES(
        Encrypt_FilePath: String?,
        Decrypt_FilePath: String?
    ) {
        val fis = FileInputStream(Encrypt_FilePath)
        val fos = FileOutputStream(Decrypt_FilePath)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // val spec = GCMParameterSpec(128, encryptionIv)
        val spec = GCMParameterSpec(128, encryptionIv)
        cipher.init(Cipher.DECRYPT_MODE, generateKeyStore("PUBLIC_KEY"), spec)
        val cis = CipherInputStream(fis, cipher)
        var b: Int
        val data = ByteArray(2048)
        while (cis.read(data).also { b = it } != -1) {
            val decrypted: ByteArray =  cipher.update(data)
            fos.write(decrypted, 0, b)
        }
        fos.flush()
        fos.close()
        cis.close()
    }


}