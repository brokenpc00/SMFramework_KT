package com.brokenpc.smframework.network.Downloader

import com.brokenpc.smframework.ClassHelper
import com.loopj.android.http.AsyncHttpClient
import java.io.File
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import javax.net.ssl.SSLException
import kotlin.collections.HashMap

class AndroidDownloader {
    private var _id:Int = 0
    private val _httpClient:AsyncHttpClient = AsyncHttpClient()
    private var _tempFileNameSuffix = ""
    private var _countOfMaxProcessingTask:Int = 0
    private var _taskMap:HashMap<Int, AndroidDownloadTask> = HashMap()
    private var _taskQueue:Queue<Runnable> = LinkedList()
    private var _runningTaskCount:Int = 0

    companion object {
        var _resumingSupport:HashMap<String, Boolean> = HashMap()

        @JvmStatic
        fun setResumingSupport(host: String, support: Boolean) {
            AndroidDownloader._resumingSupport[host] = support
        }

        @JvmStatic
        fun createDownloader(id: Int, hints: DownloaderHints): AndroidDownloader {
            return createDownloader(id, hints.timeoutInSeconds, hints.tempFileNameSuffix, hints.countOfMaxProcessingTask)
        }

        @JvmStatic
        fun createDownloader(id: Int, timeoutInSecond: Int, tempFileNameSuffix: String, countOfMaxProcessTasks: Int): AndroidDownloader {
            val downloader = AndroidDownloader()
            downloader._id = id
            downloader._httpClient.setEnableRedirects(true)
            if (timeoutInSecond>0) {
                // milli second???
                downloader._httpClient.setTimeout(timeoutInSecond*1000)
            }
            AsyncHttpClient.allowRetryExceptionClass(SSLException::class.java)

            downloader._httpClient.setURLEncodingEnabled(false)
            downloader._tempFileNameSuffix = tempFileNameSuffix
            downloader._countOfMaxProcessingTask = countOfMaxProcessTasks
            return downloader
        }

        @JvmStatic
        fun createTask(downloader: AndroidDownloader, id_: Int, url_: String, path_: String) {
            val id = id_
            val url = url_
            var path = path_

            val taskRunnable = Runnable {
                val task = AndroidDownloadTask()
                if (path.isEmpty()) {
                    task.handler = DataTaskHandler(downloader, id)
                    task.handle = downloader._httpClient.get(ClassHelper.getActivity(), url, task.handler)
                }

                do {
                    if (path.isEmpty()) break

                    var domain: String = ""
                    try {
                        val uri = URI(url)
                        domain = uri.host
                    } catch (e: URISyntaxException) {
                        break
                    }

                    val host = if (domain.startsWith("www.")) domain.substring(4) else domain

                    var supportResuming = false
                    var requestHeader = true

                    if (_resumingSupport.containsKey(host)) {
                        supportResuming = _resumingSupport[host] ?: false
                        requestHeader = false
                    }

                    if (requestHeader) {
                        task.handler = HeadTaskHandler(downloader, id, host, url, path)
                        task.handle = downloader._httpClient.head(ClassHelper.getActivity(), url, null, null, task.handler)
                        break
                    }

                    val tempFile = File(path+downloader._tempFileNameSuffix)
                    if (tempFile.isDirectory) break

                    val parent = tempFile.parentFile
                    if (parent!=null && !parent.isDirectory && !parent.mkdirs()) break

                    val finalFile = File(path)
                    if (finalFile.isDirectory) break

                    task.handler = FileTaskHandler(downloader, id, tempFile, finalFile)

                } while (false)

                if (null==task.handle) {
                    val errStr = "Can't create DownloadTask for $url"
                    ClassHelper.runOnGLThread {
                        downloader._onDownloadFinish?.onDownloadFinish(downloader._id, id, 0, errStr, null)
                    }
                } else {
                    downloader._taskMap[id] = task
                }
            }
            downloader.enqueueTask(taskRunnable)
        }

        @JvmStatic
        fun cancelAllRequest(downloader: AndroidDownloader) {
            ClassHelper.getActivity()?.runOnUiThread {
                val iter = downloader._taskMap.entries.iterator()
                while (iter.hasNext()) {
                    val entry = iter.next() as Map.Entry<*, *>
                    val task = entry.value as AndroidDownloadTask
                    task.handle?.cancel(true)
                }
            }
        }
    }

    fun enqueueTask(taskRunnable: Runnable) {
        synchronized(_taskQueue) {
            if (_runningTaskCount < _countOfMaxProcessingTask) {
                ClassHelper.getActivity()?.runOnUiThread {
                    _runningTaskCount++
                }
            } else {
                _taskQueue.add(taskRunnable)
            }
        }
    }

    fun runNextTaskIfExists() {
        synchronized(_taskQueue) {
            val taskRunnable = this@AndroidDownloader._taskQueue.poll()
            if (taskRunnable!=null) {
                ClassHelper.getActivity()?.runOnUiThread(taskRunnable)
            } else {
                _runningTaskCount
            }
        }
    }


    interface OnDownloadProgress {
        fun onDownloadProgress(id:Int, taskId:Int, dl:Long, dlnow:Long, dltotal:Long)
    }
    var _onDownloadProgress:OnDownloadProgress? = null

    interface OnDownloadFinish {
        fun onDownloadFinish(id:Int, taskId:Int, errCode:Int, errStr:String, data: ByteArray?)
    }
    var _onDownloadFinish:OnDownloadFinish? = null

    fun onProgress(id:Int, downloadBytes: Long, downloadNow: Long, downloadTotal: Long) {
        val task:AndroidDownloadTask? = _taskMap[id]
        if (task!=null) {
            task.bytesReceived = downloadBytes
            task.totalBytesReceived = downloadNow
            task.totalBytesExpected = downloadTotal
        }
        ClassHelper.runOnGLThread { _onDownloadProgress?.onDownloadProgress(_id, id, downloadBytes, downloadNow, downloadTotal) }
    }

    fun onStart(id:Int) {
        val task:AndroidDownloadTask? = _taskMap[id]
        task?.resetStatus()
    }

    fun onFinish(id:Int, errCode: Int, errStr: String, data: ByteArray?) {
        val task: AndroidDownloadTask = _taskMap[id] ?: return

        _taskMap.remove(id)
        ClassHelper.runOnGLThread { _onDownloadFinish?.onDownloadFinish(_id, id, errCode, errStr, data) }
    }


}