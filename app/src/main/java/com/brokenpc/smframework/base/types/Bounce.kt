package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc.Companion.M_PI
import com.brokenpc.smframework.util.tweenfunc.Companion.M_PI_2
import kotlin.math.cos

class Bounce(dirctor: IDirector) : ActionInterval(dirctor) {

    protected var _bounceRate = 0f
    protected var _bounceCount = 0
    protected var _maxScale = 1f
    protected var _view:SMView? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, bounceRate: Float, bounceCount: Int): Bounce {
            return create(director, duration, bounceRate, bounceCount, 1f)
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, bounceRate: Float, bounceCount: Int, maxScale: Float): Bounce {
            return create(director, duration, bounceRate, bounceCount, maxScale, null)
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, bounceRate: Float, bounceCount: Int, maxScale: Float, target: SMView?): Bounce {
            val action = Bounce(director)
            if (action.initWithDuration(duration)) {
                action._bounceRate = bounceRate
                action._bounceCount = bounceCount
                action._maxScale = maxScale
                action._view = target
            }

            return action
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        if (target!=null) {
            _view = target
        }
    }

    override fun update(dt: Float) {
        super.update(dt)

        val a = cos(dt*M_PI_2).toFloat()
        val s = (a * _bounceRate * Math.abs(Math.sin(dt * _bounceCount * M_PI))).toFloat()
        _target?.setScale((1f+s) * _maxScale)
    }
}