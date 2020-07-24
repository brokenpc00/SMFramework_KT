package com.interpark.smframework.view

import android.view.MotionEvent
import android.view.VelocityTracker
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.scroller.PageScroller
import com.interpark.smframework.base.scroller.PageScroller.PAGE_CALLBACK
import com.interpark.smframework.base.types.Vec2
import com.interpark.smframework.util.AppConst

class EdgeSwipeLayerForPushBack(director:IDirector) : EdgeSwipeLayer(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, tag: Int, x: Float, y: Float, width: Float, height: Float): EdgeSwipeLayerForPushBack {
            return create(director, tag, x, y, width, height, 0f, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, tag: Int, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): EdgeSwipeLayerForPushBack {
            val layer:EdgeSwipeLayerForPushBack = EdgeSwipeLayerForPushBack(director)
            layer.setAnchorPoint(anchorX, anchorY)
            layer.setContentSize(width, height)
            layer.setPosition(x, y)
            layer.init()
            return layer
        }
    }

    override fun init(): Boolean {
        super.init()
        _scroller = PageScroller(getDirector())
        _scrollEventTargeted = false
        if (_scroller != null) {
            _scroller!!.pageChangedCallback = object : PAGE_CALLBACK {
                override fun pageChangedCallback(page: Int) {
                    openStateChanged(page)
                }
            }
            return true
        }
        return false
    }


    fun back() {
        back(true)
    }

    fun back(immediate: Boolean) {
        if (immediate) {
            _scroller!!.setScrollPosition(_swipeSize)
        } else {
            scheduleUpdate()
            _fakeFlingDirection = 1
        }
        if (_velocityTracker != null) {
            _velocityTracker!!.clear()
        }
    }

    fun reset() {
        _scroller!!.setCurrentPage(1, true)
        _openState = 0
        _curPosition = 0f
        _inScrollEvent = false
        _scrollEventTargeted = false
        unscheduleUpdate()
    }

    interface SWIPTE_BACK_UPDATE_CALLBCK {
        fun onSwipeUpdate(a: Int, b: Float)
    }

    var _swipeUpdateCallback: SWIPTE_BACK_UPDATE_CALLBCK? = null


    override fun updateScrollPosition(dt: Float) {
        if (_fakeFlingDirection != 0) {
            val velocity = if (_fakeFlingDirection > 0) (-5000).toFloat() else +5000.toFloat()
            _scroller!!.onTouchFling(velocity, _openState)
            _fakeFlingDirection = 0
        }
        _scroller!!.update()
        _curPosition = _scroller!!.getScrollPosition() - _swipeSize
        if (_swipeUpdateCallback != null) {
            _swipeUpdateCallback!!.onSwipeUpdate(_openState, -_curPosition)
        }
    }

//    public int SMViewDispatchTouchEvent (MotionEvent e) {
//        return super.dispatchTouchEvent(e);
//    }

    //    public int SMViewDispatchTouchEvent (MotionEvent e) {
    //        return super.dispatchTouchEvent(e);
    //    }
    override fun dispatchTouchEvent(ev: MotionEvent): Int {
        val x = ev.x
        val y = ev.y
        if (_velocityTracker == null) {
            _velocityTracker = VelocityTracker.obtain()
        }
        val action = ev.action
        val point = Vec2(x, y)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                _scrollEventTargeted = false
                _inScrollEvent = false
                _lastMotionX = x
                _lastMotionY = y
                _firstMotionTime = getDirector().getGlobalTime()
                _scrollEventTargeted = if (Math.abs(_curPosition) as Int <= 1 && x < _edgeSize) {
                    true
                } else {
                    false
                }
                if (_scrollEventTargeted) {
                    _scroller!!.onTouchDown()
                    _velocityTracker!!.addMovement(ev)
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (_scrollEventTargeted) {
                    if (_inScrollEvent) {
                        _inScrollEvent = false
                        var vx = _velocityTracker!!.getXVelocity(0)
                        var vy = _velocityTracker!!.getYVelocity(0)
                        if (vx == 0f && vy == 0f) {
                            val dt = getDirector().getGlobalTime() - _firstMotionTime
                            if (dt > 0) {
                                val p1 = Vec2(_touchStartPosition)
                                val p2 = Vec2(_touchCurrentPosition)
                                vx = (p2.x - p1.x) / dt
                                vy = (p2.y - p1.y) / dt
                            }
                        }
                        if (Math.abs(vx) > 1000) {
                            _scroller!!.onTouchFling(vx, 1 - _openState)
                        } else {
                            _scroller!!.onTouchUp()
                        }
                        scheduleUpdate()
                    } else {
                        _scroller!!.onTouchUp()
                        scheduleUpdate()
                    }
                }
                _scrollEventTargeted = false
                _inScrollEvent = false
                _velocityTracker!!.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                if (_scrollEventTargeted) {
                    _velocityTracker!!.addMovement(ev)
                    val deltaX: Float
                    deltaX = if (!_inScrollEvent) {
                        x - _lastMotionX
                    } else {
                        point.x - _touchPrevPosition.x
                    }
                    if (!_inScrollEvent) {
                        val ax = Math.abs(x - _lastMotionX)
                        if (ax > AppConst.Config.SCROLL_TOLERANCE) { // 10보다움직였으면
                            _inScrollEvent = true
                        }
                        if (_inScrollEvent) {
                            if (_touchMotionTarget != null) {
                                cancelTouchEvent(_touchMotionTarget, ev)
                                _touchMotionTarget = null
                            }
                        }
                    }
                    if (_inScrollEvent) {
                        _scroller!!.onTouchScroll(+deltaX)
                        _lastMotionX = x
                        _lastMotionY = y
                        scheduleUpdate()
                    }
                }
            }
        }
        if (_inScrollEvent) {
            return TOUCH_INTERCEPT
        }
        if (action == MotionEvent.ACTION_UP) {
            return TOUCH_FALSE
        }
        return if (_scrollEventTargeted) TOUCH_TRUE else TOUCH_FALSE
    }
}