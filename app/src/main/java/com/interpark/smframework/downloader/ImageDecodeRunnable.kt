package com.brokenpc.smframework.downloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.brokenpc.smframework.util.webp.WebPFactory

class ImageDecodeRunnable : Runnable {
    companion object {
        const val NUMBER_OF_DECODE_TRIES = 2
        const val SLEEP_TIME_MILLISECONDS = 250
        const val LOG_TAG = "ImageDecodeRunnable"

        const val DECODE_STATE_FAILED = -1
        const val DECODE_STATE_STARTED = 0
        const val DECODE_STATE_COMPLETED = 1
    }

    interface TaskRunnableDecodeMethods {
        fun setImageDecodeThread(currentThread:Thread?)
        fun getByteBuffer() : ByteArray?
        fun handelDecodeState(state:Int)
        fun getTargetWidth():Int
        fun getTargetHeight():Int
        fun getTargetDegrees():Int
        fun getMediaType():Int
        fun setImage(image:Bitmap?)
    }

    val _imageTask:TaskRunnableDecodeMethods

    constructor(downloadTask: TaskRunnableDecodeMethods) {
        _imageTask = downloadTask
    }

    override fun run() {
        _imageTask.setImageDecodeThread(Thread.currentThread())

        val imageBuffer = _imageTask.getByteBuffer()

        var returnBitmap:Bitmap? = null

        try {
            _imageTask.handelDecodeState(DECODE_STATE_STARTED)

            if (Thread.interrupted()) {
                return
            }
            Thread.sleep(1)

            for (i in 0 until NUMBER_OF_DECODE_TRIES) {
                try {
                    if (_imageTask.getMediaType()==Constants.MEDIA_NETWORK) {
                        returnBitmap = WebPFactory.decodeByteArray(imageBuffer)
                        break
                    } else {
                        returnBitmap = BitmapFactory.decodeByteArray(imageBuffer, 0, imageBuffer!!.size, null)
                        break
                    }
                } catch (e:Throwable) {
                    System.gc()

                    if (Thread.interrupted()) {
                        return
                    }

                    Thread.sleep(SLEEP_TIME_MILLISECONDS.toLong())
                }
            }
        } catch (e:InterruptedException) {

        } finally {
            if (null == returnBitmap) {
                _imageTask.handelDecodeState(DECODE_STATE_FAILED)
            } else {
                _imageTask.setImage(returnBitmap)
                _imageTask.handelDecodeState(DECODE_STATE_COMPLETED)
            }

            _imageTask.setImageDecodeThread(null)
            Thread.interrupted()
        }
    }
}