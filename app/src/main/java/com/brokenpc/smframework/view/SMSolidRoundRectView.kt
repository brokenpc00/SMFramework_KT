package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.shape.PrimitiveSolidRect
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4

open class SMSolidRoundRectView : SMShapeView {

    constructor(director:IDirector) : super(director) {
        _bgShape = PrimitiveSolidRect(director)
        _cornerRadius = 0.0f

    }

    constructor(director:IDirector, radius:Float) : super(director) {
        _bgShape = PrimitiveSolidRect(director)
        _cornerRadius = radius

    }

    constructor(director:IDirector, round:Float, color:Color4F) : super(director) {
        _bgShape = PrimitiveSolidRect(director)
        _cornerRadius = round
        setColor(color)
    }

    protected var _bgShape:PrimitiveSolidRect


    companion object {
        @JvmStatic
        fun create(director: IDirector): SMSolidRoundRectView {
            val view = SMSolidRoundRectView(director)
            view.init()
            return view
        }
    }

    override fun setCornerRadius(radius: Float) {_cornerRadius = radius}

    override fun setBackgroundColor(color: Color4F) {
        setColor(color)
    }

    override fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float) {
        setColor(r, g, b, a)
    }

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)
        _bgShape.draawRect(_contentSize.width/2f, _contentSize.height/2f, _contentSize.width, _contentSize.height, _cornerRadius, 1f)
    }

}