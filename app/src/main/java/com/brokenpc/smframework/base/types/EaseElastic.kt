package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval

open class EaseElastic(director: IDirector) : ActionEase(director) {
    protected var _period: Float = 0f

    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?, period: Float): EaseElastic? {
            val ease = EaseElastic(director)
            if (ease.initWithAction(action, period)) {
                return ease
            }

            return null
        }
    }

    override fun initWithAction(action: ActionInterval?): Boolean {
        return initWithAction(action, 0.3f)
    }

    fun initWithAction(action: ActionInterval?, period: Float): Boolean {
        if (super.initWithAction(action)) {
            _period = period
            return true
        }

        return false
    }

    fun getPeriod(): Float {return _period}

    fun setPeriod(period: Float) {_period = period}
}