package com.brokenpc.smframework.network.Downloader

import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestHandle

class AndroidDownloadTask {
    var bytesReceived:Long = 0L
    var totalBytesReceived:Long = 0L
    var totalBytesExpected:Long = 0L
    var data:ByteArray? = null

    var handle:RequestHandle? = null
    var handler:AsyncHttpResponseHandler? = null

    constructor() {
        handle = null
        handler = null
        resetStatus()
    }

    fun resetStatus() {
        bytesReceived = 0
        totalBytesReceived = 0
        totalBytesExpected = 0
        data = null
    }
}