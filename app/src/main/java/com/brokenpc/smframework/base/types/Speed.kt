package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Action
import com.brokenpc.smframework.base.types.ActionInterval

class Speed(director: IDirector) : Action(director) {
    protected var _speed = 0f
    protected var _innerAction:ActionInterval? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, action: ActionInterval?, speed: Float): Speed {
            val ret = Speed(director)
            ret.initWithAction(action, speed)
            return ret
        }
    }

    override fun Clone(): Action? {
        if (_innerAction!=null) {
            return create(getDirector(), _innerAction?.Clone(), _speed)
        }
        return null
    }

    override fun startWithTarget(target: SMView?) {
        if (target!=null) {
            super.startWithTarget(target)
            _innerAction?.startWithTarget(target)
        }
    }

    override fun step(dt: Float) {
        _innerAction?.step(dt*_speed)
    }

    override fun reverse(): Action? {
        if (_innerAction!=null) {
            return create(getDirector(), _innerAction?.reverse(), _speed)
        }
        return null
    }

    override fun stop() {
        _innerAction?.stop()
        super.stop()
    }

    override fun isDone(): Boolean {
        return _innerAction?.isDone()?:true
    }

    fun getSpeed(): Float {return _speed}
    fun setSpeed(speed: Float) {_speed=speed}
    fun setInnerAction(action: ActionInterval?) {
        if (_innerAction!=action) {
            _innerAction?.stop()
            _innerAction = action
        }
    }

    protected fun initWithAction(action: ActionInterval?, speed: Float): Boolean {
        _innerAction = action
        _speed = speed
        return true
    }
}