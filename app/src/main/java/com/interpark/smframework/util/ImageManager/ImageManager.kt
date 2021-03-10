package com.interpark.smframework.util.ImageManager

import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log
import kotlin.math.max
import kotlin.math.roundToInt

class ImageManager() {
    companion object {
        @Volatile
        private var sScaleMatrix: Matrix? = null

        @JvmStatic
        fun getPhoneAlbumInfo(context: Context, listener: OnImageLoadListener) {
            val items = Vector<PhoneAlbum>()
            val PROJECTION_BUCKET = arrayOf(
                MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.ORIENTATION
            )
            val BUCKET_GROUP_BY = "1) GROUP BY 1,(2"
            val BUCKET_ORDER_BY = "MAX(${MediaStore.Images.ImageColumns.DATE_ADDED}) DESC"

            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_BUCKET,
                BUCKET_GROUP_BY,
                null,
                BUCKET_ORDER_BY
            )
            if (cursor!=null) {
                if (cursor.moveToFirst()) {
                    var bucketId: Long = -1
                    var id: Int = -1
                    var bucketName: String = ""
                    var data: String = ""
                    var orientation: Int = 0

                    val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                    val bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)
                    val bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                    val orientationColumn = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION)
                    var index = 0
                    val columnIndexID = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

                    do {
                        id = cursor.getInt(idColumn)
                        val imageId = cursor.getLong(columnIndexID)
                        bucketId = cursor.getLong(bucketIdColumn)
                        bucketName = cursor.getString(bucketColumn)
                        val uriImage = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "" + imageId
                        )
                        orientation = cursor.getInt(orientationColumn)

                        val album = PhoneAlbum()
                        album.setId(id)
                        album.setBucketId(bucketId)
                        album.setName(bucketName)
                        album.setAlbumIndex(index)
                        album.setCoverUri(uriImage.toString())
                        items.add(album)
                        index++
                    } while (cursor.moveToNext())
                }

                var totalCount = 0
                val numItems = items.size
                for (i in numItems-1 downTo 0) {
                    val item = items[i]
                    val count = countInBucket(context, item.getBucketId())
                    if (count>0) {
                        item.setPhotoCount(count)
                        totalCount += count
                    } else {
                        items.removeAt(i)
                    }
                }

                if (items.size==0) {
                    listener.onError()
                    return
                }

                val firstAlbum = items.firstElement()
                val totalCoverUrl = firstAlbum.getCoverUri()


                // all photo (like ios camera roll)
                val allPhotoAlbum = PhoneAlbum()
                allPhotoAlbum.setId(0)
                allPhotoAlbum.setBucketId(0)
                allPhotoAlbum.setCoverUri(totalCoverUrl)
                allPhotoAlbum.setName("ALL PHOTOS")
                allPhotoAlbum.setAlbumIndex(-99)
                allPhotoAlbum.setPhotoCount(totalCount)
                items.add(0, allPhotoAlbum)
                cursor.close()
            }

            if (items.size>0) {
                val albums = ArrayList<PhoneAlbum>()
                Collections.copy(albums, items)
                items.clear()
                listener.onAlbumImageLoadComplete(albums)
            } else {
                listener.onError()
            }
        }

        @JvmStatic
        fun countInBucket(context: Context, bucketId: Long): Int {
            val PROJECTION_COUNT = arrayOf("count(${MediaStore.Images.Media._ID})")
            val WHERE_CLAUSE = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            val WHERE_ARG = arrayOf(bucketId.toString())

            val cursor:Cursor? = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_COUNT,
                WHERE_CLAUSE,
                WHERE_ARG,
                null
            )
            var countInBucket = 0
            if (cursor!=null) {
                if (cursor.moveToFirst()) {
                    countInBucket = cursor.getInt(0)
                }
                cursor.close()
            }

            return countInBucket
        }

        @JvmStatic
        fun getPhotosInfo(context: Context, album: PhoneAlbum): PhoneAlbum {
            val PROJECTION_BUCKET = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.DATE_ADDED
            )

            val ORDER_BY = MediaStore.Images.Media.DATE_ADDED
            var WHERE_CLAUSE: String? = null
            var WHERE_ARG: Array<String>? = null

            val bucketId = album.getBucketId()

            if (bucketId!=0L && album.getAlbumIndex() >= 0) {
                WHERE_CLAUSE = "${MediaStore.Images.Media.BUCKET_ID} = ?"
                WHERE_ARG = arrayOf(bucketId.toString())
            }

            val cur = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION_BUCKET,
                WHERE_CLAUSE,
                WHERE_ARG,
                "$ORDER_BY DESC"
            )

            if (cur!=null && cur.count>0) {
                if (cur.moveToFirst()) {

                    var id: Int = -1
                    var orientation: Int = 0
                    var imageUriColumn = cur.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val imageIdColumn = cur.getColumnIndex(MediaStore.Images.Media._ID)
                    val orientationIdColumn = cur.getColumnIndex(MediaStore.Images.Media.ORIENTATION)

                    do {
                        id = cur.getInt(imageIdColumn)
                        val imageId = cur.getLong(imageUriColumn)
                        val uriImage = Uri.withAppendedPath(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            "" + imageId
                        )
                        orientation = cur.getInt(orientationIdColumn)

                        val photo = PhonePhoto()
                        photo.setAlbumName(album.getName())
                        photo.setPhotoUri(uriImage.toString())
                        photo.setId(id)
                        photo.setOrientation(orientation)
                        photo.setPhotoIndex(album.getAlbumPhotos().size)
                        album.getAlbumPhotos().add(photo)
                    } while (cur.moveToNext())
                }
            }
            return album
        }

        @JvmStatic
        fun getThumbnailPath(context: Context, imageId: Long): String {
            if (Build.VERSION.SDK_INT <= 29) {
                var cursor: Cursor? = null
                var filePath: String = ""
                try {
                    cursor = MediaStore.Images.Thumbnails.queryMiniThumbnail(context.contentResolver, imageId, MediaStore.Images.Thumbnails.MINI_KIND, null)
                    if (cursor!=null && cursor.moveToFirst()) {
                        filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    if (cursor!=null) {
                        cursor.close()
                        cursor = null
                    }
                }

                return filePath
            } else {
                return ""
            }
        }

        @JvmStatic
        fun loadBitmap(context: Context, path: String, degrees: Int, reqWidth: Int, reqHeight: Int) {

        }

        @JvmStatic
        fun loadBitresizeBitmap(src: Bitmap, degrees: Int, reqWidth: Int, reqHeight: Int) {

        }

        @JvmStatic
        fun loadBitmapRoughly(
            context: Context,
            path: String,
            degrees: Int,
            reqWidth: Int,
            reqHeight: Int
        ) {

        }

        @JvmStatic
        fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
            var inSampleSize = 1

            if (width>reqWidth || height>reqHeight) {
                if (width>height) {
                    inSampleSize = (height.toFloat() / reqHeight.toFloat()).roundToInt()
                } else {
                    inSampleSize = (width.toFloat() / reqWidth.toFloat()).roundToInt()
                }

                val totalPixels: Float = width.toFloat()*height.toFloat()
                val totalReqPixelsCap: Float = reqWidth.toFloat() * reqHeight.toFloat() * 2.0f

                while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                    inSampleSize++
                }
            }
            return inSampleSize
        }

        @JvmStatic
        fun loadBitmapResize(pathName: String?, degrees: Int, maxSize: Int): Bitmap? {
            if (pathName==null) return null

            var bitmap: Bitmap? = null

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(pathName, options)

            var outWidth = 0
            var outHeight = 0
            if (degrees==90 || degrees==270) {
                outWidth = options.outHeight
                outHeight = options.outWidth
            } else {
                outWidth = options.outWidth
                outHeight = options.outHeight
            }

            var bNeedResize = false
            var ratio = 1.0f

            if (outWidth>maxSize || outHeight>maxSize) {
                val widthRatio = (maxSize/outWidth.toFloat())
                val heightRatio = (maxSize/outHeight.toFloat())
                ratio = widthRatio.coerceAtMost(heightRatio)
            }

            val newWidth = (outWidth*ratio).toInt()
            val newHeight = (outHeight*ratio).toInt()

            Log.i(
                "IMAGE MANAGER",
                "[[[[[ out width : $outWidth, height : $outHeight, new width : $newWidth, height : $newHeight"
            )

            // load bitmap
            options.inJustDecodeBounds = false
            options.inSampleSize = (maxSize/outWidth).coerceAtLeast(maxSize / outHeight).coerceAtLeast(
                1
            )
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            var retry = 0
            while (bitmap==null && retry<5) {
                try {
                    bitmap = BitmapFactory.decodeFile(pathName, options)
                    if (bitmap!=null)
                        break
                } catch (e: OutOfMemoryError) {
                    System.gc()
                }

                retry++
                options.inSampleSize++
            }

            Log.i(
                "IMAGE MANAGER",
                "[[[[[ create bitmap : origin ($outWidth, $outHeight), new ($newWidth, $newHeight)"
            )

            if (bitmap!=null && (degrees!=0 || bitmap.width*bitmap.height > newWidth*newHeight)) {
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

                var rotated: Bitmap? = null
                var scaleX = newWidth/width
                var scaleY = newHeight/height

                var scale = scaleX.coerceAtLeast(scaleY)

                retry = 0
                width = newWidth.toFloat()
                height = newHeight.toFloat()

                while (rotated==null && retry<2) {
                    try {
                        rotated = Bitmap.createBitmap(
                            width.toInt(),
                            height.toInt(),
                            Bitmap.Config.ARGB_8888
                        )
                        if (rotated!=null) {
                            matrix.reset()
                            matrix.postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
                            matrix.postRotate(degrees.toFloat())
                            matrix.postScale(scale, scale)
                            matrix.postTranslate(width / 2f, height / 2f)

                            val canvas = Canvas(rotated)
                            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                            paint.isDither = true
                            canvas.drawBitmap(bitmap, matrix, paint)

                            break
                        }
                    } catch (e: OutOfMemoryError) {
                        System.gc()
                    }

                    retry++
                    width /= 2f
                    height /= 2f
                    scaleX /= 2f
                    scaleY /= 2f
                    scale = scaleX.coerceAtLeast(scaleY)
                }

                if (rotated!=null) {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                        bitmap = null
                    }

                    bitmap = rotated
                }
            }


            Log.i("IMAGE MANAGER", "[[[[[ load image from file on JNI Complete!!!!")
            return bitmap
        }

        @JvmStatic
        fun createScaledBitmap(src: Bitmap, degrees: Int, dstWidth: Int, dstHeight: Int, filter: Boolean): Bitmap {
            var m:Matrix? = null

            synchronized(Bitmap::class.java) {
                m = sScaleMatrix
                sScaleMatrix = null
            }

            if (m==null) {
                m = Matrix()
            }

            val width = src.width
            val height = src.height
            val sx = dstWidth / width.toFloat()
            val sy = dstHeight / height.toFloat()
            m!!.setScale(sx, sy)
            m!!.setRotate(degrees.toFloat())

//            Log.i("IMAGE MANAGER", "[[[[[ create resized bitmap 1")

            val b = Bitmap.createBitmap(src, 0, 0, width, height, m, filter)
            synchronized(Bitmap::class.java) {
                if (sScaleMatrix==null) {
                    sScaleMatrix = m
                }
            }

            return b
        }

        @JvmStatic
        fun loadBitmapMaxSize(pathName: String?, degrees: Int, maxSize: Int): Bitmap? {
            if (pathName==null) return null

            var bitmap:Bitmap? = null

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(pathName, options)

            val outWidth:Int
            val outHeight:Int
            if (degrees==90 || degrees==270) {
                outWidth = options.outHeight
                outHeight = options.outWidth
            } else {
                outWidth = options.outWidth
                outHeight = options.outHeight
            }

            var scaleSize = 0
            val widthRatio = outWidth / maxSize.toFloat()
            val heightRatio = outHeight / maxSize.toFloat()

            Log.i("BITMAPMAXSIZE", "[[[[[ widthRatio : $widthRatio, heightRatio : $heightRatio, MAXSIZE : $maxSize")
            var ratio = widthRatio.coerceAtMost(heightRatio)
            if (ratio>1.0f) {
                ratio = 1.0f
            }

            val newWidth = (outWidth*ratio).toInt()
            val newHeight = (outHeight*ratio).toInt()

            options.inJustDecodeBounds = false
            options.inSampleSize = (outWidth/maxSize).coerceAtLeast(outHeight/maxSize).coerceAtLeast(1)
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            var retry = 0
            while (bitmap==null && retry<5) {
                try {
                    bitmap = BitmapFactory.decodeFile(pathName, options)
                    if (bitmap!=null) {
                        break
                    }
                } catch (e: OutOfMemoryError) {
                    System.gc()
                }

                retry++
                options.inSampleSize++
            }

            if (bitmap!=null && (degrees!=0 || bitmap.width*bitmap.height > newWidth*newHeight)) {
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

                var rotated: Bitmap? = null
                var scaleX = newWidth/width
                var scaleY = newHeight/height
                var scale = scaleX.coerceAtLeast(scaleY)
                retry = 0
                width = newWidth.toFloat()
                height = newHeight.toFloat()
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
                    } catch (e: OutOfMemoryError) {
                        System.gc()
                    }

                    retry++
                    width /= 2f
                    height /= 2f
                    scaleX /= 2f
                    scaleY /= 2f
                    scale = scaleX.coerceAtLeast(scaleY)
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

        @JvmStatic
        fun internalLoadBitmapExactly(context: Context, pathName: String?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
            if (pathName==null) return null

            var bitmap: Bitmap? = null

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(pathName, options)

            val outWidth:Int
            val outHeight:Int

            if (degrees==90 || degrees==270) {
                outWidth = options.outHeight
                outHeight = options.outWidth
            } else {
                outWidth = options.outWidth
                outHeight = options.outHeight
            }

            // load bitmap
            options.inJustDecodeBounds = false
            options.inSampleSize = (outWidth/reqWidth).coerceAtLeast(outHeight/reqHeight).coerceAtLeast(1)
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            var retry = 0
            while (bitmap==null && retry<5) {
                try {
                    bitmap = BitmapFactory.decodeFile(pathName, options)
                    if (bitmap!=null) {
                        break
                    }
                } catch (e: OutOfMemoryError) {
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

                var rotated: Bitmap? = null
                var scaleX = reqWidth/width
                var scaleY = reqHeight/height
                var scale = scaleX.coerceAtLeast(scaleY)
                retry = 0
                width = reqWidth.toFloat()
                height = reqHeight.toFloat()
                while (rotated==null && retry<3) {
                    try {
                        rotated = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888 )
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
                    } catch (e: OutOfMemoryError) {
                        System.gc()
                    }

                    retry++
                    width /= 2f
                    height /= 2f
                    scaleX /= 2f
                    scaleY /= 2f
                    scale = scaleX.coerceAtLeast(scaleY)
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

        @JvmStatic
        fun extractThumbnailFromFile(pathName: String?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
            if (pathName==null) return null

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(pathName, options)

            val outWidth:Int
            val outHeight:Int
            if (degrees==90 || degrees==270) {
                outWidth = reqHeight
                outHeight = reqWidth
            } else {
                outWidth = reqWidth
                outHeight = reqHeight
            }

            if (outWidth <=0 || outHeight <=0) return null

            // load bitmap
            options.inJustDecodeBounds = false
            options.inSampleSize = (outWidth/reqWidth).coerceAtLeast(outHeight/reqHeight).coerceAtLeast(1)
            options.inPreferredConfig = Bitmap.Config.RGB_565

            var retry = 0
            var bitmap: Bitmap? = null

            while (bitmap==null && retry<5) {
                try {
                    bitmap = BitmapFactory.decodeFile(pathName, options)
                    if (bitmap!=null) break
                } catch (e: OutOfMemoryError) {
                    System.gc()
                }

                retry++
                options.inSampleSize++
            }

            if (bitmap!=null && (degrees!=0 || bitmap.width*bitmap.height != reqWidth*reqHeight)) {
                var width = 0f
                var height = 0f
                if (degrees==90 ||  degrees==270) {
                    width = bitmap.height.toFloat()
                    height = bitmap.width.toFloat()
                } else {
                    width = bitmap.width.toFloat()
                    height = bitmap.height.toFloat()
                }

                var scaleX = reqWidth/width
                var scaleY = reqHeight/height
                var scale = scaleX.coerceAtLeast(scaleY)

                retry = 0
                width = reqWidth.toFloat()
                height = reqHeight.toFloat()

                var extract:Bitmap? = null
                while (extract==null && retry<3) {
                    try {
                        extract = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.RGB_565)
                        if (extract!=null) {
                            val matrix = Matrix()
                            matrix.postTranslate(-bitmap.width/2f, -bitmap.height/2f)
                            matrix.postRotate(degrees.toFloat())
                            matrix.postScale(scale, scale)
                            matrix.postTranslate(width/2f, height/2f)

                            val canvas = Canvas(extract)
                            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                            canvas.drawBitmap(bitmap, matrix, paint)
                            break
                        }
                    } catch (e: OutOfMemoryError) {
                        System.gc()
                    }

                    retry++
                    width /= 2f
                    height /= 2f
                    scaleX /= 2f
                    scaleY /= 2f
                    scale = scaleX.coerceAtLeast(scaleY)
                }

                if (extract!=null) {
                    if (!bitmap.isRecycled) {
                        bitmap.recycle()
                        bitmap = null
                    }
                    bitmap = extract
                }
            }

            return bitmap
        }
    }

    interface OnImageLoadListener {
        open fun onAlbumImageLoadComplete(albums: ArrayList<PhoneAlbum>)
        open fun onError()
    }

    fun internalLoadRoughly(context: Context, path: String?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (path==null) return null

        var bitmap: Bitmap? = null
        var IS: InputStream? = null

        var outWidth = 0
        var outHeight = 0

        val uri: Uri? = Uri.fromFile(File(path))

        if (uri==null) return null

        val options = BitmapFactory.Options()
        try {
            IS = context.contentResolver.openInputStream(uri)
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(IS, null, options)
        } catch (e: FileNotFoundException) {
            return null
        } finally {
            if (IS != null) {
                try {
                    IS.close()
                } catch (e: IOException) {

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

        // load bitmap
        options.inSampleSize = calculateInSampleSize(outWidth, outHeight, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565

        var retry = 0
        while (bitmap==null && retry<5) {
            try {
                IS = context.contentResolver.openInputStream(uri)
                bitmap = BitmapFactory.decodeStream(IS, null, options)
                if (bitmap!=null) {
                    break
                }
            } catch (e: OutOfMemoryError) {
                System.gc()
            } catch (e: FileNotFoundException) {
                break
            } finally {
                if (IS!=null) {
                    try {
                        IS.close()
                    } catch (e: IOException) {

                    }
                    IS = null
                }
            }
            retry++
            options.inSampleSize++
        }

        if (bitmap!=null && degrees!=0) {
            val matrix = Matrix()
            var width = 0.0f
            var height = 0.0f

            if (degrees==90 || degrees==270) {
                width = bitmap.height.toFloat()
                height = bitmap.width.toFloat()
            } else {
                width = bitmap.width.toFloat()
                height = bitmap.height.toFloat()
            }

            var rotated: Bitmap? = null
            retry = 0
            var scale = 1.0f
            while (rotated==null && retry<3) {
                try {
                    rotated = Bitmap.createBitmap(
                        width.toInt(),
                        height.toInt(),
                        Bitmap.Config.RGB_565
                    )
                    if (rotated!=null) {
                        matrix.reset()
                        matrix.postTranslate(-bitmap.width / 2.0f, -bitmap.height / 2.0f)
                        matrix.postRotate(degrees.toFloat())
                        matrix.postScale(scale, scale)
                        matrix.postTranslate(width / 2.0f, height / 2.0f)

                        val canvas = Canvas(rotated)
                        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                        canvas.drawBitmap(bitmap, matrix, paint)
                        break
                    }
                } catch (e: OutOfMemoryError) {
                    System.gc()
                }

                retry++
                width /= 2
                height /= 2
                scale /= 2
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

    fun internalResizeBitmap(bmp: Bitmap?, degrees: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (bmp==null) return null
        var bitmap:Bitmap? = bmp
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

            var rotated: Bitmap? = null
            var scale = (reqWidth / width).coerceAtLeast(reqHeight / height)
            var retry = 0
            width = reqWidth.toFloat()
            height = reqHeight.toFloat()

            while (rotated==null && retry<3) {
                try {
                    rotated = Bitmap.createBitmap(
                        width.toInt(),
                        height.toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    if (rotated!=null) {
                        matrix.reset()
                        matrix.postTranslate(-bitmap.width / 2f, -bitmap.height / 2f)
                        matrix.postRotate(degrees.toFloat())
                        matrix.postScale(scale, scale)
                        matrix.postTranslate(width / 2f, height / 2f)

                        val canvas = Canvas(rotated)
                        val paint = Paint(Paint.FILTER_BITMAP_FLAG)
                        paint.isDither = true
                        canvas.drawBitmap(bitmap, matrix, paint)
                        break
                    }
                } catch (e: OutOfMemoryError) {
                    System.gc()
                }

                retry++
                width /= 2f
                height /= 2f
                scale /= 2f
            }

            if (rotated!=null && rotated!=bitmap) {
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                    bitmap = null
                }
                bitmap = rotated
                rotated = null
            }
        }

        return bitmap
    }

}