package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.EaseRateAction
import com.brokenpc.smframework.util.tweenfunc

class EaseOut(director: IDirector) : EaseRateAction(director) {
    companion object {
        fun create(director: IDirector, action: ActionInterval?, rate: Float): EaseOut? {
            val ease = EaseOut(director)

            if (ease.initWithAction(action, rate)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): EaseOut? {
        if (_inner!=null) {
            return EaseOut.create(getDirector(), _inner!!.Clone(), _rate)
        }
        return null
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.easeInOut(time, _rate))
    }

    override fun reverse(): ActionInterval? {
        return EaseOut.create(getDirector(), _inner?.reverse(), 1f/_rate)
    }
}