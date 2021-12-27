package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SMView.Companion.M_PI
import com.brokenpc.smframework.base.types.ActionInterval
import kotlin.math.sin

class ScaleSine(director: IDirector) : ActionInterval(director) {
    protected var _deltaScale = 1f
    protected var _baseScale = 1f

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, maxScale: Float): ScaleSine {
            val action = ScaleSine(director)
            if (action.initWithDuration(duration)) {
                action._deltaScale = maxScale - 1f
            }
            return action
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        if (target!=null) {
            _baseScale = target.getScale()
        }
    }

    override fun update(dt: Float) {
        _target?.setScale(_baseScale*(1f + _deltaScale * sin(dt* M_PI).toFloat()))
    }
}