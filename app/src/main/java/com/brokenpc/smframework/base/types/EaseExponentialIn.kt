package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseExponentialIn(director: IDirector): ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseExponentialIn? {
            val ease = EaseExponentialIn(director)
            if (ease.initWithAction(action)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseExponentialIn.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.expoEaseIn(time))
    }

    override fun reverse(): ActionInterval? {
        return EaseExponentialOut.create(getDirector(), _inner?.reverse())
    }
}