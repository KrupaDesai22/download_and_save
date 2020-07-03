package com.example.download_and_save.service

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecretkeyGenerator{
    fun getKeyForAlias(alias: String, keyStore: KeyStore): SecretKey {
        val keyEntry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
        return keyEntry.secretKey
    }

    fun getSecretKey(alias: String): SecretKey {
        val keygen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keygen.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keygen.generateKey()
    }
}