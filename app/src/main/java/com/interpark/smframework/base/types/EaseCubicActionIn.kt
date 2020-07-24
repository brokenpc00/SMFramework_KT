package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.types.tweenfunc.Companion.cubicEaseIn

class EaseCubicActionIn(director:IDirector) : ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval): EaseCubicActionIn? {
            val ease:EaseCubicActionIn = EaseCubicActionIn(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): EaseCubicActionIn? {
        return if (_inner != null) {
            create(getDirector(), _inner!!.Clone()!!)
        } else null
    }

    override fun update(time: Float) {
        _inner!!.update(cubicEaseIn(time))
    }

    override fun reverse(): ActionEase? {
        return EaseCubicActionOut.create(getDirector(), _inner!!.reverse()!!)
    }

}