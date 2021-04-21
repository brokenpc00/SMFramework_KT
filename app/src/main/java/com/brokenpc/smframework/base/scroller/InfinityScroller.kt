package com.brokenpc.smframework.base.scroller

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.util.tweenfunc.Companion.cubicEaseOut
import kotlin.math.abs
import kotlin.math.min

class InfinityScroller(director:IDirector) : SMScroller(director) {
    override fun onTouchDown(param: Int) {
        _state = STATE.STOP
        _startPos = _newPosition
        _touchDistance = 0f
    }

    override fun onTouchUp(param: Int) {
        _state = STATE.STOP

        if (_scrollMode!=ScrollMode.BASIC) {
            // nearest position at scroll stop
            val r:Float = _newPosition % _cellSize
            var distance:Float = 0f

            distance = if (abs(r) <= _cellSize/2) -r else if (r>=0) _cellSize - r else -(_cellSize + r)
            if (distance==0f) {
                onAlignCallback?.onAlignCallback(true)
            } else {
                _state = STATE.FLING
                val dir:Float = _SIGNUM(distance).toFloat()
                _accelate = dir*GRAVITY
                _startPos = _newPosition
                _timeDuration = abs(distance) * 0.25f / 1000f

                _startPos = _newPosition
                _velocity = distance / _timeDuration - 0.5f * _accelate * _timeDuration
                _stopPos = _startPos + distance
            }
        }
    }

    override fun onTouchScroll(delta: Float, param: Int) {
        _touchDistance -= delta
        _newPosition = decPrecesion(_startPos + _touchDistance)

        if (_scrollMode!=ScrollMode.BASIC) {
            onAlignCallback?.onAlignCallback(false)
        }
    }

    override fun onTouchFling(velocity: Float, param: Int) {
        val dir = _SIGNUM(velocity)
        var v0 = abs(velocity)

        val maxVelocity = 25000f
        v0 = min(velocity, v0)

        // time of stop
        _timeDuration = v0 / GRAVITY

        _state = STATE.FLING
        _startPos = _newPosition

        _velocity = -dir*v0
        _accelate = dir*GRAVITY

        _timeStart = _director!!.getGlobalTime()
        var distance = _velocity * _timeDuration + 0.5f * _accelate * _timeDuration * _timeDuration

        if (_scrollMode==ScrollMode.PAGER) {
            if (abs(distance) > _cellSize) {
                distance = SMView.signum(distance) * _cellSize
            }
        }

        _stopPos = _startPos + distance

        if (_scrollMode!=ScrollMode.BASIC) {
            // nearest at scroll stop
            val r = _stopPos % _cellSize
            if (abs(r)<=_cellSize/2) {
                if (r>=0) distance -= r
                else distance += -r
            } else {
                if (r>=0) distance += _cellSize -r
                else distance -= _cellSize + r
            }

            _velocity = distance / _timeDuration - 0.5f * _accelate * _timeDuration
        }
    }


    fun scrollByWithDuration(distance:Float, duration:Float) {
        _state = STATE.SCROLL
        _stopPos = _startPos + distance

        _timeStart = _director!!.getGlobalTime()
        _timeDuration = duration

        if (_scrollMode != ScrollMode.BASIC) {
            onAlignCallback?.onAlignCallback(false)
        }
    }

    override fun runFling(): Boolean {
        if (_state!=STATE.FLING) {
            return false
        }

        val globalTime = _director!!.getGlobalTime()
        val nowTime = globalTime - _timeStart
        return if (nowTime>_timeDuration) {
            _newPosition = _stopPos
            _state = STATE.STOP

            // stop
            if (_scrollMode != ScrollMode.BASIC) {
                onAlignCallback?.onAlignCallback(true)
            }
            false
        } else {
            val distance = _velocity * nowTime + 0.5f * _accelate * nowTime * nowTime
            _newPosition = decPrecesion(_startPos + distance)
            true
        }
    }

    override fun runScroll(): Boolean {
        if (_state!=STATE.SCROLL) {
            return false
        }

        val dt = _director!!.getGlobalTime()-_timeStart
        var t = dt / _timeDuration

        return if (t<1) {
            t = cubicEaseOut(t)
            true
        } else {
            _state = STATE.STOP
            _newPosition = _stopPos

            if (_scrollMode!=ScrollMode.BASIC) {
                onAlignCallback?.onAlignCallback(true)
            }
            false
        }
    }
}