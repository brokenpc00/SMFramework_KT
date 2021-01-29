package com.brokenpc.smframework.network.Downloader

import android.util.Log
import com.loopj.android.http.FileAsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header
import java.io.File

class FileTaskHandler : FileAsyncHttpResponseHandler {
    var _id: Int = 0
    var _finalFile: File? = null

    private var _initFileLen:Long
    private var _lastBytesWritten:Long
    private var _downloader: AndroidDownloader

    constructor(downloader: AndroidDownloader, id: Int, temp: File, finalFile: File) : super(temp, true) {
        _finalFile = finalFile
        _downloader = downloader
        _id = id
        _initFileLen = targetFile.length()
        _lastBytesWritten = 0L
    }

    fun LogD(msg: String) {
        android.util.Log.d("AndroidDownloader", msg)
    }

    override fun onProgress(bytesWritten: Long, totalSize: Long) {
        val dlBytes = bytesWritten - _lastBytesWritten
        val dlNow = bytesWritten + _initFileLen
        val dlTotal = totalSize + _initFileLen
        _downloader.onProgress(_id, dlBytes, dlNow, dlTotal)
        _lastBytesWritten = bytesWritten
    }

    override fun onStart() {
        _downloader.onStart(_id)
    }

    override fun onFinish() {
        _downloader.runNextTaskIfExists()
    }

    override fun onFailure(
        statusCode: Int,
        headers: Array<out Header>?,
        throwable: Throwable?,
        file: File?
    ) {

        LogD("onFailure(statusCode:$statusCode headers:$headers throwable: $throwable file:$file")

        var errStr = ""
        if (null!=throwable) {
            errStr = throwable.toString()
        }
        _downloader.onFinish(_id, statusCode, errStr, null)
    }

    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, file: File) {
        LogD("onSuccess(statusCode:$statusCode header:$headers file:$file")
        var errStr = ""
        do {
            if (_finalFile!!.exists()) {
                if (_finalFile!!.isDirectory) {
                    errStr = "Dest file is directory : " + _finalFile!!.absolutePath
                    break
                }
                if (!_finalFile!!.delete()) {
                    errStr = "Can't remove old file : " + _finalFile!!.absolutePath
                }
            }
            val tempFile = targetFile
            tempFile.renameTo(_finalFile)
        } while (false)
        _downloader.onFinish(_id, 0, errStr, null)
    }
}