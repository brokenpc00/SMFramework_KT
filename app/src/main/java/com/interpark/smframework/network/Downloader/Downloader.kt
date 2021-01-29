package com.brokenpc.smframework.network.Downloader

class Downloader {

    lateinit var _impl:IDownloaderImpl

    constructor() {
        val hints = DownloaderHints()
        hints.countOfMaxProcessingTask = 6
        hints.timeoutInSeconds = 45
        hints.tempFileNameSuffix = ".tmp"
        set(hints)
    }

    constructor(hints: DownloaderHints) {
        set(hints)
    }

    fun set(hints: DownloaderHints) {
        _impl = DownloaderImpl(hints)

        _impl.onTaskProgress = object : IDownloaderImpl.OnTaskProgress {
            override fun onTaskProgress(
                task: DownloadTask?,
                byteReceived: Long,
                totalByteReceived: Long,
                totalBytesExpected: Long,
                transferDataToBuffer: IDownloaderImpl.TransferDataToBuffer?
            ) {
                _onTaskProgress?.onTaskProgress(task!!, byteReceived, totalByteReceived, totalBytesExpected)
            }
        }

        _impl.onTaskFinish = object : IDownloaderImpl.OnTaskFinish {
            override fun onTaskFinish(
                task: DownloadTask?,
                errorCode: Int,
                errorCodeInternal: Int,
                errorStr: String,
                data: ByteArray?
            ) {
                if (DownloadTask.ERROR_NO_ERROR != errorCode) {
                    _onTaskError?.onTaskError(task!!, errorCode, errorCodeInternal, errorStr)
                    return
                }

                if (task!!.storagePath.isNotEmpty()) {
                    _onFileTaskSuccess?.onFileTaskSuccess(task)
                } else {
                    _onDataTaskSuccess?.onDataTaskSuccess(task, data)
                }
            }
        }
    }

    interface OnDataTaskSuccess {
        fun onDataTaskSuccess(task: DownloadTask, data: ByteArray?)
    }
    interface OnFileTaskSuccess {
        fun onFileTaskSuccess(task: DownloadTask)
    }
    interface OnTaskProgress {
        fun onTaskProgress(task: DownloadTask, byteReceived: Long, totalByteReceived: Long, totalByteExpected: Long)
    }
    interface OnTaskError {
        fun onTaskError(task: DownloadTask, errorCode: Int, errorCodeInternal: Int, errorStr: String)
    }


    var _onDataTaskSuccess:OnDataTaskSuccess? = null
    var _onFileTaskSuccess:OnFileTaskSuccess? = null
    var _onTaskProgress:OnTaskProgress? = null
    var _onTaskError:OnTaskError? = null

    fun createDownloadDataTask(srcUrl: String): DownloadTask {
        return createDownloadDataTask(srcUrl, "")
    }

    fun createDownloadDataTask(srcUrl: String, identifier: String): DownloadTask {
        val task = DownloadTask()
        do {
            task.requestURL = srcUrl
            task.identifier = identifier
            if (srcUrl.isEmpty()) {
                _onTaskError?.onTaskError(task, DownloadTask.ERROR_INVALID_PARAMS, 0, "URL is Empty.")
                break
            }

            task._coTask = _impl.createCoTask(task)
        } while (false)

        return task
    }

    fun createDownloadFileTask(srcUrl: String, storagePath:String): DownloadTask {
        return createDownloadFileTask(srcUrl, storagePath, "")
    }

    fun createDownloadFileTask(srcUrl: String, storagePath: String, identifier: String): DownloadTask {
        val task = DownloadTask()
        do {
            task.requestURL = srcUrl
            task.storagePath = storagePath
            task.identifier = identifier

            if (srcUrl.isEmpty() || storagePath.isEmpty()) {
                _onTaskError?.onTaskError(task, DownloadTask.ERROR_INVALID_PARAMS, 0, "ULR or Storage Path is Empty");
                break
            }
            task._coTask = _impl.createCoTask(task)
        } while (false)

        return task
    }
}