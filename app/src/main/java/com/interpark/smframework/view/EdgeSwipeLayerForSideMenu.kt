package com.brokenpc.smframework.view

import android.view.MotionEvent
import android.view.VelocityTracker
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.IDirector.SIDE_MENU_STATE
import com.brokenpc.smframework.base.scroller.PageScroller
import com.brokenpc.smframework.base.scroller.PageScroller.PAGE_CALLBACK
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst

class EdgeSwipeLayerForSideMenu(director:IDirector) : EdgeSwipeLayer(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, tag: Int, x: Float, y: Float, width: Float, height: Float): EdgeSwipeLayerForSideMenu {
            return create(director, tag, x, y, width, height, 0f, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, tag: Int, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): EdgeSwipeLayerForSideMenu {
            val layer:EdgeSwipeLayerForSideMenu = EdgeSwipeLayerForSideMenu(director)
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
        if (_scroller != null) {
            _scroller!!.setBounceBackEnable(false)
            _scroller!!.pageChangedCallback = object : PAGE_CALLBACK {
                override fun pageChangedCallback(page: Int) {
                    openStateChanged(page)
                }
            }
            return true
        }
        return false
    }


    override fun setSwipeWidth(width: Float) {
        super.setSwipeWidth(width)
        _scroller!!.setScrollPosition(_swipeSize)
        _openState = 0
    }

    fun open() {
        open(true)
    }

    fun open(immediate: Boolean) {
        if (immediate) {
            _scroller!!.setScrollPosition(0f)
            _openState = 0
        } else {
            scheduleUpdate()
            // ToDo. 좌측메뉴... 우측일경우 1
            _fakeFlingDirection = -1
            //            scheduleUpdate();
        }
        if (_velocityTracker != null) {
            _velocityTracker!!.clear()
        }
    }

    fun close() {
        close(true)
    }

    fun close(immediate: Boolean) {
        if (immediate) {
            _scroller!!.setScrollPosition(_swipeSize)
            _openState = 1
        } else {
            scheduleUpdate()
            _fakeFlingDirection = +1
        }
        if (_velocityTracker != null) {
            _velocityTracker!!.clear()
        }
    }

    override fun updateScrollPosition(dt: Float) {
        if (_fakeFlingDirection != 0) {
            val velocity = if (_fakeFlingDirection > 0) (-5000).toFloat() else +5000.toFloat()
            _scroller!!.onTouchFling(velocity, _openState)
            _fakeFlingDirection = 0
        }
        _scroller!!.update()
        val position:Float = _scroller!!.getScrollPosition()
        if (position != _curPosition) {
            _curPosition = position
            _director!!.setSideMenuOpenPosition(_swipeSize - _curPosition)
        }
    }

    override fun isScrollArea(worldPoint: Vec2): Boolean {
        return if (_openState == 0) {
            true
        } else super.isScrollArea(worldPoint)
    }

    fun closeComplete() {
        unscheduleUpdate()
    }

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
                val state = _director!!.getSideMenuState()
                if (state === SIDE_MENU_STATE.CLOSE) {
                    if (x < _edgeSize) {
                        _scrollEventTargeted = true
                    }
                } else {
                    if (x > _swipeSize - _curPosition) {
                        _scrollEventTargeted = true
                    }
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
                        val vx: Float
                        var vy: Float
                        vx = _velocityTracker!!.getXVelocity(0)
                        if (Math.abs(vx) > AppConst.Config.MIN_VELOCITY) {
                            _scroller!!.onTouchFling(vx, _openState)
                        } else {
                            _scroller!!.onTouchUp()
                        }
                        scheduleUpdate()
                    } else {
                        val state = _director!!.getSideMenuState()
                        if (state !== SIDE_MENU_STATE.CLOSE) {
                            _scroller!!.onTouchUp()
                        }
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
                        if (ax > AppConst.Config.SCROLL_TOLERANCE) {
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