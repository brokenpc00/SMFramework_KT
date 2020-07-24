package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.types.tweenfunc.Companion.sineEaseIn

class EaseSineIn(director:IDirector) : EaseRateAction(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval): EaseSineIn? {
            val ease:EaseSineIn = EaseSineIn(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): EaseSineIn? {
        return if (_inner != null) {
            create(getDirector(), _inner!!.Clone()!!)
        } else null
    }

    override fun update(time: Float) {
        _inner!!.update(sineEaseIn(time))
    }

    override fun reverse(): EaseRateAction? {
        return EaseSineOut.create(getDirector(), _inner!!.reverse()!!)
    }

}