package com.interpark.smframework.util.ImageManager

import android.content.Context
import android.database.Cursor
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import java.util.*
import kotlin.collections.ArrayList

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

            val cursor:Cursor? = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION_COUNT, WHERE_CLAUSE, WHERE_ARG, null)
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

            val cur = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION_BUCKET, WHERE_CLAUSE, WHERE_ARG, "$ORDER_BY DESC")

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
    }

    interface OnImageLoadListener {
        open fun onAlbumImageLoadComplete(albums: ArrayList<PhoneAlbum>)
        open fun onError()
    }
}