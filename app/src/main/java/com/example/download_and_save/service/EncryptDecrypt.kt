package com.example.download_and_save.service

import android.content.SharedPreferences
import android.util.Log
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptDecrypt(private val pref: SharedPreferences, private val fileService: FileIOService, private val secretkeyGenerator: SecretkeyGenerator){
    @Throws(Exception::class)
    fun encrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = yourKey.getEncoded()
        val skeySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IvParameterSpec(ByteArray(cipher.getBlockSize())))
        return cipher.doFinal(fileData)
    }

    fun encryptFile(fileData: ByteArray, filePath: String) {
        try {
            //get secret key
            val secretKey = secretkeyGenerator.getSecretKey(pref)
            //encrypt file
            val encodedData = encrypt(secretKey, fileData)

            fileService.saveFile(encodedData, filePath)

        } catch (e: Exception) {
            Log.d("mTag", e.message)
        }
    }

    @Throws(Exception::class)
    fun decrypt(yourKey: SecretKey, fileData: ByteArray): ByteArray {
        val decrypted: ByteArray
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.DECRYPT_MODE, yourKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        decrypted = cipher.doFinal(fileData)
        return decrypted
    }
  
    fun decryptEncryptedFile(filePath: String): ByteArray {
        val filePath = filePath
        val fileData = fileService.readFile(filePath)
        val secretKey = secretkeyGenerator.getSecretKey(pref)
        return decrypt(secretKey, fileData)
    }

}
