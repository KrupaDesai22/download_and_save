package com.example.download_and_save

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.download_and_save.service.FileDownloadClient
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.security.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec


class MainActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var encryptionIv : ByteArray
    private var TRANSFORMATION : String = "AES/GCM/NoPadding"
//    private val basePath =
//        this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button=findViewById(R.id.button2)
        button.setOnClickListener(View.OnClickListener {
            showFile()
        })

        val basePath =
            this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files"

        val fileList = listOf<String>(
//            "https://en.unesco.org/inclusivepolicylab/sites/default/files/dummy-pdf_2.pdf",
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
        )
        downloadFileIfNotExists(fileList,basePath)

        getIV()
    }

    private fun getIV(): IvParameterSpec {
        val ivRandom = SecureRandom() //not caching previous seeded instance of SecureRandom
        // 1
        val iv = ByteArray(16)
        ivRandom.nextBytes(iv)
       return IvParameterSpec(iv)
    }

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

    private fun downloadFileIfNotExists(fileList: List<String>,basePath:String) {
        fileList.forEach {
            val file = File(basePath + "/" + getFileName(it))
            if (!file.exists())
                downloadFiles(fileList)
        }
    }

    private fun showFile(){

        val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy.pdf")

        decryptAES(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy.pdf",
            this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy_decrypt.pdf")
        val intent = Intent(Intent.ACTION_VIEW)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val apkURI = FileProvider.getUriForFile(
                applicationContext,
                "$packageName.provider",
                file
            )
            intent.setDataAndType(apkURI, "application/pdf")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/pdf")
        }
        startActivity(intent)
    }

    private fun downloadFiles(fileList: List<String>) {
        val httpClient = OkHttpClient.Builder()
        val builder = Retrofit.Builder().baseUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/")
        val retrofit = builder.client(httpClient.build()).build()
        val downloadService: FileDownloadClient = retrofit.create(FileDownloadClient::class.java)
        fileList.forEach {
            val call: Call<ResponseBody?>? =
                downloadService.downloadFile(it)
            call?.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>?,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful) {
                        Log.d("FragmentActivity.TAG", "Got the body for the file")
                        val fileName = getFileName(it)
                        object : AsyncTask<Void?, Long?, Void?>() {
                            override fun doInBackground(vararg params: Void?): Void? {
                                response.body()?.let { saveToDisk(it, fileName) }
                                return null
                            }
                        }.execute()

                    } else {
                        Log.d(
                            "FragmentActivity.TAG",
                            "Connection failed " + response.errorBody()
                        )
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                    t.printStackTrace()
                    Log.e("FragmentActivity.TAG", t.message)
                }
            })
        }
    }

    private fun getFileName(it: String): String {
        return it.substring(it.lastIndexOf('/') + 1, it.length)
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

    fun saveToDisk(body: ResponseBody, filename: String) {
        val basePath =
            this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files"

        try {

            val directory = File(basePath)
            if (! directory.exists()){
                directory.mkdir();
            }
            val destinationFile =
                File(basePath + "/" + filename)
            var `is`: InputStream? = null
            var os: OutputStream? = null
            try {
                Log.d("FragmentActivity.TAG", "File Size=" + body.contentLength())
                `is` = body.byteStream()
                os = FileOutputStream(destinationFile)
                val data = ByteArray(4096)
                var count: Int = 0
                var progress = 0
//                while (`is`.read(data).also({ count = it }) != -1) {
//                    os.write(data, 0, count)
//                    progress += count
//                    Log.d(
//                       " FragmentActivity.TAG",
//                        "Progress: " + progress + "/" + body.contentLength() + " >>>> " + progress.toFloat() / body.contentLength()
//                    )
//                }
                val cipher: Cipher = Cipher.getInstance(TRANSFORMATION)
                encryptionIv = cipher.iv
                val spec = GCMParameterSpec(128, encryptionIv)
                cipher.init(Cipher.ENCRYPT_MODE, generateKeyStore("PUBLIC_KEY"),spec)
                val cos = CipherOutputStream(os, cipher)
                while (`is`.read(data).also { count = it } != -1) {
                    val encodedBytes: ByteArray = cipher.update(data)
                    cos.write(encodedBytes, 0, count)
                    progress += count
                    Log.d(
                       " FragmentActivity.TAG",
                        "Progress: " + progress + "/" + body.contentLength() + " >>>> " + progress.toFloat() / body.contentLength()
                    )
                }

                os.flush()
                Log.d("FragmentActivity.TAG", "File saved successfully!")
                return
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("FragmentActivity.TAG", "Failed to save the file!")
                return
            } finally {
                if (`is` != null) `is`.close()
                if (os != null) os.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("FragmentActivity.TAG", "Failed to save the file!")
            return
        }
    }


}
