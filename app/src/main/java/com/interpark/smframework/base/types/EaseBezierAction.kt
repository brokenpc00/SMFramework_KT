package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.ActionEase
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.util.tweenfunc

class EaseBezierAction(director: IDirector) : ActionEase(director) {

    protected var _p0 = 0f
    protected var _p1 = 0f
    protected var _p2 = 0f
    protected var _p3 = 0f

    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): EaseBezierAction? {
            val ease = EaseBezierAction(director)
            if (ease.initWithAction(action)) {
                return ease
            }

            return null
        }
    }

    override fun Clone(): ActionInterval? {
        if (_inner!=null) {
            val ret = EaseBezierAction.create(getDirector(), _inner?.Clone())
            ret?.setBezierParameter(_p0, _p1, _p2, _p3)
            return ret
        }

        return null
    }

    override fun update(time: Float) {
        _inner?.update(tweenfunc.bezieratFunction(_p0, _p1, _p2, _p3, time))
    }

    override fun reverse(): ActionInterval? {
        val reverseAction = EaseBezierAction.create(getDirector(), _inner?.reverse())
        if (reverseAction!=null) {
            reverseAction.setBezierParameter(_p3, _p2, _p1, _p0)
            return reverseAction
        }

        return null
    }

    fun setBezierParameter(p0: Float, p1: Float, p2: Float, p3: Float) {
        _p0 = p0
        _p1 = p1
        _p2 = p2
        _p3 = p3
    }
}