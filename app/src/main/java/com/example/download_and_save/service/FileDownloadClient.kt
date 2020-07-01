package com.example.download_and_save.service

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url


public interface FileDownloadClient {
    @GET()
    fun downloadFile(@Url url :String): Call<ResponseBody?>?
}