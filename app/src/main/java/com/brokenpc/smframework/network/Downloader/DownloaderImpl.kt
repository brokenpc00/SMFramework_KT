package com.brokenpc.smframework.network.Downloader

class DownloaderImpl : IDownloaderImpl, AndroidDownloader.OnDownloadFinish, AndroidDownloader.OnDownloadProgress {

    protected var _id: Int = 0
    protected val _taskMap:HashMap<Int, DownloadTaskImpl> = HashMap()
    private var _impl:AndroidDownloader
    var _transferDataToBuffer:IDownloaderImpl.TransferDataToBuffer? = null

    companion object {
        var sDownloaderCounter:Int = 0
        val sDownloaderMap:HashMap<Int, DownloaderImpl> = HashMap()

        @JvmStatic
        fun _insertDownloader(id: Int, downloader: DownloaderImpl) {
            synchronized(sDownloaderMap) {
                sDownloaderMap.put(id, downloader)
            }
        }

        @JvmStatic
        fun _eraseDownloader(id: Int) {
            synchronized(sDownloaderMap) {
                sDownloaderMap.remove(id)
            }
        }

        @JvmStatic
        fun _findDownloader(id: Int): DownloaderImpl? {
            synchronized(sDownloaderMap) {
                return sDownloaderMap[id]
            }
        }
    }

    constructor(hints: DownloaderHints) {
        _id = ++sDownloaderCounter

        _impl = AndroidDownloader.createDownloader(_id, hints)
        _impl._onDownloadFinish = this
        _impl._onDownloadProgress = this

        _insertDownloader(_id, this)
    }

    fun cancelAllRequest() {
        AndroidDownloader.cancelAllRequest(_impl)
        _eraseDownloader(_id)
    }

    override fun createCoTask(task: DownloadTask): IDownloadTask? {
        val coTask = DownloadTaskImpl()
        coTask.task = task

        AndroidDownloader.createTask(_impl, coTask.id, task.requestURL, task.storagePath)

        _taskMap[coTask.id] = coTask

        return coTask
    }

    fun _onProgress(taskId: Int, dl: Long, dlNow: Long, dlTotal: Long) {
        val coTask = _taskMap[taskId] ?: return

        onTaskProgress?.onTaskProgress(coTask.task, dl, dlNow, dlTotal, _transferDataToBuffer)

    }

    fun _onFinish(taskId: Int, errCode: Int, errStr: String, data: ByteArray?) {
        val coTask = _taskMap[taskId] ?: return

        _taskMap.remove(taskId)

        onTaskFinish?.onTaskFinish(coTask.task, if (errStr.isNotEmpty()) DownloadTask.ERROR_IMPL_INTERNAL else DownloadTask.ERROR_NO_ERROR, errCode, errStr, data )
    }

    interface OnDownloadProgress {
        fun onDownloadProgress(id: Int, taskId: Int, dl: Long, dlNow: Long, dlTotal: Long)
    }
    var _onDownloadProgress:OnDownloadProgress? = null

    override fun onDownloadProgress(id: Int, taskId: Int, dl: Long, dlnow: Long, dltotal: Long) {
        val downloader = _findDownloader(id) ?: return

        downloader._onProgress(taskId, dl, dlnow, dltotal)

        _onDownloadProgress?.onDownloadProgress(id, taskId, dl, dlnow, dltotal)
    }

    interface OnDownloadFinish {
        fun onDownloadFinish(id: Int, taskId: Int, errCode: Int, errStr: String, data: ByteArray?)
    }
    var _onDownloadFinish:OnDownloadFinish? = null

    override fun onDownloadFinish(
        id: Int,
        taskId: Int,
        errCode: Int,
        errStr: String,
        data: ByteArray?
    ) {
        val downloader = _findDownloader(id) ?: return

        if (errStr.isNotEmpty()) {
            downloader._onFinish(taskId, errCode, errStr, data)
            return
        } else {
            downloader._onFinish(taskId, errCode, "", data)
        }

        _onDownloadFinish?.onDownloadFinish(id, taskId, errCode, errStr, data)
    }

}