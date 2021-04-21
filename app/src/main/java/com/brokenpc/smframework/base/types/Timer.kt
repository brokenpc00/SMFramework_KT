package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector

open class Timer(director: IDirector) : Ref(director) {

    protected var _scheduler:Scheduler? = null
    protected var _elapsed:Float = -1.0f
    protected var _runForever:Boolean = false
    protected var _useDelay:Boolean = false
    protected var _timesExecuted:Long = 0
    protected var _repeat:Long = 0
    protected var _delay:Float = 0.0f
    protected var _interval:Float = 0.0f
    protected var _abort:Boolean = false


    fun setupTimerWithInterval(seconds:Float, repeat:Long, delay:Float) {
        _elapsed = -1.0f;
        _interval = seconds;
        _delay = delay;
        _useDelay = _delay>0.0f
        _repeat = repeat;

        _runForever = _repeat==UInt.MAX_VALUE.toLong()
        _timesExecuted = 0
    }

    fun setAborted() {_abort = true}
    fun isAboared():Boolean {return _abort}
    fun isExhausted():Boolean {return !_runForever && _timesExecuted>_repeat}

    open fun trigger(dt:Float) {}
    open fun cancel() {}

    open fun update(dt: Float) {
        if (_elapsed==-1.0f) {
            _elapsed = 0.0f
            _timesExecuted = 0
            return
        }

        _elapsed += dt

        if (_useDelay) {
            if (_elapsed < _delay) {
                return
            }
            _timesExecuted += 1
            trigger(_delay)
            _elapsed -= _delay
            _useDelay = false

            if (isExhausted()) {
                cancel()
                return
            }
        }

        val interval:Float = if (_interval>0) _interval else _elapsed
        while ((_elapsed>=interval) && !_abort) {
            _timesExecuted += 1
            trigger(interval)
            _elapsed -= interval

            if (isExhausted()) {
                cancel()
                break
            }

            if (_elapsed<=0.0f) {
                break
            }
        }
    }
}