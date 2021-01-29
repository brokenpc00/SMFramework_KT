package com.brokenpc.smframework.view

import com.brokenpc.app.R
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.shape.PrimitiveRoundRectLine
import com.brokenpc.smframework.base.shape.ShapeConstant.LineType
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4

class SMRoundRectView : SMShapeView {
    protected var _bgShape:PrimitiveRoundRectLine? = null
    protected var _lineTexture:Texture? = null
    private var _round = 0f
    private var _type = LineType.SOLID

    constructor(director:IDirector) : super(director) {
        _type = LineType.SOLID
        _lineWidth = 10.0f
        _lineTexture = director.getTextureManager().createTextureFromResource(R.raw.dash_line_2)
        _round = 0f
    }
    constructor(director: IDirector, tickness:Float, type:LineType) : super(director) {
        _type = type
        _lineWidth = tickness*10.0f
        _bgShape = PrimitiveRoundRectLine(director, _lineTexture, _lineWidth, type)
    }
    constructor(director: IDirector, tickness:Float, type:LineType, round: Float) : super(director) {
        _type = type
        _lineWidth = tickness*10.0f
        _bgShape = PrimitiveRoundRectLine(director, _lineTexture, _lineWidth, type)
        _round = round
    }
    constructor(director: IDirector, tickness:Float, type:LineType, round: Float, color: Color4F) : super(director) {
        _type = type
        _lineWidth = tickness*10.0f
        _bgShape = PrimitiveRoundRectLine(director, _lineTexture, _lineWidth, type)
        _round = round
        setColor(color)
    }

    companion object {
        @JvmStatic
        fun create(director: IDirector):SMRoundRectView {
            val view = SMRoundRectView(director)
            view.init()
            return view
        }

        @JvmStatic
        fun create(director: IDirector, tickness: Float, type: LineType, round: Float):SMRoundRectView {
            val view = SMRoundRectView(director, tickness, type, round)
            view.init()
            return view
        }

        @JvmStatic
        fun create(director: IDirector, tickness: Float, type: LineType, round: Float, color: Color4F):SMRoundRectView {
            val view = SMRoundRectView(director, tickness, type, round, color)
            view.init()
            return view
        }
    }

    override fun setLineWidth(width: Float) {
        _lineWidth = width*10.0f
        if (_bgShape!=null) {
            _bgShape?.releaseResources()
            _bgShape = null
        }
        _bgShape = PrimitiveRoundRectLine(getDirector(), _lineTexture, _lineWidth, _type)
    }

    override fun setCornerRadius(round: Float) {_round = round}

    override fun setBackgroundColor(color: Color4F) {
        setColor(color)
    }

    override fun setBackgroundColor(r: Float, g: Float, b: Float, a: Float) {
        setColor(r, g, b, a)
    }

    fun setLineColor(color: Color4F) {setColor(color)}

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)

        _bgShape?.setSize(_contentSize.width, _contentSize.height, _round)
        _bgShape?.drawRotate(_contentSize.width/2, _contentSize.height/2, 0f)
    }
}