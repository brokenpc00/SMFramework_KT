package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseElasticInOut(director: IDirector): EaseElastic(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseElasticInOut? {
            return create(director, action, 0.3f)
        }
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?, period: Float): EaseElasticInOut? {
            val ease = EaseElasticInOut(director)
            if (ease.initWithAction(action, period)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseElasticInOut.create(getDirector(), _inner?.Clone(), _period)
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.elasticEaseInOut(time, _period))
    }

    override fun reverse(): ActionInterval? {
        return EaseElasticInOut.create(getDirector(), _inner?.reverse(), _period)
    }
}