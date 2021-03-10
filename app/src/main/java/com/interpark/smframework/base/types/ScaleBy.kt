package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval

class ScaleBy(director: IDirector): ScaleTo(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, s: Float): ScaleBy {
            val scale = ScaleBy(director)
            scale.initWithDuration(duration, s)
            return scale
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, sx: Float, sy: Float): ScaleBy {
            val scale = ScaleBy(director)
            scale.initWithDuration(duration, sx, sy)
            return scale
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, sx: Float, sy: Float, sz: Float): ScaleBy {
            val scale = ScaleBy(director)
            scale.initWithDuration(duration, sx, sy, sz)
            return scale
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)

        _deltaX = _startScaleX * _endScaelX - _startScaleX
        _deltaY = _startScaleY * _endScaleY - _startScaleY
        _deltaZ = _startScaleZ * _endScaleZ - _startScaleZ
    }

    override fun Clone(): ActionInterval? {
        return ScaleBy.create(getDirector(), _duration, _endScaelX, _endScaleY, _endScaleZ)
    }

    override fun reverse(): ActionInterval? {
        return ScaleBy.create(getDirector(), _duration, 1f/_endScaelX, 1f/_endScaleY, 1f/_endScaleZ)
    }
}