package com.interpark.smframework.base.scroller

import android.view.VelocityTracker
import com.interpark.smframework.base.types.Rect

interface _ScrollProtocol {
    fun getScroller(): SMScroller?
    fun updateScrollInParentVisit(deltaScroll: Float): Boolean
    fun setScrollParent(parent: _ScrollProtocol?)
    fun notifyScrollUpdate()
    fun setInnerScrollMargin(margin: Float)
    fun setMinScrollSize(minScrollSize: Float)
    fun setTableRect(tableRect: Rect?)
    fun setScrollRect(scrollRect: Rect?)
    fun setBaseScrollPosition(position: Float)
    fun getBaseScrollPosition(): Float
    var _scroller: SMScroller?
    var _velocityTracker: VelocityTracker?
    var _scrollParent: _ScrollProtocol?
    var _innerScrollMargin:Float
    var _minScrollSize:Float
    var _baseScrollPosition:Float
    var _inScrollEvent:Boolean
    var _tableRect: Rect?
    var _scrollRect: Rect?
}