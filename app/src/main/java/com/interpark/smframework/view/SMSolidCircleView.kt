package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.shape.PrimitiveCircle
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4

class SMSolidCircleView : SMShapeView {
    protected var _bgShape:PrimitiveCircle
    constructor(director:IDirector) : super(director) {
        _bgShape = PrimitiveCircle(director)
        _aaWidth = 0.015f
    }

    constructor(director:IDirector, color:Color4F) : super(director) {
        _bgShape = PrimitiveCircle(director)
        _aaWidth = 0.015f
    }

    companion object {
        @JvmStatic
        fun create(director: IDirector): SMSolidCircleView {
            val view = SMSolidCircleView(director)
            view.init()
            return view
        }
    }

    override fun setBackgroundColor(color: Color4F) {
        setColor(color)
    }

    override fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float) {
        setColor(r, g, b, a)
    }

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)

        val x = _contentSize.width/2.0f
        val y = _contentSize.height/2.0f
        var radius = _contentSize.width/2.0f
        if (_contentSize.width>_contentSize.height) {
            radius = _contentSize.height/2.0f
        }

        _bgShape.drawCircle(x, y, radius, _aaWidth)
    }
}