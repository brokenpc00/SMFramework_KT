package com.brokenpc.smframework.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import kotlin.math.max
import kotlin.math.roundToInt

class BitmapLoader() {
    companion object {
        const val Tag = "BitmapLoader"

        @JvmStatic
        fun loadBitmap(context: Context, path:String, degrees:Int, reqWidth:Int, reqHeight:Int): Bitmap? {
            return BitmapLoader().internalLoadBitmapExactly(context, path, degrees, reqWidth, reqHeight)
        }

        @JvmStatic
        fun resizedBitmap(src:Bitmap?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
            return BitmapLoader().internalResizeBitmap(src, degrees, reqWidth, reqHeight)
        }

        @JvmStatic
        fun loadBitmapRoughly(context: Context, path: String, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
            return BitmapLoader().internalLoadRoughly(context, path, degrees, reqWidth, reqHeight)
        }

        @JvmStatic
        fun calculateInSampleSize(width:Int, height:Int, reqWidth: Int, reqHeight: Int): Int {
            var inSampleSize = 1
            if (width>reqWidth || height>reqHeight) {
                if (width>height) {
                    inSampleSize = (height / reqHeight.toFloat()).roundToInt()
                } else {
                    inSampleSize = (width / reqWidth.toFloat()).roundToInt()
                }

                val totalPixels:Float = width * height.toFloat()
                val totalReqPixelsCap = reqWidth * reqHeight * 2

                while (totalPixels / (inSampleSize*inSampleSize) > totalReqPixelsCap) {
                    inSampleSize++
                }
            }

            return inSampleSize
        }
    }

    private fun internalLoadRoughly(context: Context, patht: String?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (patht==null) return null

        val path:String = patht

        var bitmap:Bitmap? = null
        var IS:InputStream? = null

        var outWidth:Int = 0
        var outHeight:Int = 0

        val uri:Uri = Uri.fromFile(File(path))

        val options = BitmapFactory.Options()

        try {
            IS = context.contentResolver.openInputStream(uri)
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(IS, null, options)
        } catch (e:FileNotFoundException) {
            return null
        } finally {
            if (IS!=null) {
                try {
                    IS.close()
                } catch (a:IOException) {

                }
                IS = null
            }
        }

        if (degrees==90 || degrees==270) {
            outWidth = options.outHeight
            outHeight = options.outWidth
        } else {
            outWidth = options.outWidth
            outHeight = options.outHeight
        }

        options.inSampleSize = calculateInSampleSize(outWidth, outHeight, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
//        options.inDither = true
        options.inPreferredConfig = Bitmap.Config.RGB_565

        var retry = 0
        while (bitmap==null && retry<5) {
            try {
                IS = context.contentResolver.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(IS, null, options)
                if (bitmap!=null) break
            } catch (e:OutOfMemoryError) {
                System.gc()
            } finally {
                if (IS!=null) {
                    try {
                        IS.close()
                    } catch (e:IOException) {

                    }
                    IS = null
                }
            }

            retry++
            options.inSampleSize++
        }

        if (bitmap!=null && degrees!=0) {
            val matrix = Matrix()
            var width = 0f
            var height = 0f
            if (degrees==90 || degrees==270) {
                width = bitmap.height.toFloat()
                height = bitmap.width.toFloat()
            } else {
                width = bitmap.width.toFloat()
                height = bitmap.height.toFloat()
            }

            var rotated:Bitmap? = null
            retry = 0
            var scale = 1f
            while (rotated==null && retry<3) {
                try {
                    rotated = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.RGB_565)
                    if (rotated!=null) {
                        matrix.reset()
                        matrix.postTranslate(-bitmap.width/2f, -bitmap.height/2f)
                        matrix.postRotate(degrees.toFloat())
                        matrix.postScale(scale, scale)
                        matrix.postTranslate(width/2f, height/2f)

                        val canvas = Canvas(rotated)
                        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                        paint.isDither = true
                        canvas.drawBitmap(bitmap, matrix, paint)
                    }
                } catch (e:OutOfMemoryError) {
                    System.gc()
                }

                retry++
                width /= 2f
                height /= 2f
                scale /= 2f
            }

            if (rotated!=null) {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                    bitmap = null
                }
                bitmap = rotated
            }
        }

        return bitmap
    }

    private fun internalResizeBitmap(src:Bitmap?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
//        if (src==null) return null
        var bitmap:Bitmap? = src ?: return null

        if (bitmap != null && (degrees!=0 || bitmap.width*bitmap.height > reqWidth*reqHeight)) {
            val matrix = Matrix()
            var width = 0f
            var height = 0f
            if (degrees==90 || degrees==270) {
                width = bitmap.height.toFloat()
                height = bitmap.width.toFloat()
            } else {
                width = bitmap.width.toFloat()
                height = bitmap.height.toFloat()
            }

            var rotated:Bitmap? = null
            var scale:Float = max(reqWidth/width, reqHeight/height)
            var retry = 0
            width = reqWidth.toFloat()
            height = reqHeight.toFloat()
            while (rotated==null && retry<3) {
                try {
                    rotated = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
                    if (rotated!=null) {
                        matrix.reset()
                        matrix.postTranslate(-bitmap.width/2f, -bitmap.height/2f)
                        matrix.postRotate(degrees.toFloat())
                        matrix.postScale(scale, scale)
                        matrix.postTranslate(width/2f, height/2f)

                        val canvas = Canvas(rotated)
                        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                        paint.isDither = true
                        canvas.drawBitmap(bitmap, matrix, paint)
                        break
                    }
                } catch (e:OutOfMemoryError) {
                    System.gc()
                }

                retry++
                width /= 2f
                height /= 2f
                scale /= 2f
            }

            if (rotated!=null && rotated!=bitmap) {
                if (bitmap != null && !bitmap.isRecycled) {
                    bitmap.recycle()
                    bitmap = null
                }
                bitmap = rotated
                rotated = null
            }
        }

        return bitmap
    }

    private fun internalLoadBitmapExactly(context: Context, pathName: String?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (pathName==null) return null

        var bitmap:Bitmap? = null
        var outWidth = 0
        var outHeight = 0

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)

        if (degrees==90 || degrees==270) {
            outWidth = options.outHeight
            outHeight = options.outWidth
        } else {
            outWidth = options.outWidth
            outHeight = options.outHeight
        }

        options.inJustDecodeBounds = false
        options.inSampleSize = max(1, max(outWidth/reqWidth, outHeight/reqHeight))
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        var retry = 0
        while (bitmap==null && retry<5) {
            try {
                bitmap = BitmapFactory.decodeFile(pathName, options)
                if (bitmap!=null) break
            } catch (e:OutOfMemoryError) {
                System.gc()
            }
            retry++
            options.inSampleSize++
        }

        if (bitmap!=null && (degrees!=0 || bitmap.width*bitmap.height > reqWidth*reqHeight)) {
            val matrix = Matrix()
            var width = 0f
            var height = 0f
            if (degrees==90 || degrees==270) {
                width = bitmap.height.toFloat()
                height = bitmap.width.toFloat()
            } else {
                width = bitmap.width.toFloat()
                height = bitmap.height.toFloat()
            }

            var rotated:Bitmap? = null
            var scaleX = reqWidth/width.toFloat()
            var scaleY = reqHeight/height.toFloat()
            var scale = max(scaleX, scaleY)
            retry = 0
            width = reqWidth.toFloat()
            height = reqHeight.toFloat()
            while (rotated==null && retry<3) {
                try {
                    rotated = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
                    if (rotated!=null) {
                        matrix.reset()
                        matrix.postTranslate(-bitmap.width/2f, -bitmap.height/2f)
                        matrix.postRotate(degrees.toFloat())
                        matrix.postScale(scale, scale)
                        matrix.postTranslate(width/2f, height/2f)

                        val canvas = Canvas(rotated)
                        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                        paint.isDither = true
                        canvas.drawBitmap(bitmap, matrix, paint)
                        break
                    }
                } catch (e:OutOfMemoryError) {
                    System.gc()
                }

                retry++
                width /= 2f
                height /= 2f
                scaleX /= 2f
                scaleY /= 2f
                scale = max(scaleX, scaleY)
            }

            if (rotated!=null) {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                    bitmap = null
                }
                bitmap = rotated
            }
        }

        return bitmap
    }
}