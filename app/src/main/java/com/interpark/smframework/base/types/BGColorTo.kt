package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMView
import java.time.Duration

class BGColorTo(director:IDirector) : ActionInterval(director) {
    protected var _startColor:Color4F = Color4F(Color4F.TRANSPARENT)
    protected var _toColor:Color4F = Color4F(Color4F.TRANSPARENT)
    protected var _deltaColor:Color4F = Color4F(Color4F.TRANSPARENT)

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, color: Color4F):BGColorTo {
            val colorTo:BGColorTo = BGColorTo(director)
            if (colorTo.initWithDuration(duration)) {
                colorTo._toColor = Color4F(color)
            }

            return colorTo
        }
    }

    override fun startWithTarget(target: SMView?) {
        if (target!=null) {
            super.startWithTarget(target)
            _startColor = Color4F(target.getBackgroundColor())
            _deltaColor = _toColor.minus(_startColor)
        }
    }

    override fun update(dt: Float) {
        if (_target!=null) {
            val color:Color4F = _startColor.add(_deltaColor.multiply(dt))
            _target!!.setBackgroundColor(color)
        }
    }
}