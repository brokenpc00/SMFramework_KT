package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.EaseRateAction
import com.brokenpc.smframework.util.tweenfunc

class EaseIn(director: IDirector): EaseRateAction(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?, rate: Float): EaseIn? {
            val ease = EaseIn(director)
            if (ease.initWithAction(action, rate)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseIn.create(getDirector(), _inner?.Clone(), _rate)
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.easeIn(time, _rate))
    }

    override fun reverse(): ActionInterval? {
        return EaseIn.create(getDirector(), _inner?.reverse(), _rate)
    }
}