package com.brokenpc.smframework.network.Downloader

class DownloadTask() {
    companion object {
        const val ERROR_NO_ERROR = 0
        const val ERROR_INVALID_PARAMS = -1
        const val ERROR_FILE_OP_FAILED = -2
        const val ERROR_IMPL_INTERNAL = -3
    }

    var identifier: String  = ""
    var requestURL: String = ""
    var storagePath: String = ""

    var _coTask: IDownloadTask? = null
}