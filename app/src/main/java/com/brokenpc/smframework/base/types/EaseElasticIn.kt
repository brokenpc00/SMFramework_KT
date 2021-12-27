package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseElasticIn(director: IDirector) : EaseElastic(director) {
    companion object {
        @JvmStatic
        fun creeate(director: IDirector, action: ActionInterval?): EaseElasticIn? {
            return create(director, action, 0.3f)
        }

        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?, period: Float): EaseElasticIn? {
            val ease = EaseElasticIn(director)
            if (ease.initWithAction(action, period)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseElasticIn.create(getDirector(), _inner?.Clone(), _period)
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.elasticEaseIn(time, _period))
    }

    override fun reverse(): ActionInterval? {
        return EaseElasticOut.create(getDirector(), _inner?.reverse(), _period)
    }
}