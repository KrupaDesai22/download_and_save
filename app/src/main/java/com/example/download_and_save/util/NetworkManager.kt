package com.example.download_and_save.util

import android.os.AsyncTask
import android.util.Log
import com.example.download_and_save.service.FileDownloadClient
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.InputStream

class NetworkManager() {
    lateinit var resultObserver: ResultObserver

    fun getHttpClient(): FileDownloadClient {
        val httpClient = OkHttpClient.Builder()
        val builder = Retrofit.Builder().baseUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/")
        val retrofit = builder.client(httpClient.build()).build()
        return retrofit.create(FileDownloadClient::class.java)
    }

    fun makeRequest(url: String) {
        val call: Call<ResponseBody?>? =
            getHttpClient().downloadFile(url)

        call?.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>?,
                response: Response<ResponseBody?>
            ) {
                if (response.isSuccessful) {
                    Log.d("FragmentActivity.TAG", "Got the body for the file")
                    val fileName = getFileName(url)
                    object : AsyncTask<Void?, Long?, Void?>() {
                        override fun doInBackground(vararg params: Void?): Void? {
                            response.body()?.let { responseBody ->
                                resultObserver
                                    .downloadCompleteWithResponse(
                                        responseBody.byteStream(),
                                        fileName
                                    )
//                                FileIOService(basePath, fileName)
//                                    .saveToDisk(responseBody.byteStream())
                            }
                            return null
                        }
                    }.execute()

                } else {
                    resultObserver
                        .downloadFailedWithError(
                            response
                                .errorBody()
                                .toString()
                        )
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

    private fun getFileName(it: String): String {
        return it.substring(it.lastIndexOf('/') + 1, it.length)
    }
}

interface ResultObserver {
    fun downloadCompleteWithResponse(istream: InputStream, fileName: String)
    fun downloadFailedWithError(error: String)
}
