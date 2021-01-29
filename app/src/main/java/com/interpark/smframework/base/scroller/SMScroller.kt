package com.brokenpc.smframework.base.scroller

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView.Companion.smoothInterpolate
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.base.types.Ref

open class SMScroller : Ref {

    constructor(director: IDirector) : super(director) {
        _director = director
        _scrollSpeed = 0f
        _cellSize = 0f
        _scrollMode = ScrollMode.BASIC
        onAlignCallback = null
        reset()
    }

    enum class STATE {
        STOP, SCROLL, FLING
    }

    enum class ScrollMode {
        BASIC, PAGER, ALIGNED
    }

    open fun reset() {
        _state = STATE.STOP
        _newPosition = 0f
        _position = _newPosition
        _minPosition = 0f
        _maxPosition = 0f
        _hangSize = 0f
    }

    open fun update(): Boolean {
        var updated = false
        updated = updated or runScroll()
        updated = updated or runFling()
        val ret = smoothInterpolate(
            _position,
            _newPosition - _hangSize,
            AppConst.Config.TOLERANCE_POSITION
        )
        updated = updated or ret.retB
        _position = ret.retF
        return updated
    }

    protected var _state = STATE.STOP

    open fun getState(): STATE? {
        return _state
    }

    protected var _windowSize = 0.0f
    protected var _maxPosition = 0.0f
    protected var _minPosition = 0.0f

    open fun setWindowSize(windowSize: Float) {
        _windowSize = windowSize
        _maxPosition = windowSize
        if (_windowSize <= 0) {
            _windowSize = 1f
        }
    }

    protected var _timeStart = 0.0f
    protected var _timeDuration = 0.0f


    open fun getWindowSize(): Float {
        return _windowSize
    }

    open fun setScrollSize(scrollSize: Float) {
        _maxPosition = if (scrollSize > _windowSize) {
            _minPosition + scrollSize - _windowSize + _hangSize
        } else {
            _minPosition
        }
    }

    open fun getScrollSize(): Float {
        return _maxPosition - _minPosition
    }

    open fun setScrollPosition(position: Float) {
        setScrollPosition(position, true)
    }

    open fun setScrollPosition(position: Float, immediate: Boolean) {
        if (immediate) {
            _position = position
            _newPosition = _position
        } else {
            _newPosition = position
        }
        _state = STATE.STOP
    }

    protected var _position = 0.0f
    open fun getScrollPosition(): Float {
        return _position - _minPosition
    }

    protected var _newPosition = 0.0f
    open fun getNewScrollPosition(): Float {
        return _newPosition - _minPosition
    }

    protected var _lastPosition = 0.0f

    protected var _startPos = 0.0f
    open fun setStartPosition(startPos: Float) {
        _startPos += startPos
    }

    protected var _stopPos = 0.0f

    open fun getStartPosition(): Float {
        return _startPos
    }

    var SCROLL_TIME = 0.2f
    var GRAVITY = 9.8f * 1000
    open fun _SIGNUM(x: Float): Int {
        return if (x > 0) 1 else if (x < 0) -1 else 0
    }

    var SPEED_LOW = 100.0f

    open fun isTouchable(): Boolean {
        return _state == STATE.STOP || _scrollSpeed < SPEED_LOW
    }

    open fun decPrecesion(value: Float): Float {
        return decPrecesion(value, false)
    }

    open fun decPrecesion(value: Float, isNew: Boolean): Float {
        return if (isNew) {
            Math.round(value * 100) / 100.0f
        } else {
            value
        }
    }

    protected var _scrollSpeed = 0.0f
    open fun getScrollSpeed(): Float {
        return _scrollSpeed
    }

    open fun justAtLast() {}

    open fun onTouchDown() {
        onTouchDown(0)
    }

    open fun onTouchDown(param: Int) {}

    open fun onTouchUp() {
        onTouchUp(0)
    }

    open fun onTouchUp(param: Int) {}

    open fun onTouchScroll(delta: Float) {
        onTouchScroll(delta, 0)
    }

    open fun onTouchScroll(delta: Float, param: Int) {}

    protected var _velocity = 0.0f
    protected var _accelate = 0.0f
    protected var _touchDistance = 0.0f


    open fun onTouchFling(velocity: Float) {
        onTouchFling(velocity, 0)
    }

    open fun onTouchFling(velocity: Float, param: Int) {}

    protected var _hangSize = 0.0f
    open fun setHangSize(size: Float) {
        _hangSize = size
    }

    open fun scrollBy(distance: Float) {}

    protected var _scrollMode = ScrollMode.BASIC
    open fun setScrollMode(mode: ScrollMode) {
        _scrollMode = mode
    }

    protected var _cellSize = 0.0f
    open fun setCellSize(cellSize: Float) {
        _cellSize = cellSize
    }

    open fun getCellSize(): Float {
        return _cellSize
    }

    interface ALIGN_CALLBACK {
        fun onAlignCallback(aligned: Boolean)
    }

    var onAlignCallback: ALIGN_CALLBACK? = null

    open fun getMaxPageNo(): Int {
        val maxPageNo: Int
        maxPageNo = Math.ceil(getScrollSize() / _cellSize.toDouble()).toInt()
        return maxPageNo
    }

    protected open fun runFling(): Boolean {
        return false
    }

    protected open fun runScroll(): Boolean {
        return false
    }


    open fun clone(scroller: SMScroller) {
        _state = scroller._state
        _position = scroller._position
        _newPosition = scroller._newPosition
        _windowSize = scroller._windowSize
        _minPosition = scroller._minPosition
        _maxPosition = scroller._maxPosition
        _timeStart = scroller._timeStart
        _timeDuration = scroller._timeDuration
        _startPos = scroller._startPos
        _velocity = scroller._velocity
        _accelate = scroller._accelate
        _touchDistance = scroller._touchDistance
        _hangSize = scroller._hangSize
    }
}