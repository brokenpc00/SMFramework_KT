package com.brokenpc.smframework.base

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2

open class UIContainerView : SMView {
    protected var _uiContainer:SMView
    protected var _paddingLeft:Float = 0f
    protected var _paddingRight:Float = 0f
    protected var _paddingTop:Float = 0f
    protected var _paddingBottom:Float = 0f

    fun getPaddingLeft():Float {return _paddingLeft}
    fun getPaddingRight():Float {return _paddingRight}
    fun getPaddingTop():Float {return _paddingTop}
    fun getPaddingBottom():Float {return _paddingBottom}

    constructor(director:IDirector) : super(director) {
        _uiContainer = SMView.create(director)
        _uiContainer.setAnchorPoint(0.5f, 0.5f)
        addChild(_uiContainer, 0, "")
    }
    fun getContainer():SMView {return _uiContainer}

    protected fun setSMViewContentSize(width:Float, height:Float) {
        this.setContentSize(Size(width, height))
    }
    protected fun setSMViewContentSize(size:Size) {
        super.setContentSize(size)
    }

    override fun setContentSize(width: Float?, height: Float?) {
        setContentSize(Size(width, height))
    }

    override fun setContentSize(size: Size) {
        val innerSize = Size(size.width-_paddingLeft-_paddingRight, size.height-_paddingTop-_paddingBottom)
        val pos = Vec2((_paddingLeft+size.width-_paddingRight)/2, (_paddingBottom+size.height-_paddingTop)/2)
        _uiContainer.setPosition(pos)
        _uiContainer.setContentSize(innerSize)

        super.setContentSize(size)
    }

    open fun setPadding(padding:Float) {
        setPadding(padding, padding, padding, padding)
    }

    open fun setPadding(left:Float, top:Float, right:Float, bottom:Float) {
        _paddingLeft = left
        _paddingTop = top
        _paddingRight = right
        _paddingBottom = bottom
        setContentSize(getContentSize())
    }

}