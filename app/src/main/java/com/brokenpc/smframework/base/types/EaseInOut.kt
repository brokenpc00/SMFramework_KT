package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.EaseRateAction
import com.brokenpc.smframework.util.tweenfunc

class EaseInOut(director: IDirector) : EaseRateAction(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?, rate: Float): EaseInOut? {
            val ease = EaseInOut(director)
            if (ease.initWithAction(action, rate)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseInOut.create(getDirector(), _inner?.Clone(), _rate)
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.easeInOut(time, _rate))
    }

    override fun reverse(): ActionInterval? {
        return EaseInOut.create(getDirector(), _inner?.reverse(), _rate)
    }
}