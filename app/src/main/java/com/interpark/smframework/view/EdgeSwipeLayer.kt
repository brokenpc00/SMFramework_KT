package com.brokenpc.smframework.view

import android.view.VelocityTracker
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.scroller.PageScroller
import com.brokenpc.smframework.base.types.Vec2

open class EdgeSwipeLayer(director:IDirector) : SMView(director) {
    protected var _fakeFlingDirection:Int = 0
    protected var _openState:Int = 0
    protected var _swipeSize:Float = 0f
    protected var _edgeSize:Float = 0f
    protected var _curPosition:Float = 0f
    protected var _firstMotionTime:Float = 0f
    protected var _inScrollEvent:Boolean = false
    protected var _lastMotionX:Float = 0f
    protected var _lastMotionY:Float = 0f
    protected var _lastScrollPosition:Float = 0f
    protected var _scrollEventTargeted:Boolean = false
    protected var _scroller:PageScroller? = null
    protected var _velocityTracker:VelocityTracker? = null

    open fun setSwipeWidth(width: Float) {
        _swipeSize = width
        _scroller!!.setCellSize(_swipeSize)
        _scroller!!.setWindowSize(_swipeSize)
        _scroller!!.setScrollSize(_swipeSize * 2)
    }

    open fun setEdgeWidth(width: Float) {
        _edgeSize = width
    }

    override fun update(dt: Float) {
        updateScrollPosition(dt)
    }

    open fun updateScrollPosition(dt: Float) {}

    open fun isOpen(): Boolean {
        return _openState == 1
    }

    open fun inScrollEvent(): Boolean {
        return _inScrollEvent
    }

    open fun isScrollTargeted(): Boolean {
        return _scrollEventTargeted
    }

    open fun isScrollArea(worldPoint: Vec2): Boolean {
        return worldPoint.x < _edgeSize
    }

    open fun openStateChanged(openState: Int) {
        _openState = openState
    }
}