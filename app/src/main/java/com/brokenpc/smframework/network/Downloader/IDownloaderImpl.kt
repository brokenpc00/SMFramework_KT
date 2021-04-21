package com.brokenpc.smframework.network.Downloader

abstract class IDownloaderImpl {
    interface TransferDataToBuffer {
        fun transferDataToBuffer(buffer: ByteArray?, len:Int)
    }
    interface OnTaskProgress {
        fun onTaskProgress(task: DownloadTask?, byteReceived: Long, totalByteReceived: Long, totalBytesExpected: Long, transferDataToBuffer: TransferDataToBuffer?)
    }
    interface OnTaskFinish {
        fun onTaskFinish(task: DownloadTask?, errorCode: Int, errorCodeInternal: Int, errorStr: String, data:ByteArray?)
    }

    var onTaskProgress:OnTaskProgress? = null
    var onTaskFinish: OnTaskFinish? = null
    open fun createCoTask(task: DownloadTask):IDownloadTask? {return null}
}