package com.brokenpc.smframework.network.Downloader

class DownloadTaskImpl : IDownloadTask {
    companion object {
        var sTaskCounter: Int = 0
    }

    var id: Int = 0
    var task: DownloadTask? = null

    constructor() {
        id = ++sTaskCounter
    }
}