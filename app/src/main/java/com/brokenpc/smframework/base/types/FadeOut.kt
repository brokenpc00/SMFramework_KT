package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval

class FadeOut(director: IDirector) : FadeTo(director) {
    private var _reverseAction:FadeTo? = null
    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float): FadeOut? {
            val fade = FadeOut(director)
            fade.initWithDuration(duration, 0f)
            return fade
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)

        _toAlpha = (_reverseAction as FadeOut?)?._fromAlpha ?: 0f
        _fromAlpha = target?.getAlpha() ?: 1f
    }

    override fun Clone(): ActionInterval? {
        return FadeOut.create(getDirector(), _duration)
    }

    override fun reverse(): ActionInterval? {
        val fade = FadeIn.create(getDirector(), _duration)
        fade.setReverseAction(this)
        return fade
    }

    fun setReverseAction(ac: FadeTo) {
        _reverseAction = ac
    }
}