package com.brokenpc.smframework.network.Downloader

import com.loopj.android.http.BinaryHttpResponseHandler
import cz.msebera.android.httpclient.Header


class DataTaskHandler : BinaryHttpResponseHandler {
    var _id = 0
    private var _downloader:AndroidDownloader
    private var _lastBytesWritten:Long

    fun LogD(msg:String) {
        android.util.Log.d("AndroidDownloader", msg)
    }

    constructor(downloader: AndroidDownloader, id:Int) : super(arrayOf<String>(".*")) {
        _downloader = downloader
        _id = id
        _lastBytesWritten = 0
    }

    override fun onProgress(bytesWritten: Long, totalSize: Long) {
        var dlBytes:Long = bytesWritten - _lastBytesWritten
        var dlNow:Long = bytesWritten
        var dlTotal = totalSize
        _downloader.onProgress(_id, dlBytes, dlNow, dlTotal)
        _lastBytesWritten = bytesWritten
    }

    override fun onStart() {
        _downloader.onStart(_id)
    }

    override fun onFailure(
        statusCode: Int,
        headers: Array<out Header>?,
        binaryData: ByteArray?,
        error: Throwable?
    ) {
        var errStr = ""
        if (null != error) {
            errStr = error.toString()
        }
        _downloader.onFinish(_id, statusCode, errStr, null)
    }

    override fun onSuccess(statusCode: Int, headers: Array<out Header>?, binaryData: ByteArray?) {
        _downloader.onFinish(_id, 0, "", binaryData)
    }

    override fun onFinish() {
        _downloader.runNextTaskIfExists()
    }
}