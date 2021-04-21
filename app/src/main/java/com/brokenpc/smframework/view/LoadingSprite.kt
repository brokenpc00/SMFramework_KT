package com.interpark.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4
import com.brokenpc.smframework.util.ImageManager.ImageDownloadTask
import com.brokenpc.smframework.view.SMImageView

class LoadingSprite(director: IDirector) : SMImageView(director) {

    private var _start: Float = 0.0f
    private var _visibleTime:Float = 0.0f

    companion object {
        const val NUM_TICK = 12.0f
        const val TICK_TIME = 0.1f
        const val DELAY_TIME = 0.2f
        const val FADE_TIME = 0.1f

        @JvmStatic
        fun createWithTexture(director: IDirector): LoadingSprite {
            return createWithTexture(director, null)
        }

        @JvmStatic
        fun createWithTexture(director: IDirector, texture: Texture?): LoadingSprite {
            val loadingSprite = LoadingSprite(director)

            if (texture!=null) {
                val sprite = Sprite(director, texture, 0f, 0f)
                loadingSprite.setSprite(sprite)
            } else {
                val sprite = BitmapSprite.createFromAsset(director, "images/loading_spinner_white.png", true, null)
                loadingSprite.setSprite(sprite)
            }

            loadingSprite._start = NUM_TICK*SMView.randomFloat(0f, 1.0f)
            loadingSprite.setColor(Color4F.XEEEFF1)

            return loadingSprite
        }

        @JvmStatic
        fun createWithFile(director: IDirector): LoadingSprite {
            return createWithFile(director, "")
        }

        @JvmStatic
        fun createWithFile(director: IDirector, imageFileName: String): LoadingSprite {
            val loadingSprite = LoadingSprite(director)

            var fileName = imageFileName
            if (fileName=="") {
                fileName = "images/loading_spinner_white.png"
            }

            val sprite = BitmapSprite.createFromAsset(director, fileName, true, null)
            loadingSprite.setSprite(sprite)
            loadingSprite._start = NUM_TICK*SMView.randomFloat(0f, 1.0f)
            loadingSprite.setColor(Color4F.XEEEFF1)

            return loadingSprite
        }
    }

    override fun setVisible(visible: Boolean) {
        if (_visible!=visible) {
            _visible = visible
            if (_visible) {
                _transformUpdated = true
                _transformDirty = true
                _inverseDirty = true
                _visibleTime = _director!!.getGlobalTime()
            }
        }
    }

    override fun draw(m: Mat4, flags: Int) {
        var t = _director!!.getGlobalTime() - _visibleTime

        if (t< DELAY_TIME) {
            return
        }

        t = (t- DELAY_TIME) / FADE_TIME

        var newAlpha = 1f
        if (t<1) {
            newAlpha *= t
        }

        if (getAlpha()!=newAlpha) {
            setAlpha(newAlpha)
        }

        val time = _director!!.getGlobalTime() + _start

        // 흐른 시간 만큼 rotate
        val tick = (time/TICK_TIME) % NUM_TICK
        val angle = tick * 360.0f/ NUM_TICK

        if (getRotation()!=angle) {
            setRotation(angle)
        }

        super.draw(m, flags)
    }

    override fun removeDownloadTask(task: ImageDownloadTask?) {
        TODO("Not yet implemented")
    }

    override fun addDownloadTask(task: ImageDownloadTask?): Boolean {
        TODO("Not yet implemented")
    }
}