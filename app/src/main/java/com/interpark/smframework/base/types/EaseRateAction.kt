package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector

open class EaseRateAction(director:IDirector) : ActionEase(director) {
    protected var _rate:Float = 0f
    companion object {
        @JvmStatic
        fun create(director: IDirector, action:ActionInterval, rate:Float): EaseRateAction? {
            val action:EaseRateAction = EaseRateAction(director)
            if (action.initWithAction(action, rate)) {
                return action
            }

            return null
        }
    }

    fun setRate(rate: Float) {
        _rate = rate
    }

    fun getRate(): Float {
        return _rate
    }

    protected fun initWithAction(action: ActionInterval?, rate: Float): Boolean {
        if (super.initWithAction(action)) {
            _rate = rate
            return true
        }
        return false
    }

}