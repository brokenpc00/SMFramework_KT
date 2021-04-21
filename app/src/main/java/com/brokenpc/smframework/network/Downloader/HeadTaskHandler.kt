package com.brokenpc.smframework.network.Downloader

import com.loopj.android.http.AsyncHttpResponseHandler
import cz.msebera.android.httpclient.Header

class HeadTaskHandler : AsyncHttpResponseHandler {
    var _id: Int = 0
    var _host: String = ""
    var _url: String = ""
    var _path: String= ""
    private var _downloader:AndroidDownloader

    fun LogD(msg: String) { android.util.Log.d("AndroidDownloader", msg) }

    constructor(downloader: AndroidDownloader, id: Int, host: String, url: String, path: String) : super() {
        _downloader = downloader
        _id = id
        _host = host
        _url = url
        _path = path
    }

    override fun onSuccess(statusCode: Int, headers: Array<out Header>, responseBody: ByteArray?) {
        var acceptRanges = false
        for (i in headers.indices) {
            val elm = headers[i]
            if (elm.name == "Accept-Ranges") {
                acceptRanges = elm.value=="bytes"
                break
            }
        }
        AndroidDownloader.setResumingSupport(_host, acceptRanges)
        AndroidDownloader.createTask(_downloader, _id, _url, _path)
    }

    override fun onFinish() {
        _downloader.runNextTaskIfExists()
    }

    override fun onFailure(
        statusCode: Int,
        headers: Array<out Header>?,
        responseBody: ByteArray?,
        error: Throwable?
    ) {
        var errStr = ""
        if (null!=error) {
            errStr = error.toString()
        }
        _downloader.onFinish(_id, 0, errStr, null)
    }
}