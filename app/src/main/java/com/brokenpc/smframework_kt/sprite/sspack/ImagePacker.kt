package com.brokenpc.smframework_kt.sprite.sspack

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.SMAsyncTask
import com.interpark.smframework.util.FileManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.StringBuilder
import kotlin.collections.ArrayList

class ImagePacker(director: IDirector,
                  context: Context,
                  name: String,
                  infos: ArrayList<ResInfo>,
                  requirePow2: Boolean,
                  requireSquare: Boolean,
                  maximumWidth: Int,
                  maximumHeight: Int,
                  padding: Int,
                  listener: OnLogListener) : SMAsyncTask<Void, Void, Void>(director) {
    private val _context = context
    private var _requirePow2 = requirePow2
    private var _requireSquare = requireSquare
    private var _padding = padding
    private var _outputWidth = 0
    private var _outputHeight = 0
    private var _maximumWidth = maximumWidth
    private var _maximumHeight = maximumHeight
    private var _textureId = -1
    private val _infos: ArrayList<ResInfo> = infos
    private var _name: String = name

    private val _addedSprite: ArrayList<ResInfo> = ArrayList()
    private val _remainSprite: ArrayList<ResInfo> = ArrayList()

    interface OnLogListener {
        fun onLog(text: String)
        fun onBitmap(bitmap: Bitmap)
    }

    private var _logListener: OnLogListener? = listener

    private var _handler: Handler = Handler(Looper.getMainLooper())

    companion object {
        val SAVE_ROOT_PATH = "/SpriteBuilder/"
        val RESOURCE_FILE_NAME = "SR"
        val RESOURCE_HEADER = "class SR {\n"
        val RESOURCE_FOOTER = "}\n"
        val SOURCE_FILE_NAME = "CommonSprite"
        val SOURCE_HEADER = "   private val SPRITE_INFO:ArrayList<SpriteInfo> = {\n"
        val SOURCE_FOOTER = "   }\n\n" +
                            "   public fun getSpriteInfo(index: Int): SpriteCoordInfo? {\n" +
                            "       if (index>=SPRITE_INFO.size) return null \n" +
                            "       return SPRITE_INFO[index] \n" +
                            "   }\n\n" +
                            "   public fun getNumSprite: Int { \n" +
                            "       return SPRITE_INFO.size \n" +
                            "   } \n" +
                            "} \n"


        @JvmStatic
        fun loadRawResourceBitmap(context: Context, resId: Int): Bitmap? {
            var bitmap: Bitmap? = null
            var IS: InputStream? = null
            val option = BitmapFactory.Options()
            option.inPreferredConfig = Bitmap.Config.ARGB_8888

            try {
                IS = context.resources.openRawResource(resId)
                bitmap = BitmapFactory.decodeStream(IS, null, option)
            } catch (e: OutOfMemoryError) {

            } catch (e: Resources.NotFoundException) {

            } finally {
                try {
                    IS!!.close()
                } catch (e: IOException) {

        }
                IS = null
            }

            return bitmap
        }
    }

    fun publishLog(text: String) {
        if (_logListener!=null) {
            _handler.post {
                _logListener?.onLog(text)
            }
        }
    }

    fun publishImage(bitmap: Bitmap) {
        if (_logListener!=null) {
            _handler.post {
                _logListener?.onBitmap(bitmap)
            }
        }
    }

    fun packImage(): Boolean {

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true

        publishLog("Total count of resource : ${_infos.size}")

        for (info in _infos) {
            BitmapFactory.decodeResource(_context.resources, info.resId, options)
            info.width = options.outWidth
            info.height = options.outHeight

            if (info.width<=0 || info.height<=0) {
                // invalid image
                val names = _context.resources.getResourceName(info.resId).split("/")
                publishLog("> 비정상인 리소스 : ${names[names.size-1]}")
                continue
            }
            if (info.width+_padding >= _maximumWidth ||
                    info.height+_padding >= _maximumHeight) {
                val names = _context.resources.getResourceName(info.resId).split("/")
                publishLog("> 이미지가 최대크기보다  : ${names[names.size-1]}")
                continue
            }
            _remainSprite.add(info)
        }
        publishLog("가용 리소스 수 : ${_remainSprite.size}")
        publishLog("-----------------------------------")

        // descending size sort
        _remainSprite.sort()

        var textureId = 0
        while (_remainSprite.size>0) {
            publishLog("[[[[[ start packing (Texture : $textureId)")

            _outputWidth = _maximumWidth
            _outputHeight = _maximumHeight
            _addedSprite.clear()

            if (!packImageRectangles()) {
                publishLog("-----------------------------------")
                publishLog("          Failed to packing")
                publishLog("-----------------------------------")
                return false
            }

            publishLog("........... now saving")
            val bitmap = saveBitmap(_context, _name, textureId, _outputWidth, _outputHeight, _addedSprite)
            publishImage(bitmap)

            publishLog("[[[[[ Saved ${_addedSprite.size} images at TextureId : $textureId")
            publishLog("-----------------------------------")

            textureId++
        }



        return false
    }

    fun packImageRectangles(): Boolean {
        val smallestWidth = 1
        val smallestHeight = 1

        var testWidth = _outputWidth
        var testHeight = _outputHeight

        var shrinkVertical = false
        var pass = 0

        while (true) {
            publishLog("[[[[[ ... pass : $pass")
            pass++

            _addedSprite.clear()

            if (!testPackingImages(testWidth, testHeight)) {
                if (_addedSprite.size==0) return false

                if (shrinkVertical) return true

                shrinkVertical = true
                testWidth += smallestWidth + _padding * 2
                testHeight += smallestHeight + _padding * 2
                continue
            }

            testWidth = 0
            testHeight = 0
            for (info in _addedSprite) {
                testWidth = testWidth.coerceAtLeast(info.rect!!.right)
                testHeight = testHeight.coerceAtLeast(info.rect!!.bottom)
            }

            if (!shrinkVertical) {
                testWidth -= _padding
            }
            testHeight -= _padding

            // make multiplication 2
            if (_requirePow2) {
                testWidth = MiscHelper.findNextPowerOfTwo(testWidth)
                testHeight = MiscHelper.findNextPowerOfTwo(testHeight)
            }

            if (_requireSquare) {
                val max = testWidth.coerceAtLeast(testHeight)
                testWidth = max
                testHeight = max
            }

            if (testWidth==_outputWidth && testHeight==_outputHeight) {
                if (shrinkVertical) {
                    for (info in _addedSprite) {
                        _remainSprite.remove(info)
                    }
                    return true
                }

                shrinkVertical = true
            }

            _outputWidth = testWidth
            _outputHeight = testHeight

            if (!shrinkVertical) {
                testWidth -= smallestWidth
            }
            testHeight -= smallestHeight
        }
    }

    fun testPackingImages(testWidth: Int, testHeight: Int): Boolean {
        val rectanglePacker = RectanglePacker(testWidth, testHeight)

        var origin:Point = Point(0, 0)

        var progress = 0
        for (i in 0 until _remainSprite.size) {
            val info = _remainSprite[i]

            try {

            } catch (e: OutOfSpaceException) {
                Log.e("EXCEPTION", "......... out of space ..........")
                continue
            }

            info.texId = _textureId
            info.rect = Rect(origin.x, origin.y, origin.x+info.width+_padding, origin.y+info.height+_padding)
            _addedSprite.add(info)

            val nowProgress = (10*(i+1)/_remainSprite.size)
            if (nowProgress!=progress) {
                progress = nowProgress
                publishLog("[[[[[ 진행 : ${progress*10}%")
            }
        }

        return true
    }

    fun saveBitmap(context: Context, name: String, textureId: Int, width: Int, height: Int, infos: ArrayList<ResInfo>): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        for (info in _infos) {
            val b = loadRawResourceBitmap(context, info.resId)
            if (b!=null) {
                canvas.drawBitmap(b, info.rect!!.left.toFloat(), info.rect!!.top.toFloat(), null)
                b.recycle()
            }
        }

        try {
            val filePath = FileManager.getInstance().getFullFilePath(FileManager.FileType.SpriteBuilder, "$name$textureId.png")
            val fos = FileOutputStream(File(filePath))
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos)
            fos.flush()
            fos.close()
            Log.i("TextureBuilder", "$filePath : (${bitmap.width}, ${bitmap.height}) saved.")
        } catch (e: Exception) {
            Log.e("TextureBuilder", e.toString())
        }

        return bitmap
    }

    fun writeResourceFile(context: Context, infos: ArrayList<ResInfo>) {
        var os: FileOutputStream? = null
        try {
            val filePath = FileManager.getInstance().getFullFilePath(FileManager.FileType.SpriteBuilder, "$RESOURCE_FILE_NAME.java")
            val file = File(filePath)
            file.delete()

            os = FileOutputStream(filePath, true)
            os.write(RESOURCE_HEADER.toByteArray(Charsets.UTF_8))

            var index = 0
            for (info in _infos) {
                if (info.texId>=0) {
                    val names = context.resources.getResourceName(info.resId).split("/")
                    os.write("\tval ${names[names.size-1]}:Int = ${Integer.toHexString(index)}".toByteArray(Charsets.UTF_8))
                    index++
                }
            }
            os.write(RESOURCE_FOOTER.toByteArray(Charsets.UTF_8))
            os.flush()
        } catch (e: Exception) {
            error("[[[[[ Failed to write resource file")
        } finally {
            if (os!=null) {
                try {
                    os.close()
                    os = null
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun writeSourceFile(context: Context, infos: ArrayList<ResInfo>) {
        var os:FileOutputStream? = null
        try {
            val filePath = FileManager.getInstance().getFullFilePath(FileManager.FileType.SpriteBuilder, "$SOURCE_FILE_NAME.java")
            val file = File(filePath)
            file.delete()

            os = FileOutputStream(file, true)
            os.write(SOURCE_HEADER.toByteArray(Charsets.UTF_8))

            for (info in _infos) {
                if (info.texId>=0) {
                    os.write(getInfoString(info).toByteArray(Charsets.UTF_8))
                }
            }

            os.write(SOURCE_FOOTER.toByteArray(Charsets.UTF_8))
            os.flush()
        } catch (e: Exception) {
            error("[[[[[ Failed to write source file")
        } finally {
            if (os!=null) {
                try {
                    os.close()
                    os = null
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getAlignX(align: Int, width: Int): Float {
        return when (align and 0x0f) {
            ResInfo.Align.LEFT -> {
                0f
            }
            ResInfo.Align.RIGHT -> {
                width.toFloat()
            }
            else -> {
                width/2f
            }
        }
    }

    fun getAlignY(align: Int, height: Int): Float {
        return when (align and 0xf0) {
            ResInfo.Align.TOP -> {
                0f
            }
            ResInfo.Align.BOTTOM -> {
                height.toFloat()
            }
            ResInfo.Align.CENTER_VERTICAL -> {
                height/2f
            }
            else -> 0f
        }
    }

    fun getInfoString(info: ResInfo): String {
        val sb = StringBuilder()

        sb.append("new SpriteInfo(")
        sb.append(info.texId)
        sb.append(", ")
        sb.append(info.rect!!.left)
        sb.append(", ")
        sb.append(info.rect!!.top)
        sb.append(", ")
        sb.append(info.width)
        sb.append(", ")
        sb.append(info.height)
        sb.append(", ")
        sb.append(getAlignX(info.align, info.width))
        sb.append("f, ")
        sb.append(getAlignY(info.align, info.height))
        sb.append("f ), \n")

        return sb.toString()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        packImage()
        return null
    }

    override fun onPreExecute() {
        publishLog("----------------------------------------------")
        publishLog("                   시작")
        publishLog("----------------------------------------------")
    }

    override fun onPostExecute(result: Void?) {
        publishLog("----------------------------------------------")
        publishLog("                   종료")
        publishLog("----------------------------------------------")
    }

}