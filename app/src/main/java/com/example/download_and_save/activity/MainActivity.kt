package com.example.download_and_save.activity

import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.download_and_save.R
import com.example.download_and_save.presenter.MainPresenter
import com.example.download_and_save.service.FileDownloadClient
import com.example.download_and_save.service.FileIOService
import com.example.download_and_save.util.NetworkManager
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*


class MainActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var presenter: MainPresenter
//    private val basePath =
//        this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button=findViewById(R.id.button2)
        button.setOnClickListener(View.OnClickListener {
            showFile()
        })

        val basePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files"
        presenter = MainPresenter(NetworkManager(), FileIOService(basePath))


        val fileList = listOf<String>(
            "https://en.unesco.org/inclusivepolicylab/sites/default/files/dummy-pdf_2.pdf",
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
        )
        presenter.downloadFiles(fileList, basePath)
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
}
