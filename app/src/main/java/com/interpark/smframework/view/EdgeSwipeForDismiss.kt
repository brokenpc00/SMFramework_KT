package com.interpark.smframework.view

import android.view.MotionEvent
import android.view.VelocityTracker
import com.interpark.smframework.IDirector
import com.interpark.smframework.base.scroller.PageScroller
import com.interpark.smframework.base.types.Vec2
import com.interpark.smframework.util.AppConst
import kotlin.math.abs

class EdgeSwipeForDismiss(director:IDirector) : EdgeSwipeLayer(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, tag: Int, x: Float, y: Float, width: Float, height: Float): EdgeSwipeForDismiss {
            return create(director, tag, x, y, width, height, 0f, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, tag: Int, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): EdgeSwipeForDismiss {
            val layer:EdgeSwipeForDismiss = EdgeSwipeForDismiss(director)
            layer.setAnchorPoint(anchorX, anchorY)
            layer.setPosition(x, y)
            layer.setContentSize(width, height)
            layer.init()
            return layer
        }
    }

    interface SWIPE_DISMISS_UPDATE_CALLBACK {
        fun onSwipeUpdate(a: Int, b: Float)
    }

    var _swipeUpdateCallback:SWIPE_DISMISS_UPDATE_CALLBACK? = null

    override fun init(): Boolean {
        super.init()

        _scroller = PageScroller(getDirector())
        _scrollEventTargeted = false
        _scroller!!.pageChangedCallback = object : PageScroller.PAGE_CALLBACK {
            override fun pageChangedCallback(page: Int) {
                openStateChanged(page)
            }
        }
        return true
    }

    fun back(immediate:Boolean = true) {
        if (immediate) {
            _scroller!!.setScrollPosition(_swipeSize)
        } else {
            scheduleUpdate()
            _fakeFlingDirection = 1
        }

        _velocityTracker?.clear()
    }

    fun reset() {
        _scroller!!.setCurrentPage(1, true)
        _openState = 0
        _curPosition = 0f

        _inScrollEvent = false
        _scrollEventTargeted = false

        unscheduleUpdate()
    }

    override fun updateScrollPosition(dt: Float) {
        if (_fakeFlingDirection!=0) {
            val velocity = if (_fakeFlingDirection>0) -5000f else 5000f
            _scroller!!.onTouchFling(velocity, _openState)
            _fakeFlingDirection = 0
        }

        _scroller!!.update()
        _curPosition = _scroller!!.getScrollPosition() - _swipeSize

        _swipeUpdateCallback?.onSwipeUpdate(_openState, -_curPosition)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Int {
        val x:Float = ev.x
        val y:Float = ev.y

        if (_velocityTracker==null) {
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

                if (_curPosition < 1 && y < _edgeSize) {
                    _scrollEventTargeted = true
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

                        if (vx==0f && vy==0f) {
                            val dt = getDirector().getGlobalTime() - _firstMotionTime
                            if (dt>0) {
                                val p1:Vec2 = _touchStartPosition
                                val p2:Vec2 = _touchCurrentPosition
                                vx = (p2.x - p1.x) / dt
                                vy = (p2.y - p1.y) / dt
                            }
                        }

                        if (abs(vy)>1000f) {
                            _scroller?.onTouchFling(vy, 1 - _openState)
                        } else {
                            _scroller?.onTouchUp()
                        }
                        scheduleUpdate()
                    } else {
                        _scroller?.onTouchUp()
                        scheduleUpdate()
                    }
                }

                _scrollEventTargeted = false
                _inScrollEvent = false
                // _velocityTracker!!.clear() or _velocityTracker!!.recycle() ???
//                _velocityTracker!!.clear()
                _velocityTracker!!.recycle()
            }

            MotionEvent.ACTION_MOVE -> {
                if (_scrollEventTargeted) {
                    _velocityTracker!!.addMovement(ev)

                    val deltaY = if (!_inScrollEvent) y - _lastMotionY else point.y - _touchPrevPosition.y

                    if (!_inScrollEvent) {
                        val ay = abs(y-_lastMotionY)

                        if (ay > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true

                            if (_touchMotionTarget!=null) {
                                cancelTouchEvent(_touchMotionTarget, ev)
                                _touchMotionTarget = null
                            }
                        }
                    }

                    if (_inScrollEvent) {
                        _scroller!!.onTouchScroll(deltaY)
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

        if (action==MotionEvent.ACTION_UP) {
            return TOUCH_FALSE
        }

        return if (_scrollEventTargeted) TOUCH_TRUE else TOUCH_FALSE
    }
}