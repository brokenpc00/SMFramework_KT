package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseQuadraticActionOut(director: IDirector) : ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseQuadraticActionOut? {
            val ease = EaseQuadraticActionOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseQuadraticActionOut.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.quadraticOut(time))
    }

    override fun reverse(): ActionInterval? {
        return EaseQuadraticActionIn.create(getDirector(), _inner?.reverse())
    }
}