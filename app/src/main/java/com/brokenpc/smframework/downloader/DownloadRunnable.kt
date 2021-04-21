package com.brokenpc.smframework.downloader

import android.content.Context
import com.android.volley.Response
import com.android.volley.VolleyError
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Ref
import com.brokenpc.smframework.util.IOUtils
import com.brokenpc.smframework.util.NetworkStreamRequest
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception

class DownloadRunnable : Ref, Runnable, Response.Listener<ByteArrayInputStream>, Response.ErrorListener {

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun Any.wait() = (this as java.lang.Object).wait()
    private fun Any.notifyAll() = (this as java.lang.Object).notifyAll()

    private val _context:Context
    private val _downloadTask:DownloadRunnableTaskMethods
    private var _byteIputStream:InputStream? = null
    private var _request:NetworkStreamRequest? = null

    enum class TYPE {DATA, IMAGE, EBOOK, MOVIE}

    companion object {
        const val HTTP_STATE_FAILED = -1
        const val HTTP_STATE_STARTED = 0
        const val HTTP_STATE_COMPLETED = 1
        const val READ_SIZE = 1024*2
        const val LOG_TAG = "DownloadRunnable"
    }

    interface DownloadRunnableTaskMethods {
        fun setDownloadThread(currentThread: Thread?)
        fun getByteBuffer():ByteArray?
        fun setByteBuffer(buffer: ByteArray)
        fun handleDownloadState(state: Int)

        fun getStorageMediaType():Int
        fun getDownloadPath():String
        fun getDiskCachePath():String
    }

    constructor(director:IDirector, downloadTask: DownloadRunnable.DownloadRunnableTaskMethods) : super(director) {
        _context = director.getContext()
        _downloadTask = downloadTask
    }

    override fun run() {
        _downloadTask.setDownloadThread(Thread.currentThread())

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)

        var byteBuffer = _downloadTask.getByteBuffer()

        try {
            if (Thread.interrupted()) {
                throw InterruptedException()
            }

            if (null == byteBuffer) {
                if (_downloadTask.getStorageMediaType()==Constants.MEDIA_ASSETS) {
                    // asset?

                    var IS:InputStream? = null
                    try {
                        IS = _context.assets.open(_downloadTask.getDownloadPath())
                        byteBuffer = IOUtils.toByteArray(IS)
                    } catch (e:IOException) {
                        e.printStackTrace()
                    } finally {
                        IOUtils.closeSilently(IS)
                    }

                    if (Thread.interrupted()) {
                        throw InterruptedException()
                    }

                    if (byteBuffer!=null && byteBuffer.isNotEmpty()) {
                        _downloadTask.setByteBuffer(byteBuffer)
                        _downloadTask.handleDownloadState(HTTP_STATE_COMPLETED)
                    } else {
                        _downloadTask.handleDownloadState(HTTP_STATE_FAILED)
                    }
                    return
                } else if (_downloadTask.getStorageMediaType()==Constants.MEDIA_SDCARD) {
                    // sdcard?

                    try {
                        byteBuffer = IOUtils.readFile(_downloadTask.getDownloadPath())
                    } catch (e:IOException) {
                        e.printStackTrace()
                    }

                    if (Thread.interrupted()) {
                        throw InterruptedException()
                    }

                    if (byteBuffer!=null && byteBuffer.isNotEmpty()) {
                        _downloadTask.setByteBuffer(byteBuffer)
                        _downloadTask.handleDownloadState(HTTP_STATE_COMPLETED)
                    } else {
                        _downloadTask.handleDownloadState(HTTP_STATE_FAILED)
                    }
                    return
                } else {
                    // read from cache

                    if (_downloadTask.getDiskCachePath().isNotEmpty()) {
                        try {
                            // disk cache.. read file
                            byteBuffer = IOUtils.readFile(_downloadTask.getDiskCachePath())
                        } catch (e:IOException) {
                            e.printStackTrace()
                        }

                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }

                        if (byteBuffer!=null && byteBuffer.isNotEmpty()) {
                            _downloadTask.setByteBuffer(byteBuffer)
                            _downloadTask.handleDownloadState(HTTP_STATE_COMPLETED)
                            return
                        }
                    }
                }

                // not asset and not sdcard and not diskcache... so.. network resource

                if (Thread.interrupted()) {
                    throw InterruptedException()
                }

                // start download for network
                _downloadTask.handleDownloadState(HTTP_STATE_STARTED)

                var byteStream:InputStream? = null

                _request = NetworkStreamRequest(_downloadTask.getDownloadPath(), this, this)
                _director!!.getRequestQueue().add(_request)

                synchronized(this@DownloadRunnable) {
                    wait()
                    byteStream = _byteIputStream
                }

                if (byteStream==null) {
                    // can not read
                    throw InterruptedException()
                }

                if (Thread.interrupted()) {
                    throw InterruptedException()
                }

                try {
                    var contentSize = -1

                    var tempBuffer = ByteArray(READ_SIZE)
                    var bufferLeft= tempBuffer.size
                    var bufferOffset = 0
                    var readResult = 0

                    Outer@ do {
                        while (bufferLeft>0) {
                            readResult = byteStream!!.read(tempBuffer, bufferOffset, bufferLeft)

                            if (readResult<0) {
                                break@Outer
                            }

                            bufferOffset += readResult

                            bufferLeft -= readResult

                            if (Thread.interrupted()) {
                                throw InterruptedException()
                            }
                        }

                        bufferLeft = READ_SIZE
                        val newSize = tempBuffer.size + bufferLeft

                        val expandBuffer = ByteArray(newSize)
                        System.arraycopy(tempBuffer, 0, expandBuffer, 0, tempBuffer.size)
                        tempBuffer = expandBuffer
                    } while (true)

                    byteBuffer = ByteArray(bufferOffset)

                    System.arraycopy(tempBuffer, 0, byteBuffer, 0, bufferOffset)

                    if (_downloadTask.getDiskCachePath().isNotEmpty()) {
                        val diskCacheBuffer = byteBuffer

                        Thread(Runnable {
                            try {
                                IOUtils.writeFile(diskCacheBuffer, _downloadTask.getDiskCachePath())
                            } catch (e:IOException) {
                                e.printStackTrace()
                            }
                        }).start()
                    }

                    if (Thread.interrupted()) {
                        throw InterruptedException()
                    }
                } catch (e:Exception) {
                    e.printStackTrace()
                    return
                } finally {
                    if (_request!=null) {
                        synchronized(this@DownloadRunnable) {
                            _request?.cancel()
                            _request = null
                        }
                    }
                    try {
                        byteStream?.close()
                    } catch (e:Exception) {

                    }
                }
            }

            _downloadTask.setByteBuffer(byteBuffer!!)
            _downloadTask.handleDownloadState(HTTP_STATE_COMPLETED)
        } catch (e:InterruptedException) {

        } finally {
            if (_request!=null) {
                synchronized(this@DownloadRunnable) {
                    _request?.cancel()
                    _request = null
                }
            }

            if (null == byteBuffer) {
                _downloadTask.handleDownloadState(HTTP_STATE_FAILED)
            }

            _downloadTask.setDownloadThread(null)

            Thread.interrupted()
        }
    }

    override fun onErrorResponse(error: VolleyError?) {
        synchronized(this@DownloadRunnable) {
            _request = null
            _byteIputStream = null
            notifyAll()
        }
    }

    override fun onResponse(response: ByteArrayInputStream?) {
        synchronized(this@DownloadRunnable) {
            _request = null
            _byteIputStream = response
            notifyAll()
        }

    }
}