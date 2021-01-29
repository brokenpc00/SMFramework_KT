package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.tweenfunc.Companion.sineEaseOut

class EaseSineOut(director:IDirector) : EaseRateAction(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval): EaseSineOut? {
            val ease:EaseSineOut = EaseSineOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): EaseSineOut? {
        return if (_inner != null) {
            create(getDirector(), _inner!!.Clone()!!)
        } else null
    }


    override fun update(time: Float) {
        _inner!!.update(sineEaseOut(time))
    }

    override fun reverse(): EaseRateAction? {
        return EaseSineIn.create(getDirector(), _inner!!.reverse()!!)
    }

}