package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseElasticOut(director: IDirector): EaseElastic(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseElasticOut? {
            return create(director, action, 0.3f)
        }
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?, period: Float): EaseElasticOut? {
            val ease = EaseElasticOut(director)
            if (ease.initWithAction(action, period)) {
                return ease
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        return EaseElasticOut.create(getDirector(), _inner?.Clone(), _period)
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.elasticEaseOut(time, _period))
    }

    override fun reverse(): ActionInterval? {
        return EaseElasticIn.create(getDirector(), _inner?.reverse(), _period)
    }
}