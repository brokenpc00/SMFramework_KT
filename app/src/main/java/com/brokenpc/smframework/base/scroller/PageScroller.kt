package com.brokenpc.smframework.base.scroller

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView.Companion.M_PI_2
import com.brokenpc.smframework.base.SMView.Companion.smoothInterpolate

class PageScroller(director:IDirector) : FlexibleScroller(director) {

    private var _bounceBackEnabled:Boolean = false
    fun setBounceBackEnable(enable: Boolean) {
        _bounceBackEnabled = enable
    }

    interface PAGE_CALLBACK {
        fun pageChangedCallback(page: Int)
    }

    var pageChangedCallback: PAGE_CALLBACK? = null

    fun setCurrentPage(page: Int) {
        setCurrentPage(page, true)
    }

    fun setCurrentPage(page: Int, immediate: Boolean) {
        _newPosition = _cellSize * page
        _controller!!.setPanY(_newPosition)
        if (immediate) {
            _position = _newPosition
        }
    }

    override fun reset() {
        super.reset()
        _controller!!.reset()
        _position = 0f
        _newPosition = 0f
    }

    override fun update(): Boolean {
        var updagted = _controller!!.update()
        updagted = updagted or runScroll()
        _newPosition = decPrecesion(_controller!!.getPanY(), true)
        if (!_bounceBackEnabled) {
            if (_newPosition < 0) {
                _newPosition = 0f
                _controller!!.setPanY(0f)
            } else if (_newPosition > getScrollSize()) {
                _newPosition = getScrollSize()
                _controller!!.setPanY(getScrollSize())
            }
        }
        val ret = smoothInterpolate(_position, _newPosition, 0.1f)
        updagted = updagted or ret.retB
        _position = ret.retF
        _scrollSpeed = Math.abs(_lastPosition - _position) * 60
        _lastPosition = _position
        return updagted
    }

    override fun onTouchUp(unused: Int) {
        var page = _newPosition / _cellSize
        val maxPageNo = getMaxPageNo()
        page = if (page < 0) {
            0f
        } else if (page > maxPageNo) {
            maxPageNo.toFloat()
        } else {
            val ipage = Math.floor(page.toDouble()).toInt()
            val offset = page - ipage
            if (offset <= 0.5f) {
                ipage.toFloat()
            } else {
                ipage + 1.toFloat()
            }
        }
        _startPos = _newPosition
        _stopPos = page * _cellSize
        if (_startPos <= 0 && page == 0f || _startPos >= _cellSize * maxPageNo && page == maxPageNo.toFloat() || _startPos == _stopPos) {
            // 첫페이지 또는 마지막 페이지 초기화
            _controller!!.startFling(0f)
            if (pageChangedCallback != null) {
                pageChangedCallback!!.pageChangedCallback(page.toInt())
            }
            return
        }

        // 페이지 스크롤
        _state = STATE.SCROLL
        _timeStart = _director!!.getGlobalTime()
        val distance = Math.abs(_startPos - _stopPos)
        _timeDuration = 0.05f + 0.15f * (1.0f - distance / _cellSize)
    }

    override fun onTouchFling(velocity: Float, currentPage: Int) {
        var v = velocity
        val maxVelocity = 15000f
        if (Math.abs(velocity) > maxVelocity) {
            v = _SIGNUM(v) * maxVelocity
        }
        val position:Float = _newPosition
        if (position < _minPosition || position > _maxPosition) {
            onTouchUp()
            return
        }
        val maxPageNo = getMaxPageNo()
        var page: Int
        page = if (v < 0) {
            currentPage + 1
        } else {
            currentPage - 1
        }
        if (page < 0) {
            page = 0
        } else if (page > maxPageNo) {
            page = maxPageNo
        }
        _startPos = _newPosition
        _stopPos = page * _cellSize
        _state = STATE.SCROLL
        _timeStart = _director!!.getGlobalTime()
        _timeDuration = 0.05f + 0.15f * (1.0f + Math.abs(v) / maxVelocity)
    }

    override fun runScroll(): Boolean {
        if (_state !== STATE.SCROLL) {
            return false
        }
        val dt = _director!!.getGlobalTime() - _timeStart
        val rt = dt / _timeDuration
        if (rt < 1) {
            val f = 1 - Math.sin(rt * M_PI_2).toFloat()
            val newPosition =
                decPrecesion(_stopPos + f * (_startPos - _stopPos), true)
            _controller!!.setPanY(newPosition)
        } else {
            _state = STATE.STOP
            _controller!!.setPanY(_stopPos)
            if (pageChangedCallback != null) {
                val value =
                    Math.floor(_stopPos / _cellSize.toDouble()).toFloat()
                pageChangedCallback!!.pageChangedCallback(value.toInt())
            }
        }
        return true
    }
}