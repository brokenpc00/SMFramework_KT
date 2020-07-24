package com.interpark.smframework.view

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.shape.PrimitiveRect
import com.interpark.smframework.base.types.Color4F
import com.interpark.smframework.base.types.Mat4

class SMSolidRectView(director: IDirector, solidColor: Color4F?=null) : SMShapeView(director)
{
    init {
        if (solidColor!=null) {
            setColor(solidColor)
        }
    }

    protected var _bgShape:PrimitiveRect = PrimitiveRect(director, 1f, 1f, 0f, 0f, true)

    companion object {
        @JvmStatic
        fun create(director: IDirector): SMSolidRectView {
            val view:SMSolidRectView = SMSolidRectView(director)
            view.init()
            return view
        }
    }

    override fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float) {
        setColor(r, g, b, a)
    }

    override fun setBackgroundColor(color: Color4F) {
        setColor(color)
    }

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)
        _bgShape.drawScaleXY(0f, 0f, _contentSize.width, _contentSize.height)
    }
}