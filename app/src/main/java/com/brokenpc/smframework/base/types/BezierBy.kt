package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Vec2
import kotlin.math.pow

open class BezierBy(director: IDirector) : ActionInterval(director) {
    protected val _config = ccBezierConfig()
    protected val _startPosition = Vec2(Vec2.ZERO)
    protected val _previousPosition = Vec2(Vec2.ZERO)

    companion object {
        @JvmStatic
        fun create(director: IDirector, t: Float, c: ccBezierConfig): BezierBy {
            val bezierBy = BezierBy(director)
            bezierBy.initWithDuration(t, c)
            return bezierBy
        }

        @JvmStatic
        fun bezierat(a: Float, b: Float, c: Float, d: Float, t: Float): Float {
            return (1f - t).toDouble().pow(3.0).toFloat() * a + 3 * t * Math.pow((1f-t).toDouble(), 2.0).toFloat() * b + 3 * Math.pow(t.toDouble(), 2.0).toFloat() * (1.0f-t) * c + Math.pow(t.toDouble(), 3.0).toFloat()*d
        }
    }

    protected open fun initWithDuration(t: Float, c: ccBezierConfig): Boolean {
        super.initWithDuration(t)
        _config.set(c)

        return true
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        _previousPosition.set(_startPosition.set(target!!.getPosition()))
    }

    override fun Clone(): BezierBy {
        return create(getDirector(), _duration, _config)
    }

    override fun reverse(): BezierBy? {
        val r = ccBezierConfig()
        r.endPosition.set(-_config.endPosition.x, -_config.endPosition.y)
        r.controlPoint_1.set(_config.controlPoint_2.add(Vec2(-_config.endPosition.x, -_config.endPosition.y)))
        r.controlPoint_2.set(_config.controlPoint_1.add(Vec2(-_config.endPosition.x, -_config.endPosition.y)))

        return create(getDirector(), _duration, r)
    }

    override fun update(t: Float) {
        if (_target!=null) {
            var xa = 0f
            var xb = _config.controlPoint_1.x
            var xc = _config.controlPoint_2.x
            var xd = _config.endPosition.x

            var ya = 0f
            var yb = _config.controlPoint_1.y
            var yc = _config.controlPoint_2.y
            var yd = _config.endPosition.y

            val x = bezierat(xa, xb, xc, xd, t)
            val y = bezierat(ya, yb, yc, yd, t)

            val curPos = Vec2(_target!!.getPosition())
            val diff = Vec2(curPos.minus(_previousPosition))
            _startPosition.addLocal(diff)

            val newPos = Vec2(_startPosition.add(Vec2(x, y)))
            _target!!.setPosition(newPos)

            _previousPosition.set(newPos)
        }
    }

    class ccBezierConfig() {
        fun set(c: ccBezierConfig) {
            this.endPosition.set(c.endPosition)
            this.controlPoint_1.set(c.controlPoint_1)
            this.controlPoint_2.set(c.controlPoint_2)
        }
        public var endPosition = Vec2(0, 0)
        public var controlPoint_1 = Vec2(0, 0)
        public var controlPoint_2 = Vec2(0, 0)
    }
}