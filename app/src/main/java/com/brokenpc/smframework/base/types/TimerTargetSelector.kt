package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector

open class TimerTargetSelector(director: IDirector) : Timer(director) {

    protected var _target:Ref? = null
    protected var _selecttor:SEL_SCHEDULE? = null

    fun initWithSelector(scheduler: Scheduler, selector: SEL_SCHEDULE, target: Ref, seconds:Float, repeat:Long, delay:Float): Boolean {
        _scheduler = scheduler
        _selecttor = selector
        _target = target
        setupTimerWithInterval(seconds, repeat, delay)
        return true
    }

    fun getSelector():SEL_SCHEDULE {return _selecttor!!}

    override fun trigger(dt: Float) {
        if (_target!=null) {
            _selecttor?.scheduleSelector(dt)
        }
    }

    override fun cancel() {
        _scheduler?.unschedule(_selecttor!!, _target!!)
    }
}