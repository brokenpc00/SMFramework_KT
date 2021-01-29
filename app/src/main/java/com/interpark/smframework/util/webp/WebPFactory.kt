package com.brokenpc.smframework.util.webp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.channels.FileChannel

class WebPFactory {

    init {
        System.loadLibrary("webpdecoder")
    }

    companion object {
        @JvmStatic
        fun getScaledBitmap(src: Bitmap?, width: Int, height: Int): Bitmap? {
            val bitmap: Bitmap?
            if (src != null && (src.width != width || src.height != height)) {
                bitmap = Bitmap.createScaledBitmap(src, width, height, true)
                src.recycle()
            } else {
                bitmap = src
            }
            return bitmap
        }

        @JvmStatic
        @Throws(IOException::class)
        private fun getByteArrayFromStream(IS: InputStream?): ByteArray? {
            if (IS == null) {
                return null
            }
            var dataBytes: ByteArray? = null
            if (IS is FileInputStream) {
                val fileChannel =
                    IS.channel
                val mappedByteBuffer = fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0L,
                    fileChannel.size()
                )
                dataBytes = ByteArray(mappedByteBuffer.remaining())
                mappedByteBuffer[dataBytes]
            } else {
                val os = ByteArrayOutputStream()
                var length: Int
                val buffer = ByteArray(2048)
                while (IS.read(buffer).also { length = it } >= 0) {
                    os.write(buffer, 0, length)
                }
                os.close()
                dataBytes = os.toByteArray()
            }
            return dataBytes
        }

        @JvmStatic
        fun decodeStreamScaled(
            IS: InputStream?,
            scaledWidth: Int,
            scaledHeight: Int
        ): Bitmap? {
            try {
                return decodeByteArrayScaled(getByteArrayFromStream(IS), scaledWidth, scaledHeight)
            } catch (e: IOException) {
                // Does nothing
            }
            return null
        }

        @JvmStatic
        fun decodeStream(IS: InputStream?): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                bitmap = decodeByteArray(getByteArrayFromStream(IS))
            } catch (e: IOException) {
                // Does nothing
            }
            return bitmap
        }

        @JvmStatic
        fun decodeByteArray(data: ByteArray?): Bitmap? {
            if (data == null) return null
            var bitmap: Bitmap? = null
            val dimen = IntArray(2)
            if (nativeDecodeBounds(data, dimen)) {
                if (dimen[0] > 0 && dimen[1] > 0) {
                    bitmap = Bitmap.createBitmap(dimen[0], dimen[1], Bitmap.Config.ARGB_8888)
                    if (!nativeDecodeIntoBitmap(data, bitmap)) {
                        bitmap.recycle()
                        bitmap = null
                    }
                }
            }
            if (bitmap == null) {
                // 실패하면 BitmapFactory로 읽어본다.
//            Log.i("WebPFactory", "[[[[[ data length : " + data.length);
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                //            try {
//
//            } catch (Exception e) {
////                Log.i("WebPFactory", "[[[[[ " + ;)
//                e.printStackTrace();
//            }
            }
            return bitmap
        }

        @JvmStatic
        fun decodeByteArrayScaled(
            data: ByteArray?,
            scaledWidth: Int,
            scaledHeight: Int
        ): Bitmap? {
            if (data == null || scaledWidth <= 0 || scaledHeight <= 0) return null
            var bitmap =
                Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
            if (!nativeDecodeIntoBitmap(data, bitmap)) {
                bitmap!!.recycle()
                bitmap = null
            }
            if (bitmap == null) {
                // 실패하면 BitmapFactory로 읽어본다.
                bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                bitmap = getScaledBitmap(bitmap, scaledWidth, scaledHeight)
            }
            return bitmap
        }

        @JvmStatic
        fun decodeFileScaled(
            pathName: String?,
            scaledWidth: Int,
            scaledHeight: Int
        ): Bitmap? {
            var IS: FileInputStream? = null
            var bitmap: Bitmap? = null
            try {
                IS = FileInputStream(pathName)
                return decodeStreamScaled(IS, scaledWidth, scaledHeight)
            } catch (e: Exception) {
                // Does nothing
            } finally {
                try {
                    IS?.close()
                } catch (e: IOException) {
                    // Does nothing
                }
            }
            bitmap = BitmapFactory.decodeFile(pathName)
            bitmap = getScaledBitmap(bitmap, scaledWidth, scaledHeight)
            return bitmap
        }

        @JvmStatic
        fun decodeFile(pathName: String?): Bitmap? {
            var IS: FileInputStream? = null
            var bitmap: Bitmap? = null
            try {
                IS = FileInputStream(pathName)
                bitmap = decodeStream(IS)
            } catch (e: Exception) {
                // Does nothing
            } finally {
                try {
                    IS?.close()
                } catch (e: IOException) {
                    // Does nothing
                }
            }
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeFile(pathName)
            }
            return bitmap
        }

        @JvmStatic
        fun decodeBounds(data: ByteArray?, size: IntArray?): Boolean {
            if (data != null && size != null && size.size >= 2) {
                if (nativeDecodeBounds(data, size)) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        private external fun nativeDecodeBuffer(data: ByteArray, dimen: IntArray): IntArray?

        @JvmStatic
        private external fun nativeDecodeBounds(
            data: ByteArray,
            dimen: IntArray
        ): Boolean

        @JvmStatic
        private external fun nativeDecodeIntoBitmap(
            data: ByteArray,
            bitmap: Bitmap?
        ): Boolean

    }


}