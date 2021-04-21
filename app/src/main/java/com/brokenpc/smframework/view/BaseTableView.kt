package com.brokenpc.smframework.view

import android.util.SparseArray
import android.view.VelocityTracker
import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.scroller.SMScroller
import com.brokenpc.smframework.base.scroller._ScrollProtocol
import com.brokenpc.smframework.base.types.Rect
import com.brokenpc.smframework.base.types.Ref
import java.util.*

open class BaseTableView(director:IDirector) : SMView(director), _ScrollProtocol {

    override fun getScroller(): SMScroller? {
        return _scroller
    }

    override fun updateScrollInParentVisit(deltaScroll: Float): Boolean {
        return false
    }

    override fun setScrollParent(parent: _ScrollProtocol?) {
        _scrollParent = parent
    }

    override fun notifyScrollUpdate() {}

    override fun setInnerScrollMargin(margin: Float) {
        _innerScrollMargin = margin
    }

    override fun setMinScrollSize(minScrollSize: Float) {
        _minScrollSize = minScrollSize
    }

    override fun setTableRect(tableRect: Rect?) {
        if (tableRect != null) {
            if (_tableRect != null) {
                _tableRect!!.set(tableRect)
            } else {
                _tableRect = Rect(tableRect)
            }
        } else {
            _tableRect = null
        }
    }

    override fun setScrollRect(scrollRect: Rect?) {
        if (scrollRect != null) {
            if (_scrollRect != null) {
                _scrollRect!!.set(scrollRect)
            } else {
                _scrollRect = Rect(scrollRect)
            }
        } else {
            _scrollRect = null
        }
    }

    override fun setBaseScrollPosition(position: Float) {
        _baseScrollPosition = position
    }

    override fun getBaseScrollPosition(): Float {
        return _baseScrollPosition
    }

    override var _scroller: SMScroller? = null
    override var _velocityTracker: VelocityTracker? = null
    override var _scrollParent: _ScrollProtocol? = null
    override var _innerScrollMargin = 0f
    override var _minScrollSize = 0f
    override var _baseScrollPosition = 0f
    override var _inScrollEvent = false
    override var _tableRect: Rect? = null
    override var _scrollRect: Rect? = null


    override fun removeAllChildrenWithCleanup(cleanup: Boolean) {
        // contents 들만 남기고 다 지움

        // contents 떼어냄...
        if (_contentView != null && _contentView!!.isNotEmpty()) {
            for (i in 0 until _numContainer) {
                if (_contentView!![i]!=null) {
                    super.removeChild(_contentView!![i])
                }
            }
        }

        // 나머지 다 날리고
        super.removeAllChildrenWithCleanup(cleanup)

        // contents들을 다시 붙임
        if (_contentView != null && _contentView!!.isNotEmpty()) {
            for (i in 0 until _numContainer) {
                if (_contentView!![i]!=null) {
                    super.addChild(_contentView!![i]!!)
                }
            }
        }
    }

    fun getContainerCount(): Long {
        return _numContainer.toLong()
    }

    open fun setHeaderView(headerView: SMView?) {
        if (_headerView != null && _headerView !== headerView) {
            // exist header view
            // remove and release
            super.removeChild(_headerView, true)
            _headerView = null
        }
        _headerView = headerView
        _isHeaderInList = false
    }

    open fun setFooterView(footerView: SMView?) {
        if (_footerView != null && _footerView !== footerView) {
            // exist footer view
            // remove and release
            super.removeChild(_footerView, true)
            _footerView = null
        }
        _footerView = footerView
        _isFooterInList = false
    }

    fun stop() {
        if (_scroller != null) {
            // last click;
            _scroller!!.onTouchDown()
            _scroller!!.onTouchUp()
        }
    }

    fun getColumnChildren(column: Int): ArrayList<SMView>? {
        if (BuildConfig.DEBUG && column !in 0 until _numContainer) {
            error("Assertion failed")
        }

        return _contentView!![column]!!.getChildren()
    }

    fun isHeaderInList(): Boolean {
        return _isHeaderInList
    }

    fun isFooterInList(): Boolean {
        return _isFooterInList
    }

    fun setScrollLock(bLock: Boolean) {
        _lockScroll = bLock
    }

    fun getHeaderView(): SMView? {
        return _headerView
    }


    protected fun initWithContainer(numContainer: Int): Boolean {
        if (BuildConfig.DEBUG && numContainer <= 0) {
            error("Assertion failed")
        }
        _numContainer = numContainer
        _contentView = arrayOfNulls(_numContainer)
        for (i in 0 until _numContainer) {
            _contentView!![i] = create(getDirector())
            super.addChild(_contentView!![i]!!)
        }
        if (_headerView != null && _isHeaderInList) {
            super.removeChild(_headerView, true)
            _isHeaderInList = false
        }
        if (_footerView != null && _isFooterInList) {
            super.removeChild(_footerView, true)
        }
        removeAllChildrenWithCleanup(true)
        return true
    }

    protected fun removeChildAndHold(columnNum: Int, tag: Int, child: SMView) {
        removeChildAndHold(columnNum, tag, child, true)
    }

    protected fun removeChildAndHold(columnNum: Int, tag: Int, child: SMView, cleanup: Boolean) {
        getHolder().insert(child.hashCode(), _contentView!![columnNum], child)
    }

    protected fun findFromHolder(hashCode: Int): SMView? {
        return if (_holder != null) {
            getHolder().find(hashCode)
        } else null
    }

    protected fun eraseFromHolder(hashCode: Int) {
        if (_holder != null) {
            getHolder().erase(hashCode)
        }
    }

    protected fun clearInstantHolder() {
        _holder?.clear()
    }

    protected fun addChild(columnNum: Int, child: SMView) {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.addChild(child)
    }

    protected fun addChild(columnNum: Int, child: SMView, localZOrder: Int) {
        if (BuildConfig.DEBUG && columnNum < 0) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.addChild(child, localZOrder)
    }

    protected fun addChild(columnNum: Int, child: SMView, localZOrder: Int, tag: Int) {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.addChild(child, localZOrder, tag)
    }

    protected fun addChild(columnNum: Int, child: SMView, localZOrder: Int, name: String
    ) {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.addChild(child, localZOrder, name)
    }

    protected fun removeChild(columnNum: Int, child: SMView) {
        removeChild(columnNum, child, true)
    }

    protected fun removeChild(columnNum: Int, child: SMView, cleanup: Boolean) {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.removeChild(child, cleanup)
    }

    protected fun removeChildByTag(columnNum: Int, tag: Int) {
        removeChildByTag(columnNum, tag, true)
    }

    protected fun removeChildByTag(columnNum: Int, tag: Int, cleanup: Boolean) {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.removeChildByTag(tag, cleanup)
    }

    protected fun removeChildByName(columnNum: Int, name: String) {
        removeChildByName(columnNum, name, true)
    }

    protected fun removeChildByName(columnNum: Int, name: String, cleanup: Boolean
    ) {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.removeChildByName(name, cleanup)
    }

    protected fun sortAllChildren(columnNum: Int) {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        _contentView!![columnNum]!!.sortAllChildren()
    }

    protected fun getChildrenCount(columnNum: Int): Int {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        return _contentView!![columnNum]!!.getChildrenCount()
    }

    protected fun getChildAt(columnNum: Int, index: Int): SMView? {
        if (BuildConfig.DEBUG && columnNum !in 0 until _numContainer) {
            error("Assertion failed")
        }
        return _contentView!![columnNum]!!.getChildren()[index]
    }

    // cell contentView
    protected var _contentView: Array<SMView?>? = null

    protected var _numContainer = 0

    // header
    protected var _headerView: SMView? = null
    protected var _isHeaderInList = false

    // footer
    protected var _footerView: SMView? = null
    protected var _isFooterInList = false

    protected var _lockScroll = false
    protected var _reloadFlag = false


    private class InstantHolder(director: IDirector) : Ref(director) {
        fun insert(hashCode: Int, parent: SMView?, child: SMView): Boolean {
            return insert(hashCode, parent, child, true)
        }
        fun insert(hashCode: Int, parent: SMView?, child: SMView, cleanup: Boolean): Boolean {
            val view = _data[hashCode]
            if (view != null) {
                return false
            }
            parent!!.removeChild(child, cleanup)
            _data.append(hashCode, child)
            return true
        }

        fun find(hashCode: Int): SMView? {
            return _data[hashCode]
        }

        fun erase(hashCode: Int) {
            _data.remove(hashCode)
        }

        fun clear() {
            _data.clear()
        }

        private val _data = SparseArray<SMView>()
    }

    // instant view holder
    private var _holder: InstantHolder? = null

    private fun getHolder(): InstantHolder {
        if (_holder == null) {
            _holder = InstantHolder(
                getDirector()
            )
        }
        return _holder!!
    }
}