package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.DrawNode
import com.brokenpc.smframework.base.UIContainerView
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.util.ImageManager.IDownloadProtocol

class SMImageView : UIContainerView, IDownloadProtocol {

    protected var _sprite:DrawNode? = null
    protected var _iconVisible:Boolean = true
    private var _scaleType:ScaleType = ScaleType.FIT_CENTER

    companion object {
        const val GRAVITY_LEFT = 1
        const val GRAVITY_RIGHT = GRAVITY_LEFT.shl(1)
        const val GRAVITY_CENTER_HORIZONTAL = GRAVITY_LEFT.or(GRAVITY_RIGHT)
        const val GRAVITY_TOP = GRAVITY_LEFT.shl(2)
        const val GRAVITY_BOTTOM = GRAVITY_RIGHT.shl(3)
        const val GRAVITY_CENTER_VERTICAL = GRAVITY_TOP.or(GRAVITY_BOTTOM)
    }

    enum class ScaleType {
        CENTER,
        CENTER_INSIDE,
        CENTER_CROP,
        FIT_XY,
        FIT_CENTER
    }

    constructor(director:IDirector) : super(director) {

    }
    constructor(director:IDirector, sprite: Sprite?) : super(director) {
        setSprite(sprite)
    }
    constructor(director:IDirector, assetName: String) : super(director) {
        val sprite = BitmapSprite.createFromAsset(getDirector(), assetName, true, null)
        setSprite(sprite)
    }
    constructor(director:IDirector, assetName: String, isNetwork:Boolean) : super(director) {
        if (isNetwork) {
            ImageDownloader
        } else {
            val sprite = BitmapSprite.createFromAsset(getDirector(), assetName, true, null)
            setSprite(sprite)
        }
    }
    constructor(director:IDirector, sprite: DrawNode?) : super(director) {

    }
    constructor(director:IDirector, texture: Texture?) : super(director) {

    }
    constructor(director:IDirector, x: Float, y: Float, width: Float, height: Float) : super(director) {

    }
    constructor(director:IDirector, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float) : super(director) {

    }

    fun setSprite(sprite: DrawNode?) {
        setSprite(sprite, true)
    }

    fun setSprite(sprite: DrawNode?, fitSize:Boolean) {

    }
}