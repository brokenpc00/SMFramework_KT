package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView

class BezierTo(director: IDirector) : BezierBy(director) {

    protected val _toConfig = ccBezierConfig()

    companion object {
        @JvmStatic
        fun create(director: IDirector, t: Float, c: ccBezierConfig): BezierTo {
            val bezierTo = BezierTo(director)
            bezierTo.initWithDuration(t, c)
            return bezierTo
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        _config.controlPoint_1.set(_toConfig.controlPoint_1.minus(_startPosition))
        _config.controlPoint_2.set(_toConfig.controlPoint_2.minus(_startPosition))
        _config.endPosition.set(_toConfig.endPosition.minus(_startPosition))
    }

    override fun Clone(): BezierTo {
        return BezierTo.create(getDirector(), _duration, _toConfig)
    }

    override fun reverse(): BezierBy? {
        return null
    }

    override fun initWithDuration(t: Float, c: ccBezierConfig):Boolean {
        super.initWithDuration(t, c)
        _toConfig.set(c)
        return true
    }
}