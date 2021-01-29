package com.brokenpc.smframework.base.sprite

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.texture.Texture

class BitmapSprite(director:IDirector, texture: Texture, cx: Float, cy: Float) : Sprite(director, texture.getWidth().toFloat(), texture.getHeight().toFloat(), cx, cy, 0, 0, texture) {

    companion object {
        @JvmStatic
        fun createFromFile(director: IDirector, fileName:String, loadAsync:Boolean, listener:Texture.OnTextureAsyncLoadListener?, degrees:Int):BitmapSprite? {
            return createFromFile(director, fileName, loadAsync, listener, degrees, -1)
        }

        @JvmStatic
        fun createFromTexture(director: IDirector, texture: Texture):BitmapSprite? {
            return BitmapSprite(director, texture, 0f, 0f)
        }

        @JvmStatic
        fun createFromFile(director: IDirector, fileName: String, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener?, degrees: Int, maxSideLength:Int):BitmapSprite? {
            val texture = director.getTextureManager().createTextureFromFile(fileName, loadAsync, listener, degrees, maxSideLength)
            return if (texture!=null && texture.isValid()) BitmapSprite(director, texture, 0f, 0f) else null
        }

        @JvmStatic
        fun createFromAsset(director: IDirector, fileName: String, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener?):BitmapSprite? {
            val texture = director.getTextureManager().createTextureFromAssets(fileName, loadAsync, listener)
            return if (texture!=null) BitmapSprite(director, texture, 0f, 0f) else null
        }

        @JvmStatic
        fun createFromAsset(director: IDirector, fileName: String, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener?, width:Int, height:Int):BitmapSprite? {
            val texture = director.getTextureManager().createTextureFromAssets(fileName, loadAsync, listener, width, height)
            return if (texture!=null) BitmapSprite(director, texture, 0f, 0f) else null
        }

        @JvmStatic
        fun createFromAsset(director: IDirector, fileName: String, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener?, width:Int, height:Int, cx: Float, cy: Float):BitmapSprite? {
            val texture = director.getTextureManager().createTextureFromAssets(fileName, loadAsync, listener, width, height)
            return if (texture!=null) BitmapSprite(director, texture, cx, cy) else null
        }

        @JvmStatic
        fun createFromBitmap(director: IDirector, key: String, bitmap: Bitmap):BitmapSprite {
            return createFromBitmap(director, key, bitmap, false)
        }

        @JvmStatic
        fun createFromBitmap(director: IDirector, key: String, bitmap: Bitmap, alignCenter:Boolean):BitmapSprite {
            val texture = director.getTextureManager().createTextureFromBitmap(bitmap, key)
            return BitmapSprite(director, texture, 0f, 0f)
        }

        @JvmStatic
        fun createFromResource(director: IDirector, resId:Int):BitmapSprite? {
            return createFromResource(director, resId, false)
        }

        @JvmStatic
        fun createFromResource(director: IDirector, resId: Int, alignCenter: Boolean):BitmapSprite? {
            val texture = director.getTextureManager().createTextureFromResource(resId)
            return if (alignCenter) BitmapSprite(director, texture, texture.getWidth()/2.0f, texture.getHeight()/2.0f) else BitmapSprite(director, texture, 0f, 0f)
        }

        @JvmStatic
        fun createFromDrawable(director: IDirector, drawable: Drawable, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener):BitmapSprite? {
            return createFromDrawable(director, drawable, loadAsync, listener, false)
        }

        @JvmStatic
        fun createFromDrawable(director: IDirector, drawable: Drawable, loadAsync: Boolean, listener: Texture.OnTextureAsyncLoadListener, alignCenter: Boolean):BitmapSprite? {
            val texture = director.getTextureManager().createTextureFromDrawable(drawable, loadAsync, listener)
            return if (texture!=null) BitmapSprite(director, texture, 0f, 0f) else null
        }
    }
}