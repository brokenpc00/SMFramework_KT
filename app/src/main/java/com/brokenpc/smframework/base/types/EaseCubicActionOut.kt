package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.util.tweenfunc

class EaseCubicActionOut(director:IDirector) : ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action:ActionInterval?):EaseCubicActionOut? {
            val ease:EaseCubicActionOut = EaseCubicActionOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): EaseCubicActionOut? {
        return EaseCubicActionOut.create(getDirector(), _inner?.Clone())
    }

    override fun update(time: Float) {
        _inner!!.update(tweenfunc.cubicEaseOut(time))
    }

    override fun reverse(): ActionEase? {
        return EaseCubicActionIn.create(getDirector(), _inner!!.reverse()!!)
    }
}