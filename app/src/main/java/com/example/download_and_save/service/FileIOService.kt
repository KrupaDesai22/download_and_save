package com.example.download_and_save.service

import android.util.Log
import java.io.*

class FileIOService(val basePath: String) {
    private lateinit var fileStatusObserver: FileStatusObserver

    @Throws(Exception::class)
    fun readFile(filePath: String): ByteArray {
        val file = File(filePath)
        val fileContents = file.readBytes()
        val inputBuffer = BufferedInputStream(
            FileInputStream(file)
        )
        inputBuffer.read(fileContents)
        inputBuffer.close()

        return fileContents
    }

    @Throws(Exception::class)
    fun saveFile(fileData: ByteArray, filePath: String) {
        val directory = File(basePath)
        if (!directory.exists()) {
            directory.mkdir()
        }

        var os: OutputStream? = null
        os = FileOutputStream(filePath)
        val data = ByteArray(4096)
        var count: Int = 0
        var progress = 0
        val byteStream = fileData.inputStream()
        while (byteStream.read(data).also { count = it } != -1) {
            os.write(data, 0, count)
            progress += count
        }
        os.flush()
        os.close()
    }

    @Throws(Exception::class)
    fun saveFileDecryptFile(fileData: ByteArray, filePath: String) {
        val directory = File(basePath)
        if (!directory.exists()) {
            directory.mkdir()
        }

        var os: OutputStream? = null
        os = FileOutputStream(filePath)
        val data = ByteArray(4096)
        var count: Int = 0
        var progress = 0
        val byteStream = fileData.inputStream()
        while (byteStream.read(data).also { count = it } != -1) {
            os.write(data, 0, count)
            progress += count
        }
        os.flush()
        os.close()
    }
}

interface FileStatusObserver {
    fun fileWithNameNotFound(fileName: String)
    fun fileWriteComplete(fileName: String)
}
