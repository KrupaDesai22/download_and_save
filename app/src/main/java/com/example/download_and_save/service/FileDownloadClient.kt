package com.example.download_and_save.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET


public interface FileDownloadClient {
    @GET("pdf/dummy.pdf")
    fun downloadFile(): Call<ResponseBody?>?
}