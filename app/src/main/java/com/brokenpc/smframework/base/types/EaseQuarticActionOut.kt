package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseQuarticActionOut(director: IDirector) : ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseQuarticActionOut? {
            val ease = EaseQuarticActionOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseQuarticActionOut.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.quartEaseOut(time))
    }

    override fun reverse(): ActionInterval? {
        return EaseQuarticActionIn.create(getDirector(), _inner?.reverse())
    }
}