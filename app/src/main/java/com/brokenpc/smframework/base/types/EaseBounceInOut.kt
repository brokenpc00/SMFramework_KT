package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseBounceInOut(director: IDirector) : ActionEase(director) {

    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseBounceInOut? {
            val ease = EaseBounceInOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseBounceInOut.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.bounceEaseInOut(time))
    }

    override fun reverse(): ActionInterval? {
        return EaseBounceInOut.create(getDirector(), _inner?.reverse())
    }
}