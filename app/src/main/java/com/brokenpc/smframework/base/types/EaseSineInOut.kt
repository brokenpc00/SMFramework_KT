package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.EaseRateAction
import com.brokenpc.smframework.util.tweenfunc

class EaseSineInOut(director: IDirector) : EaseRateAction(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseSineInOut? {
            val ease = EaseSineInOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseSineInOut.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.sineEaseInOut(time))
    }

    override fun reverse(): ActionInterval? {
        return EaseSineInOut.create(getDirector(), _inner?.reverse())
    }
}