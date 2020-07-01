package com.example.download_and_save

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.webkit.WebView
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


class MainActivity : AppCompatActivity() {

    lateinit var button: Button

    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button=findViewById(R.id.button2)
        button.setOnClickListener(View.OnClickListener {
            showFile()
        })


//        webView = findViewById(R.id.webView)
//
        downloadZipFile()
//        webView.settings.allowFileAccess=true
//        webView.settings.allowContentAccess=true
//        webView.settings.allowFileAccessFromFileURLs=true
//        webView.settings.allowUniversalAccessFromFileURLs=true
//
//        val file =
//            File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy.pdf")
//
//        val uri = Uri.fromFile(file)
//        webView.loadUrl(uri.toString())
    }

    private fun showFile(){
        val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy.pdf")
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

    private fun downloadZipFile() {
        val httpClient = OkHttpClient.Builder()
        val builder = Retrofit.Builder().baseUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/")
        val retrofit = builder.client(httpClient.build()).build()
        val downloadService: FileDownloadClient =
            retrofit.create<FileDownloadClient>(FileDownloadClient::class.java)
        val call: Call<ResponseBody?>? =
            downloadService.downloadFile()
        if (call != null) {
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>?,
                    response: Response<ResponseBody?>
                ) {
                    if (response.isSuccessful()) {
                        Log.d("FragmentActivity.TAG", "Got the body for the file")
                        object : AsyncTask<Void?, Long?, Void?>() {
                            override fun doInBackground(vararg params: Void?): Void? {
                                response.body()?.let { saveToDisk(it) }
                                return null
                            }
                        }.execute()

                    } else {
                        Log.d("FragmentActivity.TAG", "Connection failed " + response.errorBody())
                    }
                }

                override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                    t.printStackTrace()
                    Log.e("FragmentActivity.TAG", t.message)
                }
            })
        }
    }

    fun saveToDisk(body: ResponseBody) {
        try {
            File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files").mkdir()
            val destinationFile =
                File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy.pdf")
            var `is`: InputStream? = null
            var os: OutputStream? = null
            try {
                Log.d("FragmentActivity.TAG", "File Size=" + body.contentLength())
                `is` = body.byteStream()
                os = FileOutputStream(destinationFile)
                val data = ByteArray(4096)
                var count: Int = 0
                var progress = 0
                while (`is`.read(data).also({ count = it }) != -1) {
                    os.write(data, 0, count)
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
