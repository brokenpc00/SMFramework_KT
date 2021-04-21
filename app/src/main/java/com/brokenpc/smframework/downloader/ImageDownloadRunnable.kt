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

class ImageDownloadRunnable(director:IDirector, imageTask: TaskRunnableDownloadMethods) : Runnable, Response.Listener<ByteArrayInputStream>, Response.ErrorListener, Ref(director) {
    private fun Any.wait() = (this as java.lang.Object).wait()
    private fun Any.notifyAll() = (this as java.lang.Object).notifyAll()

    companion object {
        const val READ_SIZE = 1024*2
        const val LOG_TAG = "ImageDownloadRunnable"

        const val HTTP_STATE_FAILED = -1
        const val HTTP_STATE_STARTED = 0
        const val HTTP_STATE_COMPLETED = 1
    }

    var _imageTask:TaskRunnableDownloadMethods
    var _context:Context
    var _byteInputStream:InputStream? = null
    var _request:NetworkStreamRequest? = null

    interface TaskRunnableDownloadMethods {
        fun setDownloadThread(currentThread: Thread?)
        fun getByteBuffer():ByteArray?
        fun setByteBuffer(buffer:ByteArray)
        fun handleDownloadState(state:Int)
        fun getMediaType():Int
        fun getImagePath():String
        fun getDiskCachePath():String
    }

    init {
        _context = director.getContext()
        _imageTask = imageTask
    }

    override fun run() {
        _imageTask.setDownloadThread(Thread.currentThread())
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)

        var byteBuffer = _imageTask.getByteBuffer()

        try {
            if (Thread.interrupted()) {
                throw InterruptedException()
            }

            if (byteBuffer==null) {
                if (_imageTask.getMediaType()==Constants.MEDIA_ASSETS) {
                    var IS:InputStream? = null
                    try {
                        IS = _context.assets.open(_imageTask.getImagePath())
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
                        _imageTask.setByteBuffer(byteBuffer)
                        _imageTask.handleDownloadState(HTTP_STATE_COMPLETED)
                    } else {
                        _imageTask.handleDownloadState(HTTP_STATE_FAILED)
                    }
                    return
                } else if (_imageTask.getMediaType()==Constants.MEDIA_SDCARD) {
                    try {
                        byteBuffer = IOUtils.readFile(_imageTask.getImagePath())
                    } catch (e:IOException) {
                        e.printStackTrace()
                    }
                    if (Thread.interrupted()) {
                        throw InterruptedException()
                    }
                    if (byteBuffer!=null && byteBuffer.isNotEmpty()) {
                        _imageTask.setByteBuffer(byteBuffer)
                        _imageTask.handleDownloadState(HTTP_STATE_COMPLETED)
                    } else {
                        _imageTask.handleDownloadState(HTTP_STATE_FAILED)
                    }
                    return
                } else {
                    // cache
                    if (_imageTask.getDiskCachePath()!=null) {
                        try {
                            byteBuffer = IOUtils.readFile(_imageTask.getDiskCachePath())
                        } catch (e:IOException) {
                            e.printStackTrace()
                        }
                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        if (byteBuffer!=null && byteBuffer.isNotEmpty()) {
                            _imageTask.setByteBuffer(byteBuffer)
                            _imageTask.handleDownloadState(HTTP_STATE_COMPLETED)
                            return
                        }
                    }
                }

                // not asset, not sdcard... and not disk cache... so network job!!!

                if (Thread.interrupted()) {
                    throw InterruptedException()
                }

                _imageTask.handleDownloadState(HTTP_STATE_STARTED)

                var byteStream:InputStream? = null
                _request = NetworkStreamRequest(_imageTask.getImagePath(), this, this)
                _director!!.getRequestQueue().add(_request)

                synchronized(this@ImageDownloadRunnable) {
                    wait()
                    byteStream = _byteInputStream
                }

                if (byteStream==null) {
                    throw InterruptedException()
                }

                if (Thread.interrupted()) {
                    throw InterruptedException()
                }


                try {
                    var tempBuffer = ByteArray(READ_SIZE)
                    var bufferLeft = tempBuffer.size

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
                        val newSize = tempBuffer.size + READ_SIZE
                        val expandBuffer = ByteArray(newSize)
                        System.arraycopy(tempBuffer, 0, expandBuffer, 0, tempBuffer.size)
                        tempBuffer = expandBuffer
                    } while (true)

                    byteBuffer = ByteArray(bufferOffset)

                    System.arraycopy(tempBuffer, 0, byteBuffer, 0, bufferOffset)

                    if (_imageTask.getDiskCachePath().isNotEmpty()) {
                        val tempBuffer = byteBuffer
                        Thread(Runnable {
                            try {
                                IOUtils.writeFile(tempBuffer, _imageTask.getDiskCachePath())
                            } catch (e:IOException) {
                                e.printStackTrace()
                            }
                        }).start()
                    }

                    if (Thread.interrupted()) {
                        throw InterruptedException()
                    }
                } catch (e:IOException) {
                    e.printStackTrace()
                    return
                } finally {
                    if (_request!=null) {
                        synchronized(this@ImageDownloadRunnable) {
                            _request?.cancel()
                            _request = null
                        }
                    }
                    if (byteStream!=null) {
                        try {
                            byteStream!!.close()
                        } catch (e:Exception) {

                        }
                    }
                }
            }

            _imageTask.setByteBuffer(byteBuffer!!)
            _imageTask.handleDownloadState(HTTP_STATE_COMPLETED)

        } catch (e:InterruptedException) {

        } finally {
            if (_request!=null) {
                synchronized(this@ImageDownloadRunnable) {
                    _request?.cancel()
                    _request = null
                }
            }

            if (byteBuffer==null) {
                _imageTask.handleDownloadState(HTTP_STATE_FAILED)
            }

            _imageTask.setDownloadThread(null)
            Thread.interrupted()
        }
    }

    override fun onErrorResponse(error: VolleyError?) {
        synchronized(this@ImageDownloadRunnable) {
            _request = null
            _byteInputStream = null
            notifyAll()
        }
    }

    override fun onResponse(response: ByteArrayInputStream?) {
        synchronized(this@ImageDownloadRunnable) {
            _request = null
            _byteInputStream = response
            notifyAll()
        }
    }
}