package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMView

open class ActionEase(director:IDirector) : ActionInterval(director) {
    protected var _inner:ActionInterval? = null

    fun getInnerAction():ActionInterval? {return _inner}

    override fun startWithTarget(target: SMView?) {
        if (target != null && _inner != null) {
            super.startWithTarget(target)
            _inner!!.startWithTarget(_target)
        }
    }

    override fun stop() {
        if (_inner != null) {
            _inner!!.stop()
        }
        super.stop()
    }

    override fun update(time: Float) {
        _inner!!.update(time)
    }

    open fun initWithAction(action: ActionInterval?): Boolean {
        if (action == null) {
            return false
        }
        if (super.initWithDuration(action.getDuration())) {
            _inner = action
            return true
        }
        return false
    }
}