package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.tweenfunc

class EaseQuadraticActionInOut(director: IDirector) : ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseQuadraticActionInOut? {
            val ease = EaseQuadraticActionInOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.quadraticInOut(time))
    }

    override fun reverse(): ActionInterval? {
        return create(getDirector(), _inner?.reverse())
    }
}