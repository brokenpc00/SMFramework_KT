package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval

class FadeIn(director: IDirector) : FadeTo(director) {
    private var _reverseAction:FadeTo? = null
    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float): FadeIn {
            val fade = FadeIn(director)
            fade.initWithDuration(duration, 1f)
            return fade
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)

        _toAlpha = (_reverseAction as FadeIn?)?._fromAlpha ?: 1f
        _fromAlpha = target?.getAlpha() ?: 1f
    }

    override fun Clone(): ActionInterval? {
        return FadeIn.create(getDirector(), _duration)
    }

    override fun reverse(): ActionInterval? {
        val fade = FadeOut.create(getDirector(), _duration)
        (fade as FadeOut).setReverseAction(this)
        return fade
    }

    fun setReverseAction(ac: FadeTo) {
        _reverseAction = ac
    }
}