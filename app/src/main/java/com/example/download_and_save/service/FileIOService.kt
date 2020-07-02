package com.example.download_and_save.service

import android.util.Log
import java.io.*

class FileIOService(val basePath: String) {
    private lateinit var fileStatusObserver: FileStatusObserver

    fun saveToDisk(byteStream: InputStream, fileName: String) {
        try {
            val directory = File(basePath)
            if (!directory.exists()) {
                directory.mkdir()
            }
            val filePath = File("$basePath/$fileName")

            var os: OutputStream? = null
            os = FileOutputStream(filePath)
            val data = ByteArray(4096)
            var count: Int = 0
            var progress = 0
            while (byteStream.read(data).also { count = it } != -1) {
                os.write(data, 0, count)
                progress += count
            }
            os.flush()
            os.close()
            Log.d("FragmentActivity.TAG", "File saved successfully!")
            return
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("FragmentActivity.TAG", "Failed to save the file!")
            return
        } finally {
            byteStream.close()
        }
    }

    fun read(fileName: String): ByteArray? {
        val filePath = File("$basePath/$fileName")
        try {
            if (filePath.exists() && filePath.canRead()) {
                val fis = FileInputStream(filePath)
                return fis.readBytes()
            } else {
                fileStatusObserver.fileWithNameNotFound(fileName)
            }
        } catch (ex: IOException) {
            Log.d(ex.localizedMessage, "")
        }
        return null
    }
}

interface FileStatusObserver {
    fun fileWithNameNotFound(fileName: String)
    fun fileWriteComplete(fileName: String)
}
