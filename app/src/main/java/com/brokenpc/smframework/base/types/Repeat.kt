package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInstant
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.FiniteTimeAction
import kotlin.math.abs

class Repeat(director: IDirector) : ActionInterval(director) {
    protected var _times = 0
    protected var _total = 0
    protected var _nextDt = 0f
    protected var _actionInstant = false
    protected var _innerAction:FiniteTimeAction? = null

    companion object {
        fun create(director: IDirector, action: FiniteTimeAction?, times: Int): Repeat? {
            val repeat = Repeat(director)
            if (repeat.initWithAction(action, times)) {
                return repeat
            }

            return null
        }
    }

    fun setInnerAction(action: FiniteTimeAction) {
        if (_innerAction!=action) {
            _innerAction = action
        }
    }

    override fun Clone(): ActionInterval? {
        return Repeat.create(getDirector(), _innerAction?.Clone(), _times)
    }

    override fun reverse(): ActionInterval? {
        return Repeat.create(getDirector(), _innerAction?.reverse(), _times)
    }

    override fun startWithTarget(target: SMView?) {
        _total = 0
        _nextDt = _innerAction?.getDuration() ?: 0f / _duration
        super.startWithTarget(target)
        _innerAction?.startWithTarget(target)
    }

    override fun stop() {
        _innerAction?.stop()
        super.stop()
    }

    override fun update(dt: Float) {
        if (dt > _nextDt) {
            while (dt >= _nextDt && _total < _times) {
                _innerAction?.update(1f)
                _total++

                _innerAction?.stop()
                _innerAction?.startWithTarget(_target)
                _nextDt = _innerAction?.getDuration() ?: 0f / _duration * (_total+1)
            }

            if (abs(dt-1f) < EPSILON && _total<_times) {
                _innerAction?.update(1f)
                _total++
            }
            if (!_actionInstant) {
                if (_total==_times) {
                    _innerAction?.stop()
                } else {
                    _innerAction?.update(dt - (_nextDt - (_innerAction?.getDuration()?:0f/_duration)))
                }
            }
        } else {
            _innerAction?.update(dt*_times % 1f)
        }
    }

    override fun isDone(): Boolean {
        return _total==_times
    }

    protected fun initWithAction(a: FiniteTimeAction?, times: Int): Boolean {
        val d = a?.getDuration()?:0f * times
        if (super.initWithDuration(d)) {
            _times = times
            _innerAction = a

            _actionInstant = a is ActionInstant

            _total = 0
            return true
        }

        return false
    }
}