package com.brokenpc.smframework.base.scroller

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView.Companion.smoothInterpolate

open class FlexibleScroller : SMScroller {
    protected var _controller:ScrollController? = null
    protected var _autoScroll:Boolean = false

    constructor(director:IDirector) : super(director) {
        _controller = ScrollController(director)
        reset()
    }

    override fun update(): Boolean {
        var updated = _controller!!.update()
        updated = updated or onAutoScroll()
        _newPosition = decPrecesion(_controller!!.getPanY())
        val ret = smoothInterpolate(_position, _newPosition, 0.1f)
        updated = updated or ret.retB
        _position = ret.retF
        _scrollSpeed = Math.abs(_lastPosition - _position) * 60
        _lastPosition = _position
        _state = if (!updated) {
            STATE.STOP
        } else {
            STATE.SCROLL
        }
        return updated
    }

    override fun setWindowSize(windowSize: Float) {
        super.setWindowSize(windowSize)
        _controller!!.setViewSize(windowSize)
    }

    override fun setScrollSize(scrollSize: Float) {
        super.setScrollSize(scrollSize)
        _controller!!.setScrollSize(scrollSize)
    }

    override fun setScrollPosition(position: Float, immediate: Boolean) {
        super.setScrollPosition(position, immediate)
        _controller!!.setPanY(position)
    }

    override fun getScrollPosition(): Float {
        return _position
    }

    override fun reset() {
        super.reset()
        if (_controller == null) {
            _controller = ScrollController(_director!!)
        }
        _controller!!.reset()
        _position = 0f
        _newPosition = 0f
        _scrollSpeed = 0f
        _autoScroll = false
    }

    override fun justAtLast() {
        _controller!!.stopIfExceedLimit()
    }

    override fun onTouchDown(unused: Int) {
        _autoScroll = false
        _controller!!.stopFling()
    }

    override fun onTouchUp(unused: Int) {
        _controller!!.startFling(0f)
    }

    override fun onTouchScroll(delta: Float, unused: Int) {
        _controller!!.pan(-delta)
    }

    override fun onTouchFling(velocity: Float, unused: Int) {
        _controller!!.startFling(-velocity)
    }

    open fun scrollTo(position: Float) {
        scrollToWithDuration(position, -1f)
    }

    open fun scrollToWithDuration(position: Float, duration: Float) {
        var position = position
        if (position < _minPosition) {
            position = _minPosition
        }
        if (Math.abs(position - _newPosition) < 1) return
        onTouchDown()
        val PIXELS_PER_SEC = 20000.0f
        val dist = position - _newPosition
        val dir = _SIGNUM(dist).toFloat()
        _timeDuration = if (duration <= 0) {
            Math.min(
                1.5f,
                Math.max(0.15f, Math.abs(dist) / PIXELS_PER_SEC)
            )
        } else {
            duration
        }
        _accelate = -dir * GRAVITY

        // decelerate
        // duration 동안 감속
        _velocity = -_accelate * _timeDuration
        val dist0 =
            _velocity * _timeDuration + 0.5f * _accelate * _timeDuration * _timeDuration
        val scale = dist / dist0
        _velocity *= scale
        _accelate *= scale
        _startPos = _newPosition
        _timeStart = _director!!.getGlobalTime()
        _autoScroll = true
    }

    open fun onAutoScroll(): Boolean {
        if (!_autoScroll) {
            return false
        }
        val globalTime = _director!!.getGlobalTime()
        var nowTime = globalTime - _timeStart
        if (nowTime > _timeDuration) {
            nowTime = _timeDuration
            _autoScroll = false
        }
        val oldPosition = _newPosition
        val distance = _velocity * nowTime + 0.5f * _accelate * nowTime * nowTime
        var newPosition = decPrecesion(_startPos + distance)
        val dir = _SIGNUM(_velocity).toFloat()
        val maxScrollWindowSize = _windowSize * 0.9f
        if (Math.abs(newPosition - oldPosition) > maxScrollWindowSize) {

            // 한꺼번에 확확 넘어가는걸 방지
            // 한 프레임에 스크롤이 화면의 90%가 넘지 않도록 계산
            // 2차 방정식 근의 공식 Ax^2+Bx+C=0.
            //  x=-b+or-sqrt(b^2-4*a*c)/2*a

            // x = now
            // a = 0.5*_accelate
            // b = -_velocity
            // c = newDistance
            val newPosition2 = oldPosition + dir * maxScrollWindowSize
            val newDistance = newPosition2 - _startPos
            val a = 0.5f * _accelate
            val b = -_velocity
            val discriminant = b * b - 4 * a * newDistance.toDouble()
            val newNowTime: Float
            val bUserMath = true
            newNowTime = if (bUserMath) {
                if (discriminant > 0) {
                    (-b + Math.sqrt(discriminant).toFloat()) / (2 * a)
                } else if (discriminant == 0.0) {
                    -b / (2 * a)
                } else {
                    (-b - Math.sqrt(discriminant).toFloat()) / (2 * a)
                }
            } else {
                // ???
                (-b - dir * Math.sqrt(b * b - 4 * a * newDistance.toDouble()).toFloat()) / (2 * a)
            }
            _timeStart = globalTime - newNowTime
            newPosition = newPosition2
            newPosition = decPrecesion(newPosition)
        }
        if (newPosition > _maxPosition) {
            _controller!!.setPanY(_maxPosition)
        } else if (newPosition < _minPosition) {
            _controller!!.setPanY(_minPosition)
        } else {
            _controller!!.setPanY(newPosition, true)
        }
        return true
    }

    override fun setHangSize(size: Float) {
        _controller!!.setHangSize(size)
    }
}