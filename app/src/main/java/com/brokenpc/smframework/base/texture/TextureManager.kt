package com.brokenpc.smframework.base.texture

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.PERFORM_SEL
import com.brokenpc.smframework.base.types.Ref
import java.util.concurrent.ConcurrentHashMap

//import android.graphics.drawable.Drawable


open class TextureManager(director:IDirector) : Ref(director) {
//    private var _textureMap:Map<String, Texture> = ConcurrentHashMap()
    var _textureMap:ConcurrentHashMap<String, Texture> = ConcurrentHashMap()

    private var _activeTextureId:Int = Texture.NO_TEXTURE

    companion object {
        @JvmStatic
        fun makeDrawableTextureKey(drawable: Drawable): String {
            return "drawable:@$" + drawable.hashCode().toString()
        }

        @JvmStatic
        fun makeResourceTextureKey(resId:Int): String {
            return "resource:@$resId"
        }

        @JvmStatic
        fun makeAssetTextureKey(fileName: String): String {
            return "assets:@" + fileName.hashCode().toString()
        }

        @JvmStatic
        fun makeFileTextureKey(fileName: String): String {
            return "file:@" + fileName.hashCode().toString()
        }

        @JvmStatic
        fun makeNetworkTextureKey(url: String): String {
            return "url:@" + url.hashCode().toString()
        }

        @JvmStatic
        fun makeStringTextureKey(text:String, fontSize:Float, bold:Boolean, italic:Boolean, strikeThru:Boolean):String {
            return "string:@" + text.hashCode().toString() + "_" +
                    fontSize.toString() + "_" +
                    bold.toString() + "_" +
                    italic.toString() +  "_" +
                    strikeThru.toString()
        }

        @JvmStatic
        fun makeHtmlTextureKey(text: String, fontSize: Float): String {
            return "html:@" + text.hashCode().toString() + "_" + fontSize.toString()
        }

        @JvmStatic
        fun makeCanvasTextureKey(keyName: String, width: Int, height: Int): String {
            return "canvas:@" + keyName.hashCode().toString() + width.toString() + "x" + height.toString()
        }

        @JvmStatic
        fun makePreviewTextureKey(keyName: String, width: Int, height: Int): String {
            return "preview:@" + keyName.hashCode().toString() + width.toString() + "x" + height.toString()
        }
    }

    init {

    }

    fun bindTexture(texture: Texture?):Boolean {
        if (texture!=null) {
            if (texture.getId()==Texture.NO_TEXTURE) {
                if (texture.isAsyncLoader()) {
                    return if (texture.isLoading()) {
                        false
                    } else {
                        texture.loadTextureAsync(_director!!)
                        false
                    }
                } else {
                    if (!texture.createOrUpdate(_director!!, null)) {
                        return false
                    }
                }
            }
            if (_activeTextureId!=texture.getId()) {
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getId())
                _activeTextureId = texture.getId()
            }
            return true
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, _director!!.getFrameBufferId())
            _activeTextureId = Texture.NO_TEXTURE
        }
        return false
    }

    fun onResume() {_activeTextureId = Texture.NO_TEXTURE}

    fun onPause() {
        val textures:ArrayList<Texture> = _textureMap.values as ArrayList<Texture>

        for (texture in textures) {
            texture.setId(Texture.NO_TEXTURE)
            if (texture.getRefCount()==0) {
                _textureMap.remove(texture.getKey())
            }
        }

        _activeTextureId = Texture.NO_TEXTURE
    }

    fun size():Int {return _textureMap.size}

    fun createCanvasTexture(width:Int, height:Int, keyName:String):Texture {
        val key:String = makeCanvasTextureKey(keyName, width, height)
        var texture:Texture? = _textureMap[key]
        if (texture==null) {
            texture = CanvasTexture(_director!!, key, width, height)
            _textureMap[key] = texture
        }
        return texture
    }

    fun createPreviewTexture(width: Int, height: Int, keyName: String): Texture {
        val key:String = makePreviewTextureKey(keyName, width, height)
        var texture: Texture? = _textureMap[key]
        if (texture==null) {
            texture = CameraPreviewTexture(_director!!, key, width, height)
            _textureMap[key] = texture
        }
        return texture
    }

    fun createTextureFromBitmap(bitmap: Bitmap?, key: String): Texture {
        var texture:Texture? = _textureMap[key]
        if (texture!=null && texture.getId()==Texture.NO_TEXTURE) {
            if (texture is BitmapTexture) {
                (texture as BitmapTexture).setBitmap(bitmap)
            }
        }
        if (texture==null) {
            texture = BitmapTexture(_director!!, key, bitmap)
            _textureMap[key] = texture
        }

        return texture
    }

    fun createTextureFromResource(resId:Int):Texture {
        val key:String = makeResourceTextureKey(resId)
        var texture:Texture? = _textureMap[key]
        if (texture==null) {
            texture = ResourceTexture(_director!!, key, resId, false, null)
            if (texture!!.isValid()) {
                _textureMap[key] = texture
                texture!!.incRefCount()
            }
        } else {
            texture.incRefCount()
        }

        return texture
    }

    fun createTextureFromDrawable(drawable: Drawable, loadAsync:Boolean, listener: Texture.OnTextureAsyncLoadListener?): Texture? {
        val key:String = makeDrawableTextureKey(drawable)
        var texture:Texture? = _textureMap[key]
        if (texture==null) {
            texture = DrawableTexture(_director!!, key, drawable, loadAsync, listener)
            if (texture!!.isValid()) {
                _textureMap[key] = texture
                if (loadAsync) {
                    texture!!.loadTextureAsync(_director!!)
                }
            } else {
                texture = null
            }
        } else {
            if (loadAsync && listener!=null) {
                val l:Texture.OnTextureAsyncLoadListener = listener
                val t:Texture = texture
                _director!!.getScheduler().performFunctionInMainThread(object : PERFORM_SEL {
                    override fun performSelector() {
                        l.onTextureLoaded(t)
                    }
                })
            }
        }

        return texture
    }

    fun createTextureFromAssets(fileName: String, loadAsync:Boolean, listener:Texture.OnTextureAsyncLoadListener?): Texture? {
        val key:String = makeAssetTextureKey(fileName)
        var texture:Texture? = _textureMap[key]
        if (texture==null) {
            texture = AssetTexture(_director!!, key, fileName, loadAsync, listener)
            if (texture.isValid()) {
                _textureMap[key] = texture
                if (loadAsync) {
                    texture.loadTextureAsync(_director!!)
                }
            } else {
                texture = null
            }
        } else {
            if (loadAsync && listener!=null) {
                val l:Texture.OnTextureAsyncLoadListener = listener
                val t:Texture = texture
                _director!!.getScheduler().performFunctionInMainThread(object : PERFORM_SEL{
                    override fun performSelector() {
                        l.onTextureLoaded(t)
                    }
                })
            }
        }
        return texture
    }

    fun createTextureFromAssets(fileName: String, loadAsync:Boolean, listener: Texture.OnTextureAsyncLoadListener?, width:Int, height: Int):Texture? {
        val key:String = makeAssetTextureKey(fileName)
        var texture:Texture? = _textureMap[key]

        if (texture==null) {
            texture = AssetTexture(_director!!, key, fileName, loadAsync, listener, width, height)
            if (texture.isValid()) {
                _textureMap[key] = texture
                if (loadAsync) {
                    texture.loadTextureAsync(_director!!)
                }
            } else {
                texture = null
            }
        } else {
            if (loadAsync && listener!=null) {
                val l:Texture.OnTextureAsyncLoadListener = listener
                val t:Texture = texture
                _director!!.getScheduler().performFunctionInMainThread(object : PERFORM_SEL{
                    override fun performSelector() {
                        l.onTextureLoaded(t)
                    }
                })
            }
        }

        return texture
    }

    fun createTextureFromFile(fileName: String, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener?, degress:Int, maxSideLength: Int): Texture? {
        val key:String = makeFileTextureKey(fileName+"_"+maxSideLength+"_")
        var texture: Texture? = _textureMap[key]
        if (texture==null) {
            texture = FileTexture(_director!!, key, fileName, loadAsync, listener, degress, maxSideLength)
            if (texture.isValid()) {
                _textureMap[key] = texture
                if (loadAsync) {
                    texture.loadTextureAsync(_director!!)
                }
            } else {
                texture = null
            }
        } else {
            if (texture.isValid() && loadAsync && listener!=null) {
                val l:Texture.OnTextureAsyncLoadListener = listener
                val t:Texture = texture

                _director!!.getScheduler().performFunctionInMainThread(object : PERFORM_SEL{
                    override fun performSelector() {
                        l.onTextureLoaded(t)
                    }
                })
            }
        }

        return texture
    }

    fun createFakeAssetsTexture(fileName: String, loadAsync:Boolean, listener: Texture.OnTextureAsyncLoadListener?, srcTexture: Texture): Texture? {
        val key:String = makeAssetTextureKey(fileName)
        var texture:Texture? = _textureMap[key]

        if (texture==null) {
            texture = AssetTexture(_director!!, key, fileName, loadAsync, listener, srcTexture)
            if (texture!!.isValid()) {
                _textureMap[key] = texture
                if (loadAsync) {
                    texture!!.loadTextureAsync(_director!!)
                }
            } else {
                texture = null
            }
        } else {
            if (loadAsync && listener!=null) {
                val l:Texture.OnTextureAsyncLoadListener = listener
                val t:Texture = texture
                _director!!.getScheduler().performFunctionInMainThread(object : PERFORM_SEL{
                    override fun performSelector() {
                        l.onTextureLoaded(t)
                    }
                })
            }
        }

        return texture
    }

    fun createFakeFileTexture(fileName: String, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener?, degree:Int, maxSideLength: Int, srcTexture: Texture): Texture? {
        val key:String = makeFileTextureKey(fileName)
        var texture:Texture? = _textureMap[key]

        if (texture==null) {
            texture = FileTexture(_director!!, key, fileName, loadAsync, listener, degree, maxSideLength, srcTexture)
            if (texture!!.isValid()) {
                _textureMap[key] = texture
            } else {
                texture = null
            }
        } else {
            texture.setId(srcTexture.getId())
            if (texture.isValid() && loadAsync && listener!=null) {
                val l:Texture.OnTextureAsyncLoadListener = listener
                val t:Texture = texture
                _director!!.getScheduler().performFunctionInMainThread(object : PERFORM_SEL{
                    override fun performSelector() {
                        l.onTextureLoaded(t)
                    }
                })
            }
        }

        return texture
    }

    fun createTextureFromNetwork(url:String, width: Int, height: Int, listener: Texture.OnTextureAsyncLoadListener?, maxSideLength: Int): Texture? {
        val key:String = makeNetworkTextureKey(url)
        var texture: Texture? = _textureMap[key]

        if (texture==null) {
            texture = NetworkTexture(_director!!, key, url, width, height, listener, maxSideLength)
            if (texture.isValid()) {
                _textureMap[key] = texture
                texture.loadTextureAsync(_director!!)
            } else {
                texture = null
            }
        } else {
            if (texture.isValid() && listener!=null) {
                val l:Texture.OnTextureAsyncLoadListener = listener
                val t:Texture = texture

                _director!!.getScheduler().performFunctionInMainThread(object : PERFORM_SEL{
                    override fun performSelector() {
                        l.onTextureLoaded(t)
                    }
                })
            }
        }

        return texture
    }

    fun createTextureFromString(text: String, fontSize: Float, align: Paint.Align, bold: Boolean, italic: Boolean, strikeThru: Boolean, maxWidth:Int, maxLines:Int): Texture? {
        val key:String = makeStringTextureKey(text, fontSize, bold, italic, strikeThru)
        var texture: Texture? = _textureMap[key]
        if (texture==null) {
            texture = TextTexture(_director!!, key, text, fontSize, align, bold, italic, strikeThru, maxWidth, maxLines)
            _textureMap[key] = texture
        }

        return texture
    }

    fun createTextureFromHtmlString(text: String, fontSize: Float, bold: Boolean, italic: Boolean, strikeThru: Boolean): Texture? {
        val key:String = makeHtmlTextureKey(text, fontSize)
        var texture: Texture? = _textureMap[key]

        if (texture==null) {
            texture = TextTexture(_director!!, key, text, fontSize, Paint.Align.LEFT, bold, italic, strikeThru, true)
            _textureMap[key] = texture
        }

        return texture
    }

    fun findFileTexture(fileName: String, maxSideLength:Int):Texture? {
        return findTextureByKey(makeFileTextureKey(fileName+"_"+maxSideLength+"_"))
    }

    // get, remove texture
    fun getTextureFromResource(resId:Int):Texture? {
        return _textureMap[makeResourceTextureKey(resId)]
    }

    fun getTextureFromAssets(fileName:String):Texture? {
        return _textureMap[makeAssetTextureKey(fileName)]
    }

    fun getTextureFromFile(fileName:String):Texture? {
        return _textureMap[makeFileTextureKey(fileName)]
    }

    fun getTextureFromNetwork(url:String):Texture? {
        return _textureMap[makeNetworkTextureKey(url)]
    }

    fun getTexture(key:String): Texture? {
        return _textureMap[key]
    }

    fun removeTexture(texture: Texture?): Boolean {
        if (texture!=null) {
            if (texture.decRefCount()<=0) {
                texture.deleteTexture(_director!!.isGLThread())
                _textureMap.remove(texture.getKey())

                return true
            }
        }

        return false
    }

    fun findTextureByKey(key:String):Texture? {return _textureMap[key]}

    fun removeFakeTexture(texture: Texture?): Boolean {
        if (texture!=null) {
            _textureMap.remove(texture.getKey())
            return true
        }

        return false
    }

    fun getTextureCount():Int {return _textureMap.size}
}