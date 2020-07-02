package com.example.download_and_save.service

import android.content.SharedPreferences
import android.util.Base64
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class SecretkeyGenerator{
    @Throws(Exception::class)
    fun generateSecretKey(): SecretKey? {
        val secureRandom = SecureRandom()
        val keyGenerator = KeyGenerator.getInstance("AES")
        //generate a key with secure random
        keyGenerator?.init(128, secureRandom)
        return keyGenerator?.generateKey()
    }

    private fun saveSecretKey(sharedPref: SharedPreferences, secretKey: SecretKey): String {
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
        sharedPref.edit().putString("secretKeyPref", encodedKey).apply()
        return encodedKey
    }

    fun getSecretKey(sharedPref: SharedPreferences): SecretKey {

        val key = sharedPref.getString("secretKeyPref", null)

        if (key == null) {
            //generate secure random
            val secretKey = generateSecretKey()
            saveSecretKey(sharedPref, secretKey!!)
            return secretKey
        }

        val decodedKey = Base64.decode(key, Base64.NO_WRAP)

        return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }
}