package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4
import com.brokenpc.smframework.base.types.Vec2

open class SMShapeView(director: IDirector) : SMView(director) {

    companion object {
        @JvmStatic
        fun getQuadDimen(q:Quadrant, size: Vec2):Vec2 {
            if (q==Quadrant.ALL) return size

            return when (q) {
                Quadrant.LEFT_HALF, Quadrant.RIGHT_HALF -> Vec2(size.x*2, size.y)
                Quadrant.TOP_HALF, Quadrant.BOTTOM_HALF -> Vec2(size.x, size.y*2)
                Quadrant.LEFT_TOP, Quadrant.LEFT_BOTTOM, Quadrant.RIGHT_TOP, Quadrant.RIGHT_BOTTOM -> Vec2(size.x*2, size.y*2)
                else -> size
            }
        }
    }

    enum class Quadrant {
        ALL,
        LEFT_HALF,
        RIGHT_HALF,
        TOP_HALF,
        BOTTOM_HALF,
        LEFT_TOP,
        LEFT_BOTTOM,
        RIGHT_TOP,
        RIGHT_BOTTOM
    }

    fun getIntToQuadrent(value:Int):Quadrant {
        return when (value) {
            1 -> Quadrant.LEFT_HALF
            2 -> Quadrant.RIGHT_HALF
            3 -> Quadrant.TOP_HALF
            4 -> Quadrant.BOTTOM_HALF
            5 -> Quadrant.LEFT_TOP
            6 -> Quadrant.LEFT_BOTTOM
            7 -> Quadrant.RIGHT_TOP
            8 -> Quadrant.RIGHT_BOTTOM
            else -> Quadrant.ALL
        }
    }

    fun getQuadrantToInt(q:Quadrant):Int {
        return when (q) {
            Quadrant.LEFT_HALF -> 1
            Quadrant.RIGHT_HALF -> 2
            Quadrant.TOP_HALF -> 3
            Quadrant.BOTTOM_HALF -> 4
            Quadrant.LEFT_TOP -> 5
            Quadrant.LEFT_BOTTOM -> 6
            Quadrant.RIGHT_TOP -> 7
            Quadrant.RIGHT_BOTTOM -> 8
            else -> 0
        }
    }

    open fun setCornerRadius(radius:Float) {_cornerRadius = radius}
    open fun setAntiAliasWidth(aaWidth:Float) {_aaWidth = aaWidth}
    open fun setLineWidth(lineWidth:Float) {_lineWidth = lineWidth}
    open fun setConerQuadrant(q:Quadrant) {_quarant = getQuadrantToInt(q)}
    open fun setConerQuadrant(value:Int) {
        _quarant = value
        if (_quarant<0) _quarant = 0
        if (_quarant>8) _quarant = 8
    }

    fun getCornerRadius():Float {return _cornerRadius}
    fun getAntiAliasWidth():Float {return _aaWidth}
    fun getLineWidth():Float {return _lineWidth}
    fun getCornerQuadrant():Int {return _quarant}

    override fun updateColor() {
        _shapeColor.set(_displayedColor)
    }

    override fun draw(m:Mat4, flags:Int) {
        getDirector().setColor(_shapeColor.r, _shapeColor.g, _shapeColor.b, _shapeColor.a)
    }

    protected var _shapeColor:Color4F = Color4F(Color4F.TRANSPARENT)
    protected var _cornerRadius:Float = 0.0f
    protected var _lineWidth:Float = 1.0f
    protected var _aaWidth:Float = 0.0f
    protected var _quarant:Int = 0
}