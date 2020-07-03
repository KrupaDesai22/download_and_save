package com.example.download_and_save.presenter

import com.example.download_and_save.service.EncryptDecrypt
import com.example.download_and_save.service.FileIOService
import com.example.download_and_save.service.FileStatusObserver
import com.example.download_and_save.util.NetworkManager
import com.example.download_and_save.util.ResultObserver
import java.io.File
import java.io.InputStream

class MainPresenter(val network: NetworkManager, val fileService: FileIOService, val encryptDecrypt: EncryptDecrypt): ResultObserver, FileStatusObserver {

    init {
        network.resultObserver = this
    }

    fun downloadFiles(
        list: List<String>,
        basePath: String
    ) {
        downloadFileIfNotExists(list, basePath)
    }

    private fun downloadFileIfNotExists(fileList: List<String>, basePath: String) {
        fileList.forEach {
            val file = File(basePath + "/" + getFileName(it))
            if (!file.exists())
                network.makeRequest(it)
        }
    }

    private fun getFileName(it: String): String {
        return it.substring(it.lastIndexOf('/') + 1, it.length)
    }

    override fun downloadCompleteWithResponse(istream: InputStream, fileName: String) {
        //encryptDecrypt.encryptFile(istream.readBytes(),"${fileService.basePath}/$fileName")
        encryptDecrypt.encryptInKeyStore("attachment", istream.readBytes(), "${fileService.basePath}/$fileName")
    }

    override fun downloadFailedWithError(error: String) {
        TODO("Not yet implemented")
    }

    override fun fileWithNameNotFound(fileName: String) {
        //Restart Download
        network.makeRequest(fileName)
    }

    override fun fileWriteComplete(fileName: String) {
        //Logg file write completed
    }
}