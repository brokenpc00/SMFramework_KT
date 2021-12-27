package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseExponentialInOut(director: IDirector): ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseExponentialInOut? {
            val ease = EaseExponentialInOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseExponentialInOut.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.expoEaseInOut(time))
    }

    override fun reverse(): ActionInterval? {
        return EaseExponentialInOut.create(getDirector(), _inner?.reverse())
    }
}