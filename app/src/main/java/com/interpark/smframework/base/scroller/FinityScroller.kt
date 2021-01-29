package com.brokenpc.smframework.base.scroller

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.util.tweenfunc.Companion.cubicEaseOut
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min

class FinityScroller(director:IDirector) : FlexibleScroller(director) {
    override fun reset() {
        super.reset()
        _controller!!.reset()
        _position = 0f
        _newPosition = 0f
    }

    override fun update(): Boolean {
        var updated:Boolean = _controller!!.update()
        updated = updated.or(runScroll())
        updated = updated.or(runFling())

        _newPosition = decPrecesion(_controller!!.getPanY(), true)

        val ret:SMView.InterpolateRet = SMView.smoothInterpolate(_position, _newPosition, 0.1f)
        updated = updated.or(ret.retB)
        _position = ret.retF

        _scrollSpeed = abs(_lastPosition - _position) * 60

        _lastPosition = _position

        return updated
    }

    override fun onTouchUp(unused: Int) {
        var page:Float = _newPosition / _cellSize
        val maxPageNo = getMaxPageNo()

        if (page<0) {
            page = 0f
        } else if (page > maxPageNo) {
            page = maxPageNo.toFloat()
        } else {
            val iPage:Int = floor(page).toInt()
            val offset:Float = page - iPage

            if (offset <= 0.5f) {
                page = iPage.toFloat()
            } else {
                page = (1+iPage).toFloat()
            }
        }

        _startPos = _newPosition
        _stopPos = page * _cellSize

        if ((_startPos<=0 && page==0f) || (_startPos>=_cellSize*maxPageNo && page==maxPageNo.toFloat()) || (_startPos==_stopPos)) {
            _controller!!.startFling(0f)

            onAlignCallback?.onAlignCallback(true)
        } else {
            _state = STATE.SCROLL

            _timeStart = _director!!.getGlobalTime()
            val distance = abs(_startPos - _stopPos)
            _timeDuration = 0.05f + 0.35f * (1f - distance / _cellSize)
        }
    }

    override fun onTouchFling(velocity: Float, unused: Int) {
        val _dir = _SIGNUM(velocity)
        var v0 = abs(velocity)

        val maxVelocity = 25000f
        v0 = min(velocity, v0)

        _stopPos = _newPosition

        // stop time
        _timeDuration = v0 / GRAVITY
        _velocity = -_dir*v0
        _accelate = _dir*GRAVITY
        _timeStart = _director!!.getGlobalTime()

        var distance = _velocity * _timeDuration + 0.5f * _accelate * _timeDuration * _timeDuration

        if (_startPos + distance < _minPosition) {
            if (_startPos<=0) {
                _state = STATE.STOP
                _controller!!.startFling(0f)
                onAlignCallback?.onAlignCallback(true)
            } else {
                distance = _minPosition - _startPos

                _state = STATE.SCROLL
                _stopPos = _minPosition
                _timeStart = _director!!.getGlobalTime()
                _timeDuration = 0.25f
            }
        } else if (_startPos + distance > _maxPosition) {
            if (_startPos>=_maxPosition) {
                _state = STATE.STOP
                _controller!!.startFling(0f)
                onAlignCallback?.onAlignCallback(true)
            } else {
                distance = _maxPosition - _startPos

                _state = STATE.SCROLL
                _stopPos = _maxPosition
                _timeStart = _director!!.getGlobalTime()
                _timeDuration = 0.25f
            }
        } else {
            _state = STATE.FLING
            _startPos += distance

            if (_scrollMode!=ScrollMode.BASIC) {
                // nearest position at scroll stop
                val r:Float = _stopPos % _cellSize
                if (abs(r)<=_cellSize/2) {
                    if (r>=0) distance -= r
                    else distance += - r
                } else {
                    if (r>=0) distance += _cellSize - r
                    else distance -= _cellSize + r
                }

                _velocity = distance / _timeDuration - 0.5f * _accelate * _timeDuration
                _stopPos = _startPos + distance
            }
        }
    }

    override fun setScrollSize(scrollSize: Float) {
        _maxPosition = _minPosition + scrollSize - _cellSize
        _controller!!.setScrollSize(scrollSize)
        _controller!!.setViewSize(_cellSize)
    }

    override fun setWindowSize(windowSize: Float) {
        super.setWindowSize(windowSize)
        _controller!!.setViewSize(_cellSize)
    }

    override fun runFling(): Boolean {
        if (_state!=STATE.FLING) {
            return false
        }

        val globalTime = _director!!.getGlobalTime()
        val nowTime = globalTime - _timeStart
        if (nowTime>_timeDuration) {
            _state = STATE.STOP

            _newPosition = _stopPos
            _controller!!.setPanY(_stopPos)
            // stop
            onAlignCallback?.onAlignCallback(true)
            return false
        } else {
            val distance = _velocity * nowTime + 0.5f * _accelate * nowTime * nowTime
            _newPosition = decPrecesion(_startPos + distance)
            _controller!!.setPanY(_newPosition)
            return true
        }
    }

    override fun runScroll(): Boolean {
        if (_state!=STATE.SCROLL) {
            return false
        }

        val dt = _director!!.getGlobalTime() - _timeStart
        var t:Float = dt / _timeDuration

        if (t<1) {
            t = cubicEaseOut(t)
            val interpolate:Float = _startPos + (_stopPos - _startPos) * t;
            _newPosition = decPrecesion(interpolate)
            _controller!!.setPanY(_newPosition)
            return true
        } else {
            _state = STATE.STOP
            _newPosition = _stopPos
            _controller!!.setPanY(_stopPos)

            onAlignCallback?.onAlignCallback(true)
            return false
        }
    }
}