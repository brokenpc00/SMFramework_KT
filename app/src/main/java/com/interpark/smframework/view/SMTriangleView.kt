package com.interpark.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.shape.PrimitiveTriangle
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.view.SMShapeView

class SMTriangleView(director: IDirector): SMShapeView(director) {
    private val _p0 = Vec2(Vec2.ZERO)
    private val _p1 = Vec2(Vec2.ZERO)
    private val _p2 = Vec2(Vec2.ZERO)
    protected lateinit var _bgShape: PrimitiveTriangle


    companion object {
        @JvmStatic
        fun create(director: IDirector): SMTriangleView {
            return create(director, 0f, 0f, 0f, Vec2.ZERO, Vec2.ZERO, Vec2.ZERO)
        }

        @JvmStatic
        fun create(director: IDirector, size: Size): SMTriangleView {
            return create(director, size.width, size.height)
        }

        @JvmStatic
        fun create(director: IDirector, width: Float, height: Float): SMTriangleView {
            val view = SMTriangleView(director)
            view.initWithValue(width, height, 0.015f, Vec2.ZERO, Vec2.ZERO, Vec2.ZERO)
            return view
        }

        @JvmStatic
        fun create(director: IDirector, width: Float, height: Float, aaWidth: Float, p0: Vec2, p1: Vec2, p2: Vec2): SMTriangleView {
            val view = SMTriangleView(director)
            view.initWithValue(width, height, aaWidth, p0, p1, p2)
            return view
        }
    }

    fun setTriangle(p0: Vec2, p1: Vec2, p2: Vec2) {
        setTriangle(_aaWidth, p0, p1, p2)
    }

    fun setTriangle(aaWidth: Float, p0: Vec2, p1: Vec2, p2: Vec2) {
        _aaWidth = aaWidth
        _p0.set(p0)
        _p1.set(p1)
        _p2.set(p2)
    }

    fun initWithValue(width: Float, height: Float, lineWidth: Float, p0: Vec2, p1: Vec2, p2: Vec2): Boolean {
        _bgShape = PrimitiveTriangle(getDirector(), width, height)
        setTriangle(lineWidth, p0, p1, p2)
        return true
    }
}