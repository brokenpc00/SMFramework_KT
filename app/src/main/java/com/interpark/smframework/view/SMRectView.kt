package com.brokenpc.smframework.view

import android.opengl.GLES20
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.shape.PrimitiveRect
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4

class SMRectView : SMShapeView {

    protected var _bgShape:PrimitiveRect? = null

    constructor(director:IDirector) : super(director) {
        _bgShape = PrimitiveRect(director, 1f, 1f, 0f, 0f, false)
    }
    constructor(director: IDirector, outlineColor:Color4F) : super(director) {
        setColor(outlineColor)
    }
    constructor(director: IDirector, outlineColor: Color4F, lineWidth:Float) : super(director) {
        setColor(outlineColor)
        _lineWidth = lineWidth
    }

    companion object {
        @JvmStatic
        fun create(director: IDirector):SMRectView {
            val view = SMRectView(director)
            view.init()
            return view
        }
    }

    override fun setBackgroundColor(color: Color4F) {
        setColor(color)
    }

    override fun setLineWidth(lineWidth: Float) {
        _lineWidth = lineWidth
    }

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)
        GLES20.glLineWidth(_lineWidth)
        _bgShape?.drawScaleXY(0f, 0f, _contentSize.width, _contentSize.height)
    }
}