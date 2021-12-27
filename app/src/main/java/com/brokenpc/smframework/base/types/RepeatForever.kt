package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval

class RepeatForever(director: IDirector) : ActionInterval(director) {
    protected var _innerAction:ActionInterval? = null
    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?): RepeatForever? {
            val ret = RepeatForever(director)
            if (ret.initWithAction(action)) {
                return ret
            }

            return null
        }
    }

    protected fun initWithAction(action: ActionInterval?): Boolean {
        if (action==null) return false

        _innerAction = action
        return true
    }

    fun setInnerAction(action: ActionInterval?) {
        if (_innerAction!=action) {
            _innerAction = action
        }
    }

    override fun Clone(): ActionInterval? {
        return RepeatForever.create(getDirector(), _innerAction?.Clone())
    }

    override fun reverse(): ActionInterval? {
        return RepeatForever.create(getDirector(), _innerAction?.reverse())
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        _innerAction?.startWithTarget(target)
    }

    override fun step(dt: Float) {
        _innerAction?.step(dt)

        if (_innerAction!=null) {
            if (_innerAction!!.isDone() && _innerAction!!.getDuration()>0) {
                var diff = _innerAction!!.getElapsed() - _innerAction!!.getDuration()
                if (diff > _innerAction!!.getDuration()) {
                    diff %= _innerAction!!.getDuration()
                }

                _innerAction!!.startWithTarget(_target)

                _innerAction!!.step(0f)
                _innerAction!!.step(diff)
            }
        }
    }

    override fun isDone(): Boolean {
        return false
    }
}