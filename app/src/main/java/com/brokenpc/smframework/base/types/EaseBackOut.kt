package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseBackOut(director: IDirector) : ActionEase(director) {

    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseBackOut? {
            val ease = EaseBackOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseBackOut.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.backEaseOut(time))
    }

    override fun reverse(): ActionInterval? {
        return EaseBackIn.create(getDirector(), _inner?.reverse())
    }
}