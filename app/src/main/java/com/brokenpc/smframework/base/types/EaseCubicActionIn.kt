package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.tweenfunc.Companion.cubicEaseIn

class EaseCubicActionIn(director:IDirector) : ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseCubicActionIn? {
            val ease:EaseCubicActionIn = EaseCubicActionIn(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): EaseCubicActionIn? {
        return EaseCubicActionIn.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner!!.update(cubicEaseIn(time))
    }

    override fun reverse(): ActionEase? {
        return EaseCubicActionOut.create(getDirector(), _inner!!.reverse()!!)
    }

}