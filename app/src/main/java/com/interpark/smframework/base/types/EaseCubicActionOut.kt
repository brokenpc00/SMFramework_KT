package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.tweenfunc.Companion.cubicEaseOut

class EaseCubicActionOut(director:IDirector) : ActionEase(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, action:ActionInterval):EaseCubicActionOut? {
            val ease:EaseCubicActionOut = EaseCubicActionOut(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): EaseCubicActionOut? {
        return if (_inner != null) {
            create(getDirector(), _inner!!.Clone()!!)
        } else null
    }

    override fun update(time: Float) {
        _inner!!.update(cubicEaseOut(time))
    }

    override fun reverse(): ActionEase? {
        return EaseCubicActionIn.create(getDirector(), _inner!!.reverse()!!)
    }
}