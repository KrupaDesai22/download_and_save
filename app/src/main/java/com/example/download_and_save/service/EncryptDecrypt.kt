package com.example.download_and_save.service

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class EncryptDecrypt(
    private val pref: SharedPreferences,
    private val fileService: FileIOService,
    private val secretkeyGenerator: SecretkeyGenerator
) {
    @Throws(Exception::class)
    fun encryptInKeyStore(alias: String, fileData: ByteArray, filePath: String): Unit {
        Log.d("DEBUG", "12")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretkeyGenerator.getSecretKey(alias))
        Log.d("DEBUG", "123")
        val encText = cipher.doFinal(fileData)
        val encodedIv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        Log.d("DEBUG", "1234")
        pref.edit().putString("enc_iv", encodedIv).apply()
        fileService.saveFile(encText, filePath)
        Log.d("DEBUG", "12345")
    }

    @Throws(Exception::class)
    fun decryptFromKeyStore(alias: String, filePath: String): ByteArray {

        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val encodedIv = pref.getString("enc_iv", null)
        val iv = Base64.decode(encodedIv, Base64.NO_WRAP)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretkeyGenerator.getKeyForAlias(alias, keyStore), spec)

        return cipher.doFinal(fileService.readFile(filePath))
    }

}
