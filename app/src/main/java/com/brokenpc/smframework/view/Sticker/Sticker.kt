package com.brokenpc.smframework.view.Sticker

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.sprite.GridSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.Ref
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst

open class Sticker(director: IDirector): RemovableSticker(director) {
    protected var _fatValue = 0f
    protected var _controlType = ControlType.NONE
    private var _listener: OnSpriteLoadedCallback? = null

    companion object {
        const val FAT_STEP_VALUE  = 0.2f


        @JvmStatic
        fun create(director: IDirector): Sticker {
            return create(director, 0f, 0f, 0f, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, x: Float, y: Float, width: Float, height: Float): Sticker {
            val sticker = Sticker(director)
            sticker.setContentSize(width, height)
            sticker.setPosition(x, y)
            sticker.init()
            return sticker
        }
    }

    enum class ControlType {
        NONE,
        FAN,
        DELETE,
        UNPACK,
        PACK
    }

    override fun init(): Boolean {
        setAnchorPoint(Vec2.MIDDLE)
        return super.init()
    }

    fun isRemoved(): Boolean {return getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null}

    fun setControlType(type: ControlType) {_controlType=type}

    fun getControlType(): ControlType {return _controlType}

    override fun onImageLoadComplete(sprite: Sprite?, tag: Int, direct: Boolean) {
        if (sprite!=null) {
            val grid = GridSprite.create(getDirector(), sprite)
            if (grid!=null) {
                setSprite(grid)

                if (getContentSize().width==0f && getContentSize().height==0f) {
                    setContentSize(getSprite()!!.getWidth(), getSprite()!!.getHeight())
                }

                _listener?.onSpriteLoadedCallback(this, grid)
            } else {
                setSprite(sprite)
                _listener?.onSpriteLoadedCallback(this, sprite)
            }
        }
    }

    fun setFatValue(value: Float) {
        _fatValue = value
        if (_fatValue!=0f) {
            if (_sprite is GridSprite) {
                val size = Size(_sprite!!.getTexture()!!.getWidth(), _sprite!!.getTexture()!!.getHeight())
                (_sprite as GridSprite).grow(size.width/2f, size.height/2f, value, FAT_STEP_VALUE, size.width)
            }
        }
    }

    fun getFatValue(): Float {return _fatValue}

    interface OnSpriteLoadedCallback {
        fun onSpriteLoadedCallback(sender: Sticker, sprite: Sprite?)
    }

    fun setOnSpriteLoadedCallback(l: OnSpriteLoadedCallback) {_listener = l}

    override fun Clone(): Sticker? {
        if (getSprite()!=null) {
            val newSprite = Sprite(getDirector(), getSprite()!!.getTexture()!!, getSprite()!!.getTexture()!!.getWidth()/2f, getSprite()!!.getTexture()!!.getHeight()/2f)

            val newSticker = Sticker.create(getDirector())
            newSticker.setSprite(newSprite)
            newSticker.setPosition(getPosition())
            newSticker.setScale(getScale())
            newSticker.setRotation(getRotation())

            return newSticker
        }
        return null
    }

    override fun setAnchorPoint(point: Vec2) {
        super.setAnchorPoint(Vec2.MIDDLE)
    }

    override fun setAnchorPoint(anchorX: Float, anchorY: Float) {
        super.setAnchorPoint(0.5f, 0.5f)
    }

    override fun setAnchorPoint(point: Vec2, immediate: Boolean) {
        super.setAnchorPoint(Vec2.MIDDLE, true)
    }
}