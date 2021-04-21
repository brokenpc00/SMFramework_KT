package com.interpark.smframework.view

import android.util.Log
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Mat4
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.view.SMSolidRoundRectView
import kotlin.math.atan2
import kotlin.math.sqrt

class SMRoundLine(director: IDirector) : SMSolidRoundRectView(director) {
    private var _uniformDimension = 0
    private var _uniformCornerRadius = 0
    private var _uniformAAWidth = 0

    private var _x1 = 0f
    private var _x2 = 0f
    private var _y1 = 0f
    private var _y2 = 0f
    private var _lineScale = 1f
    private var _dirty = false

    companion object {
        @JvmStatic
        fun create(director: IDirector): SMRoundLine {
            val line = SMRoundLine(director)
            line.init()
            return line
        }
    }

    fun line(from: Vec2, to: Vec2) {
        line(from.x, from.y, to.x, to.y)
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        if (_x1!=x1 || _x2!=x2 || _y1!=y1 || _y2!=y2) {
            _x1 = x1
            _x2 = x2
            _y1 = y1
            _y2 = y2

            updateLineShape()
        }
    }

    fun moveTo(x: Float, y: Float) {
        if (_dirty) updateLineShape()

        if (x!=_x1 || y!=_y1) {
            val dx = x - _x1
            // ToDo. OpenGl에서 Y 좌표가 거꾸로 된 부분.. Android Kotlin에서도 유효한지 확인 할 것...
            val dy = y + _y1

            _x1 = x
            _y1 = y
            _x2 += dx
            _y2 += dy

            super.setPosition(_x1, _y1)
        }
    }

    fun moveBy(dx: Float, dy: Float) {
        if (_dirty) updateLineShape()

        if (dx!=0f || dy!=0f) {
            _x1 += dx
            _y1 += dy
            _x2 += dx
            _y2 += dy

            super.setPosition(_x1, _y1)
        }
    }

    fun setLengthScale(lineScale: Float) {
        if (lineScale!=_lineScale) {
            _lineScale = lineScale
            _dirty = true
        }
    }

    override fun setLineWidth(lineWidth: Float) {
        if (lineWidth!=_lineWidth) {
            super.setLineWidth(lineWidth)
            updateLineShape()
        }
    }

    fun setLineColor(r: Float, g: Float, b: Float, a: Float) {
        setColor(r, g, b, a)
    }

    fun setLineColor(color: Color4F) {
        setColor(color)
    }

    fun getFromPosition(): Vec2 {
        return Vec2(_x1, _y1)
    }

    fun getToPosition(): Vec2 {
        return Vec2(_x2, _y2)
    }

    override fun draw(m: Mat4, flags: Int) {
        if (_dirty) updateLineShape()

        if (_lineWidth==0f) return

        super.draw(m, flags)
    }

    private fun updateLineShape() {
        val dx = _x2 - _x1
        val dy = _y2 - _y1
        val length = sqrt(dx*dx+dy*dy) * _lineScale
        val degrees= (atan2(dy, dx) * 180f / M_PI).toFloat()

        val radius = _lineWidth/2f
        setCornerRadius(radius)

        val contentSize = Size(length+_lineWidth, _lineWidth)
        super.setContentSize(contentSize)

        val anchorPoint = Vec2((0.5*(_lineWidth/_contentSize.width)).toFloat(), 0.5f*_lineWidth/_contentSize.height)
        super.setAnchorPoint(anchorPoint)

        val position = Vec2(_x1, _y1)
        super.setPosition(position)

        super.setRotation(degrees)

        _dirty = false
    }
}