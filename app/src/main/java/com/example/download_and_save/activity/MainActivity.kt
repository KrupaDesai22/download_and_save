package com.example.download_and_save.activity

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.download_and_save.R
import com.example.download_and_save.presenter.MainPresenter
import com.example.download_and_save.service.EncryptDecrypt
import com.example.download_and_save.service.FileIOService
import com.example.download_and_save.service.SecretkeyGenerator
import com.example.download_and_save.util.NetworkManager
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var presenter: MainPresenter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var encryptDecrypt: EncryptDecrypt
    private lateinit var fileIOService: FileIOService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button=findViewById(R.id.button2)
        button.setOnClickListener(View.OnClickListener {
            showFile()
        })

        val basePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files"
        sharedPreferences = this.getSharedPreferences("DOWNLOAD", 0)
        fileIOService = FileIOService(basePath)
        encryptDecrypt= EncryptDecrypt(sharedPreferences,fileIOService, SecretkeyGenerator())

        presenter = MainPresenter(NetworkManager(), FileIOService(basePath),encryptDecrypt)

        val fileList = listOf<String>(
           "https://en.unesco.org/inclusivepolicylab/sites/default/files/dummy-pdf_2.pdf",
            "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
        )
       presenter.downloadFiles(fileList, basePath)
    }


    private fun showFile(){
        val fPath = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy.pdf"
        val decryptData = encryptDecrypt.decryptFromKeyStore("attachment", fPath)

        fileIOService.saveFileDecryptFile(decryptData,this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy_decrypt.pdf")
        val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + "/" + "files/dummy_decrypt.pdf")
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
