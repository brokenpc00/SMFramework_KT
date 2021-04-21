package com.interpark.smframework.view

import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.scroller.PageScroller
import com.brokenpc.smframework.base.scroller.SMScroller
import com.brokenpc.smframework.view.SMTableView

class SMPageView(director: IDirector): SMTableView(director), PageScroller.PAGE_CALLBACK {

    protected lateinit var _pageScroller:PageScroller

    companion object {
        @JvmStatic
        fun create(director: IDirector, orient: Orientation, x: Float, y: Float, width: Float, height: Float): SMPageView {
            return create(director, orient, x, y, width, height, 0f, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, orientation: Orientation, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): SMPageView {
            val view = SMPageView(director)
            view.initWithOrientAndSize(orientation, if (orientation==Orientation.HORIZONTAL){width} else {height})
            view.setContentSize(width, height)
            view.setPosition(x, y)
            view.setAnchorPoint(anchorX, anchorY)
            return view
        }
    }

    fun initFixedPages(numOfPages: Int, pageSize: Float) {
        initFixedPages(numOfPages, pageSize, 0)
    }

    fun initFixedPages(numOfPages: Int, pageSize: Float, initPage: Int) {
        initFixedColumnInfo(numOfPages, pageSize, initPage)
    }

    interface OnPageScrollCallback {
        fun onPageScrollCallback(view: SMPageView, position: Float, distance: Float)
    }
    private var _onPageScrollCallback:OnPageScrollCallback? = null
    fun setOnPageScrollCallback(callback: OnPageScrollCallback?) {_onPageScrollCallback=callback}

    interface OnPageChangedCallback {
        fun onPageChangedCallback(view: SMPageView, page: Int)
    }
    private var _onPageChangedCallback: OnPageChangedCallback? = null
    fun setOnPageChangedCallback(callback: OnPageChangedCallback?) {_onPageChangedCallback=callback}

    override fun scrollFling(velocity: Float) {
        val movePage = (_scroller!!.getScrollPosition()/_scroller!!.getWindowSize()).toInt()

        if (velocity>0) {
            if (movePage==_currentPage) {
                _scroller!!.onTouchFling(velocity, _currentPage+1)
                return
            }
        } else {
            if (movePage==_currentPage-1) {
                _scroller!!.onTouchFling(velocity, _currentPage-1)
                return
            }
        }

        _scroller!!.onTouchFling(velocity, _currentPage)
    }

    fun goPage(page: Int) {
        goPage(page, false)
    }

    fun goPage(page: Int, immediate: Boolean) {
        if (BuildConfig.DEBUG && !(page >= 0 && page <= _pageScroller.getMaxPageNo())) {
            error("Assertion failed")
        }

        if (getScroller() is PageScroller) {
            val scroller = getScroller() as PageScroller
            if (immediate) {
                fakeSetCurrentPage(page)
            } else {
                jumpPage(page, scroller.getCellSize())
            }
        }
    }

    fun getCurrentPage(): Int {return _currentPage}

    fun fakeSetCurrentPage(page: Int) {
        setScrollPosition(page*_scroller!!.getCellSize())
        _currentPage = page
    }

    protected fun initWithOrientAndSize(orient: Orientation, pageSize: Float): Boolean {
        if (super.initWithOrientAndColumns(orient, 1)) {
            super.hintFixedCellSize(pageSize)

            _pageScroller.setCellSize(pageSize)
            _pageScroller.pageChangedCallback = this

            return true
        }

        return false
    }

    override fun pageChangedCallback(page: Int) {
        _currentPage = page

        _onPageChangedCallback?.onPageChangedCallback(this, _currentPage)
    }

    override fun initScroller(): SMScroller? {
        _scroller = PageScroller(getDirector()).also { _pageScroller = it }
        return _scroller
    }

    override fun onScrollChanged(position: Float, distance: Float) {
        _onPageScrollCallback?.onPageScrollCallback(this, position/_scroller!!.getCellSize(), distance)
    }

    override fun scrollTo(positiont: Float) {

    }

    override fun scrollBy(offset: Float) {

    }

    override fun resizeRowForCell(
        child: SMView,
        newSize: Float,
        durationt: Float,
        delayt: Float
    ): Boolean {
        return false
    }

    override fun setScrollMarginSize(firstMargin: Float, lastMargin: Float) {

    }

    override fun hintFixedCellSize(cellSize: Float) {

    }

}