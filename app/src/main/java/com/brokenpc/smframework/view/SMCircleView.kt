package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.shape.PrimitiveCircle
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4

class SMCircleView : SMShapeView {

    protected var _bgShape:PrimitiveCircle

    constructor(director:IDirector) : super(director) {
         _bgShape = PrimitiveCircle(director)
    }
    constructor(director:IDirector, lineWidth:Float) : super(director) {
        _bgShape = PrimitiveCircle(director)
        _lineWidth = lineWidth
    }
    constructor(director:IDirector, lineWidth: Float, color: Color4F) : super(director) {
        _bgShape = PrimitiveCircle(director)
        _lineWidth = lineWidth
        setColor(color)
    }

    companion object {
        @JvmStatic
        fun create(director: IDirector): SMCircleView {
            val view = SMCircleView(director)
            view.init()
            return view
        }
    }

    override fun setLineWidth(lineWidth: Float) {
        _lineWidth = lineWidth * 2.0f
    }

    fun setLineColor(color:Color4F) {
        setColor(color)
    }

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)
        val x = _contentSize.width/2.0f
        val y = _contentSize.height/2.0f
        var radius = _contentSize.width/2.0f

        if (_contentSize.width > _contentSize.height) {
            radius = _contentSize.height/2
        }
        _bgShape.drawRing(x, y, radius, _lineWidth, 1.5f)
    }
}