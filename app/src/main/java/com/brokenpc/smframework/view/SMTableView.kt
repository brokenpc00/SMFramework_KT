package com.brokenpc.smframework.view

import android.view.MotionEvent
import android.view.VelocityTracker
import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SMView.Companion.create
import com.brokenpc.smframework.base.SMView.Companion.getDirection
import com.brokenpc.smframework.base.SMView.Companion.signum
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.scroller.FlexibleScroller
import com.brokenpc.smframework.base.scroller.SMScroller
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.base.types.DelayTime.Companion.create
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.SMTableView.RefreshState
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max

open class SMTableView(director:IDirector) : BaseTableView(director) {
    private var _column: Array<ColumnInfo?>? = null
    private val _columnIndex: Int = 0
    private var _orient: Orientation = Orientation.VERTICAL
    private var _lastItemCount: Int = 0
    private var _firstMargin: Float = 0f
    private var _lastMargin: Float = 0f
    private var _preloadPadding: Float = 0f
    private var _lastScrollPosition: Float = 0f
    private var _hintFixedChildSize: Float = 0f
    private var _hintIsFixedSize: Boolean = false
    private var _justAtLast: Boolean = false
    private var _canExactScrollSize: Boolean = false
    private var _internalActionTag: Int = AppConst.TAG.ACTION_LIST_ITEM_DEFAULT
    private var _reuseScrapper: ReuseScrapper = ReuseScrapper()
    private var _refreshView: SMView? = null
    private var _refreshTriggerSize: Float = 0f
    private var _refreshMinSize: Float = 0f
    private var _refreshSize: Float = 0f
    private var _lastRefreshSize: Float = 0f
    private var _refreshState: RefreshState = RefreshState.NONE
    private var _lastRefreshState: RefreshState = RefreshState.NONE
    private var _progressLoading: Boolean = false
    private var _animationDirty: Boolean = false
    private var _firstMotionTime: Float = 0f
    private var _lastMotionX: Float = 0f
    private var _lastMotionY: Float = 0f
    private var _deltaScroll: Float = 0f
    private var _needUpdate: Boolean = false
    private var _skipUpdateOnVisit: Boolean = false
    private var _forceJumpPage: Boolean = false
    private var _touchFocused: Boolean = false
    private var _fillWithCellsFirstTime: Boolean = false
    private var _maxVelocicy: Float = AppConst.Config.MAX_VELOCITY
    private var _accelScrollEnable: Boolean = false
    private var _lastVelocityX: Float = 0f
    private var _lastVelocityY: Float = 0f
    private var _lastFlingTime: Float = 0f
    private var _accelCount: Int = 0
    private var _initRefreshEnable: Boolean = false
    private var _reloadExceptHeader: Boolean = false

    protected var _currentPage: Int = 0

    enum class Orientation {
        VERTICAL, HORIZONTAL
    }

    enum class RefreshState {
        NONE, ENTER, READY, REFRESHING, EXIT
    }

    companion object {
        const val JUMP_ACTION_DURATION:Float = 0.25f
        const val CLEANUP_FLAG:Boolean = false
        const val FLAG_SCROLL_UPDATE:Long = 1L

        // member class
        const val ITEM_FLAG_DELETE: Int = 1
        const val ITEM_FLAG_RESIZE: Int = 1 shl 1
        const val ITEM_FLAG_INSERT: Int = 1 shl 2


        @JvmStatic
        fun createa(director: IDirector, orient:Orientation): SMTableView {
            return createMultiColumn(director, orient, 1)!!
        }

        @JvmStatic
        fun create(director: IDirector, orient: Orientation, x: Float, y: Float, width: Float, height: Float): SMTableView? {
            return SMTableView.createMultiColumn(director, orient, 1, x, y, width, height, 0f, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, orient: Orientation, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): SMTableView? {
            return SMTableView.createMultiColumn(director, orient, 1, x, y, width, height, anchorX, anchorY)
        }

        @JvmStatic
        fun createMultiColumn(director: IDirector, orient: Orientation, numOfColumn: Int): SMTableView? {
            val view = SMTableView(director)
            return if (view.initWithOrientAndColumns(orient, numOfColumn)) {
                view
            } else null
        }

        @JvmStatic
        fun createMultiColumn(director: IDirector, orient: Orientation, numOfColumn: Int, x: Float, y: Float, width: Float, height: Float): SMTableView? {
            return createMultiColumn(director, orient, numOfColumn, x, y, width, height, 0f, 0f)
        }

        @JvmStatic
        fun createMultiColumn(director: IDirector, orient: Orientation, numOfColumn: Int, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): SMTableView? {
            val view = SMTableView(director)
            if (view.initWithOrientAndColumns(orient, numOfColumn)) {
                view.setContentSize(Size(width, height))
                view.setPosition(x, y)
                view.setAnchorPoint(anchorX, anchorY)
                return view
            }
            return null
        }
    }

    open fun dequeueReusableCellWithIdentifier(cellID: String): SMView? {
        _reuseScrapper._internalReuseType = _reuseScrapper.getReuseType(cellID)
        _reuseScrapper._internalReuseNode = _reuseScrapper.back(_reuseScrapper._internalReuseType)
        return _reuseScrapper._internalReuseNode
    }


    // callback & listener
    // section 당 row 개수 delegate
    interface NumberOfRowsInSection {
        fun numberOfRowsInSection(section: Int): Int
    }

    var numberOfRowsInSection: NumberOfRowsInSection? = null

    // IndexPath로 cell을 하나 얻어오는 delegate
    interface CellForRowAtIndexPath {
        fun cellForRowAtIndexPath(indexPath: IndexPath): SMView
    }

    var cellForRowAtIndexPath: CellForRowAtIndexPath? = null

    interface CellResizeCallback {
        fun onCellResizeCallback(cell: SMView?, newSize: Float)
    }

    var onCellResizeCallback: CellResizeCallback? = null

    interface CellResizeCompletionCallback {
        fun onCellResizeCompletionCallback(cell: SMView?)
    }

    var onCellResizeCompletionCallback: CellResizeCompletionCallback? = null

    interface CellInsertCallback {
        fun onCellInsertCallback(cell: SMView?, progress: Float)
    }

    var onCellInsertCallback: CellInsertCallback? = null

    interface CellDeleteCallback {
        fun onCellDeleteCallback(cell: SMView?, progress: Float)
    }

    var onCellDeleteCallback: CellDeleteCallback? = null

    interface CellDeleteCompletionCallback {
        fun onCellDeleteCompletionCallback()
    }

    var onCellDeleteCompletionCallback: CellDeleteCompletionCallback? = null

    interface ScrollCallback {
        fun onScrollCallback(position: Float, distance: Float)
    }

    var onScrollCallback: ScrollCallback? = null


    interface RefreshDataCallback {
        fun onRefreshDataCallback(cell: SMView?, state: RefreshState, size: Float)
    }

    var onRefreshDataCallback: RefreshDataCallback? = null

    interface CanRefreshData {
        fun canRefreshData(): Boolean
    }

    var canRefreshData: CanRefreshData? = null


    // load more callback... callback이 세팅 되었을때 footer가 나타나면 호출된다. 여기서 통신등 페이지 더보기를 호출 하면 된다. 끝나면 endLoadData()를 호출 할 것.
    // 다시 호출 될일 이 없다면 callback을 nullptr로 세팅하거나 footer 자체를 nullptr로 세팅하면 된다.
    interface LoadDataCallback {
        fun onLoadDataCallback(cell: SMView?): Boolean
    }

    var onLoadDataCallback: LoadDataCallback? = null
    private var onLoadDataCallbackTemp: LoadDataCallback? = null


    // cell이 처음 나타날때 애니메이션을 위한 callback (willDisplayCell...같은 역할)
    interface InitFillWithCells {
        fun onInitFillWithCells(tableView: SMTableView?)
    }

    var onInitFillWithCells: InitFillWithCells? = null


    override fun isTouchEnable(): Boolean {
        return true
    }

    open fun getColumnCount(): Int {
        return getContainerCount().toInt()
    }

    // page view에서
    open fun jumpPage(pageNo: Int, pageSize: Float): Boolean {
        if (BuildConfig.DEBUG && cellForRowAtIndexPath==null) {
            error("Assertion Failed")
        }
        if (_forceJumpPage) {
            return false
        }
        val currentPage = (_scroller!!.getNewScrollPosition() / pageSize).toInt()
        val info: ColumnInfo = _column!![0]!!
        if (pageNo == currentPage) {
            return false
        } else if (pageNo == currentPage + 1) {
            // 다음 페이지
            _scroller?.onTouchFling(-10000f, currentPage)
            scheduleScrollUpdate()
        } else if (pageNo == currentPage - 1) {
            _scroller?.onTouchFling(10000f, currentPage)
            scheduleScrollUpdate()
        } else {
            val numChild: Int = getChildrenCount(0)
            val cursor: Cursor = Cursor(info.getViewLastCursor())
            //            Cursor cursor = info.getViewLastCursor();
            for (i in numChild - 1 downTo 0) {
                cursor.dec(true)
                if (cursor.getIndexPath().getIndex() and currentPage != 0) {
                    val child: SMView? = getChildAt(0, i)
                    val item: Item? = cursor.getItem()
                    removeChildAndReuseScrap(0, item!!._reuseType, child, true)
                }
            }

            // 적절한 위치에 추가.
            val position: Float
            val direction: Int
            if (pageNo > currentPage) {
                // 뒤쪽 페이지
                position = pageSize
                direction = +1
            } else {
                position = -pageSize
                direction = -1
            }
            cursor.set(info.getFirstCursor())
            for (i in 0 until pageNo) {
                // target cursor
                cursor.inc(false)
            }
            _reuseScrapper._internalReuseType = -1
            _reuseScrapper._internalReuseNode = null
            val child: SMView = cellForRowAtIndexPath!!.cellForRowAtIndexPath(cursor.getIndexPath())
            if (child.getParent() != null) {
                _scroller!!.onTouchUp()
                _forceJumpPage = false
                return true
            }

            // order
            child.setLocalZOrder(cursor.getPosition())
            val item: Item? = cursor.getItem()
            if (_reuseScrapper._internalReuseType >= 0) {
                item?._reuseType = _reuseScrapper._internalReuseType
            }
            child.setPositionX(position)
            addChild(0, child)
            sortAllChildren(0)
            if (_reuseScrapper._internalReuseNode != null && _reuseScrapper._internalReuseNode === child) {
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType)
                _reuseScrapper._internalReuseType = -1
                _reuseScrapper._internalReuseNode = null
            }
            val remainAction: Action? = getActionByTag(AppConst.TAG.ACTION_LIST_JUMP)
            if (remainAction != null) {
                val action: _PageJumpAction = remainAction as _PageJumpAction
                action.complete()
                stopAction(action)
            }
            _forceJumpPage = true
            val action: _PageJumpAction = _PageJumpAction(getDirector(), this, cursor, pageSize, currentPage, pageNo, direction)
            action.setTag(AppConst.TAG.ACTION_LIST_JUMP)
            action.setDuration(SMTableView.JUMP_ACTION_DURATION)
            runAction(action)
        }
        return true
    }


    protected open fun initWithOrientAndColumns(orient: Orientation, numOfColumn: Int): Boolean {
        if (initWithContainer(numOfColumn)) {
            _orient = orient
            _column = arrayOfNulls<ColumnInfo>(numOfColumn)
            for (col in 0 until numOfColumn) {
                _column!![col] = ColumnInfo()
                _column!![col]!!.init(this, col)
            }
            _reuseScrapper = ReuseScrapper()
            initScroller()
            _lastScrollPosition = _scroller!!.getScrollPosition()
            if (_velocityTracker == null) {
                _velocityTracker = VelocityTracker.obtain()
            }
            _velocityTracker!!.clear()
            scheduleScrollUpdate()
            return true
        }
        return false
    }

    protected open fun initScroller(): SMScroller? {
        _scroller = FlexibleScroller(getDirector())
        return _scroller
    }

    override fun setContentSize(size: Size) {
        super.setContentSize(size)
        if (isVertical()) {
            _scroller!!.setWindowSize(size.height)
            for (col in 0 until _numContainer) {
                _contentView!![col]!!.setContentSize(Size(size.width / _numContainer, size.height))
                _contentView!![col]!!.setPositionX(col * size.width / _numContainer)
            }
        } else {
            _scroller!!.setWindowSize(size.width)
            for (col in 0 until _numContainer) {
                _contentView!![col]!!.setContentSize(Size(size.width, size.height / _numContainer))
                _contentView!![col]!!.setPositionY(col * size.height / _numContainer)
            }
        }
        scheduleScrollUpdate()
    }

    open fun hintFixedCellSize(cellSize: Float) {
        _hintIsFixedSize = true
        _hintFixedChildSize = cellSize
    }

    //View가 미리 생성되는 경계선 바깥쪽 padding
    //@param paddingPixels Scroll padding pixels
    open fun setPreloadPaddingSize(paddingSize: Float) {
        if (paddingSize >= 0) {
            _preloadPadding = paddingSize
        }
        scheduleScrollUpdate()
    }

    open fun setScrollMarginSize(firstMargin: Float, lastMargin: Float) {
        _firstMargin = firstMargin
        _lastMargin = lastMargin
        scheduleScrollUpdate()
    }

    // position childe view
    protected open fun positionChildren(scrollPosition: Float, containerSize: Float, headerSize: Float, footerSize: Float) {
        if (BuildConfig.DEBUG && cellForRowAtIndexPath==null) {
            error("Assertion Failed")
        }

        // scroll 위치에 따라 children 좌표 세팅.
        val startLocation: Float = headerSize + _firstMargin + _innerScrollMargin - scrollPosition
        var lastLocation: Float = 0f
        for (col in 0 until _numContainer) {
            val numChild: Int = getChildrenCount(col)
            if (numChild > 0) {
                val info: ColumnInfo = _column!![col]!!
                val cursor: Cursor = Cursor(info.getViewFirstCursor())
                var i: Int = 0
                while (i < numChild) {
                    var child: SMView = getChildAt(col, i)!!
                    val item: Item = cursor.getItem()!!
                    if (item._reload) {
                        // cell reload
                        item._reload = false

                        // child를 없애고 다시 만든다.
                        removeChild(col, child)
                        _reuseScrapper._internalReuseType = -1
                        _reuseScrapper._internalReuseNode = null
                        child = cellForRowAtIndexPath!!.cellForRowAtIndexPath(item._indexPath)
                        addChild(col, child)
                        child.setLocalZOrder(cursor.getPosition())
                        if (_reuseScrapper._internalReuseType >= 0) {
                            item._reuseType = _reuseScrapper._internalReuseType
                        }
                        if (_reuseScrapper._internalReuseNode != null && _reuseScrapper._internalReuseNode === child) {
                            _reuseScrapper.popBack(_reuseScrapper._internalReuseType)
                            _reuseScrapper._internalReuseType = -1
                            _reuseScrapper._internalReuseNode = null
                        }
                        if (_hintIsFixedSize) {
                            item._newSize = _hintFixedChildSize
                        } else {
                            if (isVertical()) {
                                item._newSize = child.getContentSize().height
                            } else {
                                item._newSize = child.getContentSize().width
                            }
                        }
                        info.resizeCursor(cursor)
                        sortAllChildren(col)
                    }

                    // Resize 처리
                    if (item._size != item._newSize) {
                        info.resizeCursor(cursor)
                        onCellResizeCallback?.onCellResizeCallback(child, item._newSize)
                    }
                    val location: Float = startLocation + cursor.getLocation()
                    onPositionCell(child, location, false)
                    child.setLocalZOrder(cursor.getPosition())
                    i++
                    cursor.inc(false)
                }
                lastLocation = lastLocation.coerceAtLeast(info.getLastCursor().getLocation())
            }
        }
        lastLocation += startLocation
        if (_headerView != null && _isHeaderInList) {
            onPositionHeader(_headerView!!, startLocation - headerSize, false)
        }
        if (_footerView != null && _isFooterInList) {
            onPositionFooter(_footerView!!, lastLocation, false)
        }
        if (_refreshView != null && _refreshState != RefreshState.NONE) {
            if (isVertical()) {
                _refreshView!!.setPositionY(startLocation)
            } else {
                _refreshView!!.setPositionX(startLocation - _refreshView!!.getContentSize().width)
            }
        }
    }

    open fun onPositionCell(cell: SMView, position: Float, isAdded: Boolean) {
        if (isVertical()) {
            cell.setPositionY(position)
        } else {
            cell.setPositionX(position)
        }
    }

    open fun onPositionHeader(headerView: SMView, position: Float, isAdded: Boolean) {
        if (isVertical()) {
            headerView.setPositionY(position)
        } else {
            headerView.setPositionX(position)
        }
    }

    open fun onPositionFooter(footerView: SMView, position: Float, isAdded: Boolean) {
        if (isVertical()) {
            footerView.setPositionY(position)
        } else {
            footerView.setPositionX(position)
        }
    }

    // view를 벗어난 cell를 화면에서 제거
    protected open fun clippingChildren(scrollPosition: Float, containerSize: Float, headerSize: Float, footerSize: Float) {
        // 스크롤 위치에 따라 화면에 보이지 않는 child 제거
        val startLocation: Float = headerSize + _firstMargin + _innerScrollMargin - scrollPosition
        var lastLocation: Float = 0f
        for (col in 0 until _numContainer) {
            var numChild: Int = getChildrenCount(col)
            val info: ColumnInfo = _column!![col]!!

            // 상단 제거
            if (numChild > 0) {
                for (i in 0 until numChild) {
                    val cursor: Cursor = Cursor(info.getViewFirstCursor())
                    //                    Cursor cursor = info.getViewFirstCursor();
                    val child: SMView? = getChildAt(col, 0)
                    if (child != null && startLocation + cursor.getLastLocation() <= -_preloadPadding) {
                        val item: Item = cursor.getItem()!!
                        if (item.isDeleted()) {
                            //Delete중인 상단 child는 hold (Animation이 진행되어야 하기 때문에)
                            removeChildAndHold(col, item._tag, child, false)
                        } else {
                            removeChildAndReuseScrap(col, item._reuseType, child, CLEANUP_FLAG)
                        }
                        info.retreatViewFirst()
                    } else {
                        break
                    }
                }
            }

            // 하단 제거
            numChild = getChildrenCount(col)
            if (numChild > 0) {
                for (i in numChild - 1 downTo 0) {
                    val cursor = Cursor(info.getViewLastCursor(-1))
                    val child: SMView? = getChildAt(col, i)
                    if (child != null && startLocation + cursor.getLocation() >= containerSize + _preloadPadding) {
                        val item: Item = cursor.getItem()!!
                        if (item.isDeleted()) {
                            // 삭제중인 하단 child는 즉시 삭제
                            stopAndCompleteChildAction(item._tag)
                        } else {
                            if (item._tag != 0) {
                                // Animation중인 하단 child는 즉시 적용
                                stopAndCompleteChildAction(item._tag)
                                item._tag = 0
                            }
                            removeChildAndReuseScrap(col, item._reuseType, child, SMTableView.CLEANUP_FLAG)
                            info.retreatViewLast()
                        }
                    } else {
                        break
                    }
                }
            }
            lastLocation = max(lastLocation, info.getLastCursor().getLocation())
        }
        lastLocation += startLocation

        // 헤더 제거
        if (_headerView != null && _isHeaderInList) {
            if (startLocation < -_preloadPadding) {
                super.removeChild(_headerView, SMTableView.CLEANUP_FLAG)
                _isHeaderInList = false
            }
        }

        // 푸터 제거
        if (_footerView != null && _isFooterInList) {
            if (lastLocation > containerSize + _preloadPadding) {
                super.removeChild(_footerView, SMTableView.CLEANUP_FLAG)
                _isFooterInList = false
            }
        }
    }

    // fill backward
    protected open fun fillListBack(adapterItemCount: Int, scrollPosition: Float, containerSize: Float, headerSize: Float, footerSize: Float): Boolean {
        if (BuildConfig.DEBUG && cellForRowAtIndexPath==null) {
            error("Assertion Failed")
        }
        var scrollLocation: Float = _firstMargin + _innerScrollMargin - scrollPosition
        var limitLocation: Float = containerSize + _preloadPadding - scrollLocation
        if (_headerView != null) {
            limitLocation -= headerSize
        }
        var child: SMView? = null
        var info: ColumnInfo? = null
        var added: Boolean = false
        var lastIndex: Int = 0
        for (col in 0 until _numContainer) {
            lastIndex += _column!![col]!!.getAliveItemCount()
        }
        while (adapterItemCount > 0) {
            var lastLocation: Float = Float.Companion.MAX_VALUE
            val isAtLast: Boolean = (lastIndex == adapterItemCount)
            var column: Int = -1
            for (col in 0 until _numContainer) {
                info = _column!![col]!!
                if (isAtLast) {
                    if (info.getViewLastCursor().getLocation() < lastLocation && !info.isAtLast()) {
                        column = col
                        lastLocation = info.getViewLastCursor().getLocation()
                    }
                } else {
                    if (info.getViewLastCursor().getLocation() < lastLocation) {
                        column = col
                        lastLocation = info.getViewLastCursor().getLocation()
                    }
                }
            }
            if (lastLocation >= limitLocation || column < 0) {
                break
            }


            // 다음 추가할 아이템을 찾는다.
            info = _column!![column]!!
            val indexPath = if (info.isAtLast()) {
                // 이전에 생성된 아이템 없음 => 추가
                IndexPath(0, column, lastIndex++)
            } else {
                // 이전에 생성된 아이템 있음.
                IndexPath(info.getViewLastCursor().getIndexPath())
            }
            _reuseScrapper._internalReuseType = -1
            _reuseScrapper._internalReuseNode = null
            child = cellForRowAtIndexPath!!.cellForRowAtIndexPath(indexPath)
            if (child.getParent() != null) {
                // 이미 attach 되어 있다???
                break
            }
            var childSize: Float
            var reload: Boolean = false
            if (!info.isAtLast()) {
                val item: Item = info.getViewLastCursor().getItem()!!
                if (item._reload) {
                    item._reload = false
                    if (_hintIsFixedSize) {
                        childSize = _hintFixedChildSize
                    } else {
                        childSize =
                            if (isVertical()) child.getContentSize().height else child.getContentSize().width
                    }
                    item._newSize = childSize
                    reload = true
                } else {
                    childSize = item._size
                }
            } else {
                if (_hintIsFixedSize) {
                    childSize = _hintFixedChildSize
                } else {
                    childSize = if (isVertical()) {child.getContentSize().height} else {child.getContentSize().width}
                }
            }

            // cursor 진행
            val cursor = Cursor(info.advanceViewLast(IndexPath(0, column, indexPath.getIndex()), _reuseScrapper._internalReuseType, childSize))
            //            Cursor cursor = info.advanceViewLast(new IndexPath(0, column, indexPath.getIndex()), _reuseScrapper._internalReuseType, childSize);
            if (reload) {
                info.resizeCursor(cursor)
            }

            // order
            child.setLocalZOrder(cursor.getPosition())
            val item: Item = cursor.getItem()!!
            if (_reuseScrapper._internalReuseType >= 0) {
                item._reuseType = _reuseScrapper._internalReuseType
            }
            if ((item._flags and ITEM_FLAG_RESIZE) > 0) {
                onCellResizeCallback?.onCellResizeCallback(child, item._size)
            }
            if (item._tag == 0) {
                if ((item._flags and ITEM_FLAG_INSERT) > 0) {
                    onCellInsertCallback?.onCellInsertCallback(child, 1f)
                }
                item._flags = 0
            }
            childSize = item._size

            // view 내 위치 참조
            val locationInView: Float = headerSize + _firstMargin + _innerScrollMargin - scrollPosition
            onPositionCell(child, locationInView + cursor.getLocation(), true)
            addChild(column, child)
            if (_reuseScrapper._internalReuseNode != null && _reuseScrapper._internalReuseNode === child) {
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType)
                _reuseScrapper._internalReuseType = -1
                _reuseScrapper._internalReuseNode = null
            }
            added = true
        }
        return added
    }

    // fill forward
    protected open fun fillListFront(adapterItemCount: Int, scrollPosition: Float, containerSize: Float, headerSize: Float, footerSize: Float): Boolean {
        if (BuildConfig.DEBUG && cellForRowAtIndexPath==null) {
            error("Assertion Failed")
        }
        val limitLocation: Float = -_preloadPadding + scrollPosition - (headerSize + _firstMargin + _innerScrollMargin)
        var child: SMView? = null
        var info: ColumnInfo? = null
        var added: Boolean = false
        val nCount: Int = 0
        while (adapterItemCount > 0) {
            var firstLocation: Float = Float.Companion.MIN_VALUE

            // 다음 child 추가할 컬럼 / 인덱스 찾기
            var column: Int = -1
            for (col in 0 until _numContainer) {
                info = _column!![col]!!
                if (info.getViewFirstCursor().getLocation() > firstLocation && !info.isAtFirst()) {
                    column = col
                    firstLocation = info.getViewFirstCursor().getLocation()
                }
            }
            if (firstLocation <= limitLocation || column < 0) {
                break
            }
            info = _column!![column]!!
            val cursor: Cursor = Cursor(info.advanceViewFirst())
            //            Cursor cursor = info.advanceViewFirst();
            val item: Item = cursor.getItem()!!
            if (item.isDeleted()) {
                // 삭재중 아이템
                child = findFromHolder(item.hashCode())
                if (child == null) {
                    child = SMView.Companion.create(getDirector())
                    if (isVertical()) {
                        // 아이템의 높이
                        child.setContentSize(Size(child.getContentSize().width, item._size))
                    } else {
                        // 아이템의 넓이
                        child.setContentSize(Size(item._size, child.getContentSize().width))
                    }
                }
            } else {
                _reuseScrapper._internalReuseType = -1
                _reuseScrapper._internalReuseNode = null
                child = cellForRowAtIndexPath!!.cellForRowAtIndexPath(item._indexPath)
                if (_reuseScrapper._internalReuseType >= 0) {
                    item._reuseType = _reuseScrapper._internalReuseType
                }
                if (item._reload) {
                    item._reload = false
                    if (_hintIsFixedSize) {
                        item._newSize = _hintFixedChildSize
                    } else {
                        item._newSize = if (isVertical()) child!!.getContentSize().height else child!!.getContentSize().width
                    }
                    info.resizeCursor(cursor)
                }
            }
            if (child.getParent() != null) {
                // 이미 attach 되어 있다???
                break
            }

            // order
            child.setLocalZOrder(cursor.getPosition())
            if ((item._flags and ITEM_FLAG_RESIZE) > 0) {
                if (onCellResizeCallback != null && child.javaClass == _DeleteNode::class.java) {
                    onCellResizeCallback!!.onCellResizeCallback(child, item._size)
                }
            }
            if (item._tag == 0) {
                if ((item._flags and ITEM_FLAG_INSERT) > 0) {
                    if (onCellInsertCallback != null) {
                        onCellInsertCallback!!.onCellInsertCallback(child, 1f)
                    }
                }
                item._flags = 0
            }

            // view 안의 위치
            val locationInView: Float = headerSize + _firstMargin + _innerScrollMargin - scrollPosition
            onPositionCell(child, locationInView + cursor.getLocation(), true)
            addChild(column, child)
            if ((item._flags and ITEM_FLAG_DELETE) > 0) {
                eraseFromHolder(item._tag)
            } else if (_reuseScrapper._internalReuseNode != null && _reuseScrapper._internalReuseNode === child) {
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType)
                _reuseScrapper._internalReuseType = -1
                _reuseScrapper._internalReuseNode = null
            }
            added = true
        }
        if (added) {
            for (col in 0 until _numContainer) {
                sortAllChildren(col)
            }
        }
        return added
    }

    // fill list
    protected open fun fillList(adapterItemCount: Int, scrollPosition: Float, containerSize: Float, headerSize: Float, footerSize: Float): Boolean {
        var backAdded: Boolean = fillListBack(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize)
        var frontAdded: Boolean = fillListFront(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize)
        if (_headerView != null) {
            if (!_isHeaderInList) {
                if (scrollPosition < headerSize + _firstMargin + _innerScrollMargin) {
                    super.addChild(_headerView!!)

                    val position: Float = -scrollPosition + _firstMargin + _innerScrollMargin
                    onPositionHeader(_headerView!!, position, true)
                    _isHeaderInList = true
                    frontAdded = frontAdded or true
                }
            }
        }
        if (_footerView != null) {
            while (!_isFooterInList) {
                var lastLocation: Float = 0f
                var aliveItemCount: Int = 0
                for (col in 0 until _numContainer) {
                    if (!_column!![col]!!.isAtLast()) {
                        aliveItemCount = -1
                        break
                    }
                    aliveItemCount += _column!![col]!!.getAliveItemCount()
                    lastLocation = max(
                        _column!![col]!!.getLastCursor().getLocation(),
                        lastLocation
                    )
                }
                if ((aliveItemCount >= adapterItemCount) && (adapterItemCount > 0) && (scrollPosition + containerSize > headerSize + _firstMargin + _innerScrollMargin + lastLocation)) {
                    if (onLoadDataCallback != null) {
                        if (!_progressLoading) {
                            _progressLoading = true
                            onLoadDataCallback!!.onLoadDataCallback(_footerView)
                        }
                    } else if (onLoadDataCallbackTemp != null) {
                        break
                    }
                    super.addChild(_footerView!!)
                    val position: Float = (headerSize + _firstMargin + _innerScrollMargin + lastLocation) - scrollPosition
                    onPositionFooter(_footerView!!, position, true)
                    _isFooterInList = true
                    backAdded = true
                }
                break
            }
        }
        return frontAdded or backAdded
    }

    // scroll size 계산
    protected open fun measureScrollSize(): Float {
        var headerSize: Float = 0f
        if (_headerView != null && _headerView!!.isVisible()) {
            headerSize =
                if (isVertical()) _headerView!!.getContentSize().height else _headerView!!.getContentSize().width
        }
        var footerSize: Float = 0f
        if (_footerView != null && _footerView!!.isVisible()) {
            footerSize = if (isVertical()) _footerView!!.getContentSize().height else _footerView!!.getContentSize().width
        }

        // 스크롤 사이즈 최종 계산
        var scrollSize: Float = 0f
        _canExactScrollSize = false
        if (_hintIsFixedSize) {
            scrollSize = ceil(_lastItemCount.toFloat() / _numContainer.toFloat()) * _hintFixedChildSize
            _canExactScrollSize = true
        } else {
            var aliveItemCount: Int = 0
            for (col in 0 until _numContainer) {
                aliveItemCount += _column!![col]!!.getAliveItemCount()
            }
            if (aliveItemCount >= _lastItemCount) {
                // 마지막일때 정확한 계산
                for (col in 0 until _numContainer) {
                    scrollSize = max(scrollSize, _column!![col]!!.getLastCursor().getLocation())
                }
                _canExactScrollSize = true
            } else {
                // 마지막 아닐때 평균으로 계산(정확한 사이즈를 알수 없으므로...)
                if (aliveItemCount > 0) {
                    val containerSize: Float = if (isVertical()) _contentSize.height else _contentSize.width
                    var columnSizeTotal: Float = 0f
                    for (col in 0 until _numContainer) {
                        columnSizeTotal += _column!![col]!!.getLastCursor().getLocation()
                    }
                    scrollSize = (_lastItemCount * columnSizeTotal / aliveItemCount.toFloat()) / _numContainer.toFloat()
                    scrollSize += containerSize * 0.3f // 30%여분 추가
                }
                _justAtLast = false
            }
        }
        scrollSize += headerSize + footerSize + _firstMargin + _innerScrollMargin + _lastMargin
        return scrollSize
    }

    protected open fun scheduleScrollUpdate() {
        registerUpdate(SMTableView.FLAG_SCROLL_UPDATE)
        _scrollParent?.notifyScrollUpdate()
    }

    protected open fun unscheduleScrollUpdate() {
        unregisterUpdate(SMTableView.FLAG_SCROLL_UPDATE)
    }

    override fun updateScrollInParentVisit(deltaScroll: Float): Boolean {
        var deltaScroll: Float = deltaScroll
        _needUpdate = false
        _deltaScroll = 0f
        if (isUpdate(SMTableView.FLAG_SCROLL_UPDATE)) {
            _skipUpdateOnVisit = false
            onUpdateOnVisit()
            _skipUpdateOnVisit = true
        }
        deltaScroll = _deltaScroll
        return _needUpdate
    }


    override fun onUpdateOnVisit() {
        if (_skipUpdateOnVisit) {
            _skipUpdateOnVisit = false
            return
        }
        if (_contentSize.width <= 0 || _contentSize.height <= 0) {
            return
        }
        if (BuildConfig.DEBUG && (cellForRowAtIndexPath==null || numberOfRowsInSection==null)) {
            error("Assertion Failed")
        }
        if (_reloadFlag) {
            _reloadFlag = false
            _velocityTracker!!.clear()
            _scroller!!.reset()
            _scroller!!.setScrollPosition(getBaseScrollPosition())
            _lastScrollPosition = _scroller!!.getScrollPosition()
            _lastItemCount = 0
            _inScrollEvent = false
            _touchFocused = false
            _justAtLast = false
            _forceJumpPage = false
            _fillWithCellsFirstTime = false
            for (col in 0 until _numContainer) {
                val info: ColumnInfo = _column!![col]!!

                // 수행중인 Animation 종료
                val cursor: Cursor = Cursor(info.getFirstCursor())
                while (cursor._position < info.getViewLastCursor()._position) {

//                for (Cursor cursor = info.getFirstCursor(); cursor._position<info.getViewLastCursor()._position; cursor.inc(true)) {
                    if (cursor.getItem()!!._tag > 0) {
                        stopActionByTag(cursor.getItem()!!._tag)
                    }
                    cursor.inc(true)
                }

                // child 제거
                val numChild: Int = getChildrenCount(col)
                if (numChild > 0) {
                    val cursor = Cursor(info.getViewFirstCursor())
                    //                    Cursor cursor = info.getViewFirstCursor();
                    var i: Int = 0
                    while (i < numChild) {
                        val child: SMView = getChildAt(col, 0)!!
                        // reload면 reuse하지 않는다.
                        removeChild(col, child, true)
                        i++
                        cursor.inc(true)
                    }

                    // scrapper clear
                    _reuseScrapper.clear()

                    // holder clear
                    clearInstantHolder()
                }
                info.init(this, col)
                if (onLoadDataCallbackTemp != null) {
                    onLoadDataCallback = onLoadDataCallbackTemp
                    onLoadDataCallbackTemp = null
                }
            }
            if (!_reloadExceptHeader) {
                if (_headerView != null && _isHeaderInList) {
                    super.removeChild(_headerView, SMTableView.CLEANUP_FLAG)
                    _isHeaderInList = false
                }
            }
            //            _reloadExceptHeader = true;
            _reloadExceptHeader = false
            if (_footerView != null && _isFooterInList) {
                super.removeChild(_footerView, SMTableView.CLEANUP_FLAG)
                _isFooterInList = false
            }
        }
        if (_forceJumpPage) {
            return
        }
        var updated: Boolean = false
        val adapterItemCount: Int = numberOfRowsInSection!!.numberOfRowsInSection(0)
        if (_lastItemCount != adapterItemCount) {
            _lastItemCount = adapterItemCount
            updated = true
        }
        updated = updated or (_scroller as SMScroller).update()
        var scrollPosition: Float = _scroller!!.getScrollPosition()
        val containerSize: Float =
            if (isVertical()) _contentSize.height else _contentSize.width
        var headerSize: Float = 0f
        var footerSize: Float = 0f
        if (_refreshView != null) {
            var updateRefresh: Boolean = false
            if (_refreshState != _lastRefreshState) {
                if (_lastRefreshState == RefreshState.NONE) {
                    super.addChild(_refreshView!!, 1)
                }
                if (_refreshState == RefreshState.NONE) {
                    super.removeChild(_refreshView)
                } else if (_refreshState == RefreshState.REFRESHING) {
                    // 터치 release됨, Refresh 시작
                    _scroller!!.setHangSize(_refreshTriggerSize)
                    updated = updated or _scroller!!.update()
                    scrollPosition = _scroller!!.getScrollPosition()
                } else if (_lastRefreshState == RefreshState.REFRESHING && _refreshState == RefreshState.NONE) {
                    _scroller!!.setHangSize(0f)
                }
                updateRefresh = true
            }
            if (_refreshState != RefreshState.NONE) {
                _refreshSize = -_scroller!!.getScrollPosition()
                if (_refreshState == RefreshState.REFRESHING) {
                    if (_refreshSize < _refreshMinSize) {
                        _refreshSize =
                            max(-_scroller!!.getScrollPosition(), _refreshMinSize)
                    }
                } else {
                    _refreshSize = max(.0f, -_scroller!!.getScrollPosition())
                }
                if (_refreshSize != _lastRefreshSize) {
                    updateRefresh = true
                }
            } else {
                _refreshSize = 0f
            }
            if (updateRefresh) {
                _lastRefreshState = _refreshState
                _lastRefreshSize = _refreshSize
                if (getActionByTag(AppConst.TAG.ACTION_LIST_HIDE_REFRESH) == null) {
                    onRefreshDataCallback?.onRefreshDataCallback(_refreshView, _refreshState, _refreshSize)

                    // ToDo. 대충 기준을 10으로 잡자... 나중에 수정해야 함.
                    if (_refreshSize <= 10 && _refreshState == RefreshState.EXIT) {
                        _refreshState = RefreshState.NONE
                    }
                }
                updated = true
            }
        }
        if (_headerView != null) {
            headerSize =
                if (isVertical()) _headerView!!.getContentSize().height else _headerView!!.getContentSize().width
        }
        if (_footerView != null) {
            footerSize =
                if (isVertical()) _footerView!!.getContentSize().height else _footerView!!.getContentSize().width
        }
        if (_animationDirty) {
            _animationDirty = false
            positionChildren(scrollPosition, containerSize, headerSize, footerSize)
            val newScrollSize: Float = measureScrollSize()
            _scroller!!.setScrollSize(max(_minScrollSize, newScrollSize))
            if (!_justAtLast && _canExactScrollSize) {
                // 최초로 마지막에 도달했을 때 잘못된 오버스크롤 방지
                _justAtLast = true
                _scroller!!.justAtLast()
            }
            scrollPosition = _scroller!!.getScrollPosition()
        }
        positionChildren(scrollPosition, containerSize, headerSize, footerSize)
        clippingChildren(scrollPosition, containerSize, headerSize, footerSize)

//        boolean fillFlag = fillList(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize);
//        if (fillList(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize)) {
//        if (fillFlag) {
        if (fillList(adapterItemCount, scrollPosition, containerSize, headerSize, footerSize)) {
            _scroller!!.setScrollSize(max(_minScrollSize, measureScrollSize()))
            if (!_justAtLast && _canExactScrollSize) {
                // 최초로 마지막에 도달했을 때 잘못된 오버스크롤 방지
                _justAtLast = true
                _scroller!!.justAtLast()
            }
            if (adapterItemCount > 0) {
                if (!_fillWithCellsFirstTime) {
                    _fillWithCellsFirstTime = true
                    onInitFillWithCells?.onInitFillWithCells(this)
                    if (_initRefreshEnable && _refreshView != null) {
                        _initRefreshEnable = false
                        if (_scroller!!.getNewScrollPosition() <= 0) {
                            _scroller!!.setScrollPosition(-_refreshTriggerSize)
                        }
                        _scroller!!.setHangSize(_refreshTriggerSize)
                        _refreshState = RefreshState.REFRESHING
                        positionChildren(-_refreshTriggerSize, containerSize, headerSize, footerSize)
                        onRefreshDataCallback?.onRefreshDataCallback(_refreshView, _refreshState, _refreshTriggerSize)
                        updated = true
                    }
                }
            }
        }
        _needUpdate = true
        if (_lastScrollPosition != scrollPosition) {
            val distance: Float = (scrollPosition - _lastScrollPosition)
            onScrollChanged(scrollPosition, distance)
        } else if (!updated) {
            _needUpdate = false
            unscheduleScrollUpdate()
        }
        _deltaScroll = _lastScrollPosition - scrollPosition
        _lastScrollPosition = scrollPosition
    }

    protected open fun onScrollChanged(position: Float, distance: Float) {
        onScrollCallback?.onScrollCallback(position, distance)
    }

    protected open fun scrollFling(velocity: Float) {
        _scroller!!.onTouchFling(velocity)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Int {
        if (_forceJumpPage) {
            return SMView.Companion.TOUCH_TRUE
        }
        val action: Int = ev.getAction()
        val point: Vec2 = Vec2(ev.getX(), ev.getY())
        if ((_tableRect != null) && (action == MotionEvent.ACTION_DOWN) && !_tableRect!!.containsPoint(point)) {
            if (_lockScroll) {
                return SMView.Companion.TOUCH_TRUE
            }
        } else {
            if (_lockScroll) {
                return super.dispatchTouchEvent(ev)
            }
            if (!_inScrollEvent && _scroller!!.isTouchable()) {
                if (action == MotionEvent.ACTION_DOWN && _scroller!!.getState() !== SMScroller.STATE.STOP) {
                    stop()
                    scheduleScrollUpdate()
                }
                val ret: Int = super.dispatchTouchEvent(ev)
                if (ret == SMView.Companion.TOUCH_INTERCEPT) {
                    return SMView.Companion.TOUCH_INTERCEPT
                }
            }
        }
        if (_velocityTracker == null) {
            _velocityTracker = VelocityTracker.obtain()
        }
        val x: Float = point.x
        val y: Float = point.y
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                _inScrollEvent = false
                _lastMotionX = x
                _lastMotionY = y
                _firstMotionTime = _director!!.getGlobalTime()
                if (_accelScrollEnable) {
                    if (_scroller!!.getScrollSpeed() > 2000) {
                        _accelCount++
                    } else {
                        _accelCount = 0
                    }
                }
                _scroller!!.onTouchDown()
                if (_scrollRect != null && !_scrollRect!!.containsPoint(point)) {
                    _touchFocused = false
                    return SMView.Companion.TOUCH_FALSE
                }
                _touchFocused = true
                _velocityTracker!!.addMovement(ev)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                _touchFocused = false
                if (_inScrollEvent) {
                    _inScrollEvent = false
                    var vx: Float
                    var vy: Float
                    vx = _velocityTracker!!.getXVelocity(0)
                    vy = _velocityTracker!!.getYVelocity(0)

                    // Velocity tracker에서 계산되지 않았을때 보정..
                    if (vx == 0f && vy == 0f) {
                        val dt: Float = _director!!.getGlobalTime() - _firstMotionTime
                        if (dt > 0) {
                            val p1: Vec2 = _touchStartPosition
                            val p2: Vec2 = point
                            vx = -(p2.x - p1.x) / dt
                            vy = -(p2.y - p1.y) / dt
                        }
                    }

                    // Accelate scroll
                    var maxVelocity: Float = _maxVelocicy
                    if (_accelScrollEnable) {
                        val dt: Float = _director!!.getGlobalTime() - _firstMotionTime
                        if (dt < 0.15 && _accelCount > 3) {
                            maxVelocity *= (_accelCount - 2).toFloat()
                        }
                    }
                    if (isVertical()) {
                        if (abs(vy) > AppConst.Config.MIN_VELOCITY) {
                            if (abs(vy) > maxVelocity) {
                                vy = SMView.signum(vy) * maxVelocity
                            }
                            scrollFling(-vy)
                        } else {
                            _scroller!!.onTouchUp()
                        }
                        _lastVelocityY = vy
                        _lastVelocityX = 0f
                        _lastFlingTime = _director!!.getGlobalTime()
                    } else {
                        if (abs(vx) > AppConst.Config.MIN_VELOCITY) {
                            if (abs(vx) > maxVelocity) {
                                vx = SMView.signum(vx) * maxVelocity
                            }
                            scrollFling(-vx)
                        } else {
                            _scroller!!.onTouchUp()
                        }
                        _lastVelocityX = vx
                        _lastVelocityY = 0f
                        _lastFlingTime = _director!!.getGlobalTime()
                    }
                    scheduleScrollUpdate()
                } else {
                    _scroller!!.onTouchUp()
                    scheduleScrollUpdate()
                    _lastVelocityX = 0f
                    _lastVelocityY = 0f
                }
                _velocityTracker!!.clear()
                //                _velocityTracker.recycle();
//                _velocityTracker = null;

                // 터치로 놓을 때 refreshView 처리
                if (_refreshView != null && _refreshState != RefreshState.NONE) {
                    // float size = -_scroller->getScrollPosition();
                    when (_refreshState) {
                        RefreshState.ENTER -> {
                            _refreshState = RefreshState.EXIT
                            scheduleScrollUpdate()
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                _velocityTracker!!.addMovement(ev)
                val deltaX: Float
                val deltaY: Float
                if (!_inScrollEvent) {
                    deltaX = x - _lastMotionX
                    deltaY = y - _lastMotionY
                } else {
                    deltaX = point.x - _touchPrevPosition.x
                    deltaY = point.y - _touchPrevPosition.y
                }


                // 터치로 당길 때 refreshView 처리
                if (_refreshView != null && getActionByTag(AppConst.TAG.ACTION_LIST_HIDE_REFRESH) == null) {
                    val size: Float = _scroller!!.getScrollPosition()
                    when (_refreshState) {
                        RefreshState.NONE -> {
                            if (canRefreshData==null || !canRefreshData!!.canRefreshData()) {
                                if (size > 0) {
                                    // 최상단에서 아래로 당길때 RefreshView 추가
                                    _refreshState = RefreshState.ENTER
                                    scheduleScrollUpdate()
                                }
                            }
                        }
                        RefreshState.ENTER -> {
                            if (size < 0) {
                                _refreshState = RefreshState.NONE
                            } else if (size >= _refreshTriggerSize) {
                                // 발동 사이즈 이상이면 준비 상태로 전환 (삭제)... 나중에 ready가 필요하면 다시 넣자
                                // _refreshState = RefreshState.READY;

                                // 충분히 당겨지면 바로 발동하는 것으로 변경함 (추가)
                                _refreshState = RefreshState.REFRESHING
                            }
                            scheduleScrollUpdate()
                        }
                        RefreshState.READY -> {

                            // 나중에 쓰는 걸로...
                            if (size < 0) {
                                // refresh 시작 전 사이즈 0 이하면 취소
                                _refreshState = RefreshState.NONE
                            } else if (size < _refreshTriggerSize) {
                                // 발동 사이즈 이하로 내려가면 ENTER 상태로 전환
                                _refreshState = RefreshState.ENTER
                            }
                            scheduleScrollUpdate()
                        }
                        else -> {
                        }
                    }
                }
                if (_touchFocused && !_inScrollEvent) {
                    val ax: Float = x - _lastMotionX
                    val ay: Float = y - _lastMotionY

                    // 첫번째 스크롤 이벤트에서만 체크한다
                    val dir: Direction = SMView.Companion.getDirection(ax, ay)
                    if (isVertical()) {
                        if ((dir === Direction.UP || dir === Direction.DOWN) && abs(ay) > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true
                        }
                    } else {
                        if ((dir === Direction.LEFT || dir === Direction.RIGHT) && abs(ax) > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true
                        }
                    }
                    if (_inScrollEvent) {
                        if (_touchMotionTarget != null) {
                            cancelTouchEvent(_touchMotionTarget, ev)
                            _touchMotionTarget = null
                        }
                    }
                }
                if (_inScrollEvent) {
                    if (isVertical()) {
                        _scroller!!.onTouchScroll(deltaY)
                    } else {
                        _scroller!!.onTouchScroll(deltaX)
                    }
                    _lastMotionX = x
                    _lastMotionY = y
                    scheduleScrollUpdate()
                }
            }
        }
        if (_inScrollEvent) {
            return SMView.Companion.TOUCH_INTERCEPT
        }
        return SMView.Companion.TOUCH_TRUE
    }

    private fun findCursorForIndexPath(indexPath: IndexPath): FindCursorRet {
        if (BuildConfig.DEBUG && (indexPath.getIndex()<0 || indexPath.getIndex()!=numberOfRowsInSection!!.numberOfRowsInSection(0))) {
            error("Assertion Failed")
        }
        val index: Int = indexPath.getIndex()
        for (col in 0 until _numContainer) {
            val info: ColumnInfo = _column!![col]!!
            val begin = Cursor(info.getFirstCursor())
            val end = Cursor(info.getLastCursor())
            if (!info.getViewFirstCursor().isEnd()) {
                if (index < info.getViewFirstCursor().getIndexPath().getIndex()) {
                    end.set(info.getViewFirstCursor().inc(true))
                } else if (!info.getViewLastCursor().isEnd()) {
                    if (index < info.getViewLastCursor().getIndexPath().getIndex()) {
                        begin.set(info.getViewFirstCursor())
                        end.set(info.getViewLastCursor())
                    } else {
                        begin.set(info.getViewLastCursor())
                    }
                }
            }
            val cursor: Cursor = Cursor(begin)
            var count: Int = end._position - begin._position
            while (count > 0) {
                if (!cursor.getItem()!!.isDeleted()) {
                    if (cursor.getIndexPath().getIndex() === index) {
                        indexPath.set(IndexPath(0, col, index))
                        return FindCursorRet(cursor, true)
                    }
                }
                cursor.inc(true)
                --count
            }
        }
        return FindCursorRet(null, false)
    }

    private fun findCursorForChild(child: SMView): FindCursorRet {
        var retCursor: Cursor? = null
        for (col in 0 until _numContainer) {
            val numChild: Int = getChildrenCount(col)
            for (i in 0 until numChild) {
                if (child === getChildAt(col, i)) {
                    val info: ColumnInfo = _column!![col]!!
                    retCursor = Cursor(info.getViewFirstCursor(i))
                    if ((retCursor.getItem()!!._flags and ITEM_FLAG_DELETE) <= 0) {
                        return FindCursorRet(retCursor, true)
                    }
                    break
                }
            }
        }
        return FindCursorRet(retCursor, false)
    }

    private fun findChildForIndexPath(indexPath: IndexPath): FindCursorRet {
        val ret: FindCursorRet = findCursorForIndexPath(indexPath)
        if (!ret.retBool) {
            return FindCursorRet(null, false, null)
        }
        var retCursor: Cursor = ret.retCursor!!
        val info: ColumnInfo = _column!![indexPath.getColumn()]!!
        if (retCursor._position < info.getViewFirstCursor()._position || retCursor._position >= info.getViewLastCursor()._position) {
            return FindCursorRet(null, false, null)
        }
        val offset: Int = retCursor._position - info.getViewFirstCursor()._position
        if (offset < 0 || offset >= getChildrenCount(indexPath.getColumn())) {
            return FindCursorRet(null, false, null)
        }
        val view: SMView? = getChildAt(indexPath.getColumn(), retCursor._position - info!!.getViewFirstCursor()._position)
        return FindCursorRet(retCursor, true, view)
    }

    protected open fun stopAndCompleteChildAction(tag: Int) {
        if (tag <= 0) {
            return
        }
        val action: Action? = getActionByTag(tag)
        if (action != null) {
            var a: _BaseAction? = null
            if (action.javaClass == _DelaySequence::class.java) {
                val seq: _DelaySequence = action as _DelaySequence
                a = seq.getAction()
                a.complete()
                stopActionByTag(tag)
            } else {
                if (action.javaClass == _BaseAction::class.java) {
                    a = action as _BaseAction?
                    if (a != null) {
                        a.complete()
                        stopActionByTag(tag)
                    }
                }
            }
        }
    }

    private fun deleteCursor(column: Int, cursor: Cursor) {
        deleteCursor(column, cursor, true)
    }

    private fun deleteCursor(column: Int, cursor: Cursor, cleanup: Boolean) {
        val info: ColumnInfo = _column!![column]!!

        // 화면에 표시중이면 child 삭제
        if (cursor._position >= info.getViewFirstCursor()._position && cursor._position < info.getViewLastCursor()._position) {
            val position: Int = cursor._position - info.getViewFirstCursor()._position
            val child: SMView = getChildAt(column, position)!!
            val item: Item? = cursor.getItem()
            if (onCellDeleteCallback != null) {
                onCellDeleteCallback!!.onCellDeleteCallback(child, 1f)
            }
            if (child.javaClass == _DeleteNode::class.java || item!!._dontReuse) {
                removeChild(column, child, cleanup)
            } else {
                removeChildAndReuseScrap(column, item._reuseType, child, SMTableView.CLEANUP_FLAG)
            }
        }

        // column에서 cursor 삭제
        _column!![column]!!.deleteCursor(cursor)

        // 삭제 후 child reorder
        if (_column!![column]!!._data.size > 0) {
            val numChild: Int = getChildrenCount(column)
            if (numChild > 0) {
                val c: Cursor = Cursor(_column!![column]!!.getViewFirstCursor())
                for (i in 0 until numChild) {
                    val child: SMView? = getChildAt(column, i)
                    child?.setLocalZOrder(c.getPosition())
                    c.inc(false)
                }
                sortAllChildren(column)
            }
        }
    }

    private fun performDelete(cursor: Cursor, child: SMView, duration: Float, delay: Float): Boolean {
        return performDelete(cursor, child, duration, delay, true)
    }

    private fun performDelete(cursor: Cursor, child: SMView, duration: Float, delay: Float, cleanup: Boolean): Boolean {
        if (cursor.getItem()!!.isDeleted()) {
            return false
        }
        val column: Int = cursor.getIndexPath().getColumn()
        for (col in 0 until _numContainer) {
            _column!![col]!!.markDeleteCursor(cursor)
        }
        val item: Item = cursor.getItem()!!
        if (item._tag > 0) {
            val a: Action? = getActionByTag(item._tag)
            if (a != null) {
                stopAction(a)
            }
        }
        _column!![column]!!._resizeReserveSize = -(item._size).toInt()
        if (duration > 0 || delay > 0) {
            // 애니메이션
            val delteAction: _BaseAction = _DeleteAction(getDirector(), this, column, cursor)
            delteAction.setDuration(duration)
            var action: Action? = null
            if (delay > 0) {
                action = _DelaySequence(getDirector(), delay, delteAction)
            } else {
                action = delteAction
            }
            item._flags = item._flags or ITEM_FLAG_RESIZE
            item._tag = _internalActionTag++
            action.setTag(item._tag)
            runAction(action)
        } else {
            item._newSize = 0f
            _column!![column]!!.resizeCursor(cursor)
            deleteCursor(column, cursor, cleanup)
        }
        scheduleScrollUpdate()
        return true
    }

    private fun performResize(cursor: Cursor, child: SMView, newSize: Float, duration: Float, delay: Float): Boolean {
        val item: Item = cursor.getItem()!!
        if (item._tag > 0) {
            stopAndCompleteChildAction(item._tag)
        }
        val indexPath: IndexPath = cursor.getIndexPath()
        _column!![indexPath.getIndex()]!!._resizeReserveSize += (newSize - item._size).toInt()
        if (duration > 0 || delay > 0) {
            // 애니메이션
            val resizeAction: _BaseAction =
                _ResizeAction(getDirector(), this, indexPath.getColumn(), cursor, newSize)
            resizeAction.setDuration(duration)
            var action: Action? = null
            if (delay > 0) {
                action = _DelaySequence(getDirector(), delay, resizeAction)
            } else {
                action = resizeAction
            }
            item._tag = _internalActionTag++
            action.setTag(item._tag)
            runAction(action)
        } else {
            item._newSize = newSize
            _column!![indexPath.getColumn()]!!.resizeCursor(cursor)
            _animationDirty = true
        }
        scheduleScrollUpdate()
        return true
    }

    override fun setMinScrollSize(minScrollSize: Float) {
        super.setMinScrollSize(minScrollSize)
        _scroller!!.setScrollSize(max(_minScrollSize, _scroller!!.getScrollSize()))
    }

    open fun setScrollPosition(position: Float) {
        _scroller!!.setScrollPosition(position)
    }

    open fun reloadData() {
        reloadData(false)
    }

    open fun reloadData(bExceptHeader: Boolean) {
        _reloadExceptHeader = bExceptHeader
        _reloadFlag = true
        scheduleScrollUpdate()
    }

    // update date.. 보통 아래 또는 위로 추가되었을경우
    open fun updateData() {
        _animationDirty = true
        scheduleScrollUpdate()
    }


    override fun notifyScrollUpdate() {
        scheduleScrollUpdate()
    }

    open fun reloadRowsAtIndexPath(indexPath: IndexPath) {
        val ret: FindCursorRet = findCursorForIndexPath(indexPath)
        if (!ret.retBool) {
            return
        }
        ret.retCursor!!.getItem()!!._reload = true
        scheduleScrollUpdate()
    }

    open fun getCellForIndexPath(indexPath: IndexPath): SMView? {
        val ret: FindCursorRet = findChildForIndexPath(indexPath)
        val retView: SMView? = ret.retView
        if (retView == null || retView.javaClass == _DeleteNode::class.java) {
            // 해당 cell 찾지 못함 or 이미 지워짐
            return null
        }
        return ret.retView
    }

    open fun getIndexPathForCell(child: SMView): IndexPath? {
        val ret: FindCursorRet = findCursorForChild(child)
        if (!ret.retBool) {
            // 해당 item 찾지 못함 or 이미 지워짐
            return null
        }
        return ret.retCursor!!.getIndexPath()
    }

    open fun insertRowAtIndexPath(indexPath: IndexPath, estimateSize: Float): Boolean {
        return insertRowAtIndexPath(indexPath, estimateSize, 0f)
    }

    open fun insertRowAtIndexPath(indexPath: IndexPath, estimateSize: Float, duration: Float): Boolean {
        return insertRowAtIndexPath(indexPath, estimateSize, duration, 0f)
    }

    open fun insertRowAtIndexPath(indexPath: IndexPath, estimateSize: Float, duration: Float, delay: Float): Boolean {
        return insertRowAtIndexPath(indexPath, estimateSize, duration, delay, false)
    }

    open fun insertRowAtIndexPath(indexPath: IndexPath, estimateSize: Float, duration: Float, delay: Float, immediate: Boolean): Boolean {

        if (BuildConfig.DEBUG && (numberOfRowsInSection==null || cellForRowAtIndexPath==null || (indexPath.getIndex()<0 || indexPath.getIndex()>numberOfRowsInSection!!.numberOfRowsInSection(0)))) {
            error("Assertion Failed")
        }

        lateinit var info: ColumnInfo
        var column: Int = -1
        var lastLocation: Float = Float.MAX_VALUE
        var lastIndex: Int = Int.MIN_VALUE
        for (col in _numContainer - 1 downTo 0) {

            info = _column!![col]!!

            lastIndex = max(lastIndex, info._lastIndexPath.getIndex())
            if (info._data.size == 0) {
                column = col
                lastLocation = 0f
            } else {
                if (info.getLastCursor().getLocation() + info._resizeReserveSize < lastLocation) {
                    column = 0
                    lastLocation = info.getLastCursor().getLocation() + info._resizeReserveSize
                }
            }
        }
        if (BuildConfig.DEBUG && column == -1) {
            error("Assertion failed")
        }
        if (indexPath.getIndex() > lastIndex + 1) {
            // 아직 붙지 않은 cell이면 나중에 하고 지금은 넘어간다.
            return true
        }
        lateinit var cursor: Cursor
        for (col in 0 until _numContainer) {
            if (col == column) {
                cursor = _column!![col]!!.insertItem(
                    IndexPath(0, column, indexPath.getIndex()),
                    -1,
                    estimateSize
                )
            } else {
                _column!![col]!!.markInsertItem(IndexPath(0, column, indexPath.getIndex()))
            }
        }
        val item: Item? = cursor.getItem()
        info = _column!![column]!!
        var needchild: Boolean =
            cursor._position >= info.getViewFirstCursor()._position && cursor._position < info.getViewLastCursor()._position
        var addChildOnTop: Boolean = false
        if (immediate) {
            if (info.getViewFirstCursor()._position > info.getFirstCursor()._position && cursor._position == info.getViewFirstCursor(
                    -1
                )._position
            ) {
                addChildOnTop = true
                needchild = true
            } else if (cursor._position == info.getViewLastCursor(1)._position) {
                needchild = true
            }
        }
        if (needchild && cursor._position == info.getViewLastCursor(-1)._position) {
            var headerSize = 0f
            if (_headerView != null) {
                headerSize =
                    if (isVertical()) _headerView!!.getContentSize().height else _headerView!!.getContentSize().width
            }

            // view 안의 위치
            val location: Float =
                headerSize + _firstMargin + _scroller!!.getScrollPosition() + cursor.getLocation()
            val containerSize: Float =
                if (isVertical()) _contentSize.height else _contentSize.width
            if (location > containerSize) {
                info.retreatViewLast()
                needchild = false
            }
        }
        val needAnimation: Boolean =
            (duration > 0 || delay > 0) && (needchild || cursor._position < info!!.getViewFirstCursor()._position)
        if (!needAnimation) {
            item!!._newSize = estimateSize
            info!!.resizeCursor(cursor)
        }
        if (needchild) {
            // 화면에 즉시 보여야 한다.
            var childIndex: Int = 0
            val numChild: Int = getChildrenCount(column)

            // 1) 현재 children reorder
            val c: Cursor = Cursor(info!!.getViewFirstCursor())
            while (childIndex < numChild && c._position < info!!.getViewLastCursor()._position) {
                if (c._position == cursor._position) {
                    c.inc(true)
                    continue
                }
                val child: SMView? = getChildAt(column, childIndex++)
                child?.setLocalZOrder(c.getPosition())
                c.inc(true)
            }
            if (addChildOnTop) {
                info.advanceViewFirst()
            }

            // 2) child 추가
            _reuseScrapper._internalReuseType = -1
            _reuseScrapper._internalReuseNode = null
            val child: SMView = cellForRowAtIndexPath!!.cellForRowAtIndexPath(item!!._indexPath)
            if (_reuseScrapper._internalReuseType >= 0) {
                item._reuseType = _reuseScrapper._internalReuseType
            }

            // order
            child.setLocalZOrder(cursor.getPosition())
            var headerSize: Float = 0f
            if (_headerView != null) {
                headerSize =
                    if (isVertical()) _headerView!!.getContentSize().height else _headerView!!.getContentSize().width
            }

            // view 안의 위치
            val locationInView: Float = headerSize + _firstMargin - _scroller!!.getScrollPosition()
            onPositionCell(child, cursor.getLocation() + locationInView, true)
            addChild(column, child)
            if (_reuseScrapper._internalReuseNode != null && _reuseScrapper._internalReuseNode === child) {
                _reuseScrapper.popBack(_reuseScrapper._internalReuseType)
            }
            _reuseScrapper._internalReuseType = -1
            _reuseScrapper._internalReuseNode = null
            if (!needAnimation) {
                item!!._newSize = estimateSize
                info!!.resizeCursor(cursor)
            }
            if (onCellResizeCallback != null) {
                onCellResizeCallback!!.onCellResizeCallback(child, item!!._newSize)
            }
            if (!needAnimation && onCellInsertCallback != null) {
                onCellInsertCallback!!.onCellInsertCallback(child, 1f)
            }
            sortAllChildren(column)
        }
        if (needAnimation) {
            // animation
            val insertAction: _BaseAction =
                _InsertAction(getDirector(), this, column, cursor, estimateSize)
            insertAction.setDuration(duration)
            var action: Action? = null
            if (delay > 0) {
                action = _DelaySequence(getDirector(), delay, insertAction)
            } else {
                action = insertAction
            }
            item!!._flags = ITEM_FLAG_RESIZE or ITEM_FLAG_INSERT
            item!!._tag = _internalActionTag++
            action.setTag(item!!._tag)
            runAction(action)
        } else {
            info!!.resizeCursor(cursor)
            if (!needchild) {
                // child도 없고 animation도 없으면 flags에 표시만 해둔다.
                // => 추가되는 순간 onCellInsertCallback 호출됨
                cursor.getItem()!!._flags = ITEM_FLAG_INSERT
            }
        }
        scheduleScrollUpdate()
        return false
    }

    open fun deleteRowForCell(child: SMView): Boolean {
        return deleteRowForCell(child, 0f)
    }

    open fun deleteRowForCell(child: SMView, duration: Float): Boolean {
        return deleteRowForCell(child, duration, 0f)
    }

    open fun deleteRowForCell(child: SMView, durationt: Float, delayt: Float): Boolean {
        var duration: Float = durationt
        var delay: Float = delayt
        val ret: FindCursorRet = findCursorForChild(child)
        if (!ret.retBool) {
            // child가 없거나 delete중임
            return false
        }
        if (ret.retCursor!!._position >= _column!!.get(ret.retCursor!!.getIndexPath().getColumn())!!.getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0f
            delay = 0f
        }
        return performDelete(ret.retCursor!!, child, duration, delay)
    }

    open fun popCell(child: SMView): SMView? {
        val ret: FindCursorRet = findCursorForChild(child)
        if (!ret.retBool) {
            return null
        }
        if (ret.retCursor!!._position < _column!!.get(ret.retCursor!!.getIndexPath().getColumn())!!.getViewFirstCursor()._position
            || ret.retCursor!!._position >= _column!![ret.retCursor!!.getIndexPath().getColumn()]!!.getViewLastCursor()._position ) {
            return null
        }
        ret.retCursor!!.getItem()!!._dontReuse = true
        if (performDelete(ret.retCursor!!, child, 0f, 0f, false)) {
            return child
        }
        return null
    }

    // 일반 method
    open fun isDeleteCell(child: SMView): Boolean {
        val ret: FindCursorRet = findCursorForChild(child)
        if (!ret.retBool) {
            // child가 없거나 delete중임
            return true
        }
        return false
    }


    open fun deleteRowAtIndexPath(indexPath: IndexPath): Boolean {
        return deleteRowAtIndexPath(indexPath, 0f)
    }

    open fun deleteRowAtIndexPath(indexPath: IndexPath, duration: Float): Boolean {
        return deleteRowAtIndexPath(indexPath, 0f, 0f)
    }

    open fun deleteRowAtIndexPath(indexPath: IndexPath, durationt: Float, delayt: Float): Boolean {
        // 일단 0 section만
        var duration: Float = durationt
        var delay: Float = delayt
        if (BuildConfig.DEBUG && !(indexPath.getIndex() >= 0 && indexPath.getIndex() <= numberOfRowsInSection!!.numberOfRowsInSection(0))) {
            error("Assertion failed")
        }
        var lastIndex: Int = Int.MIN_VALUE
        for (col in 0 until _numContainer) {
            lastIndex = max(lastIndex, _column!![col]!!.getLastIndexPath().getIndex())
        }
        if (indexPath.getIndex() > lastIndex) {
            // 아직 생성되지 않은 Item이면 바로 지운다.
            return true
        }
        val ret: FindCursorRet = findChildForIndexPath(indexPath)
        if (ret.retView == null) {
            // 해당 item 찾지 못함 or 이미 지워짐
            return false
        }
        if (ret.retCursor!!._position >= _column!!.get(indexPath.getColumn())!!.getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0f
            delay = 0f
        }
        return performDelete(ret.retCursor!!, ret.retView!!, duration, delay)
    }

    open fun resizeRowForCell(child: SMView, newSize: Float): Boolean {
        return resizeRowForCell(child, newSize, 0f)
    }

    open fun resizeRowForCell(child: SMView, newSize: Float, duration: Float): Boolean {
        return resizeRowForCell(child, newSize, 0f, 0f)
    }

    open fun resizeRowForCell(child: SMView, newSize: Float, durationt: Float, delayt: Float): Boolean {
        var duration: Float = durationt
        var delay: Float = delayt
        val ret: FindCursorRet = findCursorForChild(child)
        if (!ret.retBool) {
            // 해당 item 찾지 못함 or 이미 지워짐
            return false
        }
        if (ret.retCursor!!._position >= _column!!.get(ret.retCursor!!.getIndexPath().getColumn())!!.getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0f
            delay = 0f
        }
        return performResize(ret.retCursor!!, child, newSize, duration, delay)
    }

    open fun resizeRowForIndexPath(indexPath: IndexPath, newSize: Float): Boolean {
        return resizeRowForIndexPath(indexPath, newSize, 0f)
    }

    open fun resizeRowForIndexPath(indexPath: IndexPath, newSize: Float, duration: Float): Boolean {
        return resizeRowForIndexPath(indexPath, newSize, duration, 0f)
    }

    open fun resizeRowForIndexPath(indexPath: IndexPath, newSize: Float, durationt: Float, delayt: Float): Boolean {
        var duration: Float = durationt
        var delay: Float = delayt
        val ret: FindCursorRet = findChildForIndexPath(indexPath)
        if (!ret.retBool) {
            return false
        }
        if (ret.retCursor!!._position >= _column!!.get(ret.retCursor!!.getIndexPath().getColumn())!!.getViewLastCursor()._position) {
            // 화면 밖 뒤에 있으면 즉시 삭제 (애니메이션 필요 없음)
            duration = 0f
            delay = 0f
        }
        return performResize(ret.retCursor!!, ret.retView!!, newSize, duration, delay)
    }

    open fun getVisibleCells(): ArrayList<SMView> {
        return getVisibleCells(0)
    }

    open fun getVisibleCells(column: Int): ArrayList<SMView> {
        if (BuildConfig.DEBUG && column !in 0 until _numContainer) {
            error("Assertion failed")
        }
        return _contentView!![column]!!.getChildren()
    }

    // current scroll position
    open fun getScrollPosition(): Float {
        return _scroller!!.getScrollPosition()
    }

    // scroll size... 이거는 끝까지 가봐야 안다... 정확하지 않음. (fixed인경우 계산에 의해 뱉어낼 수 있음)
    open fun getScrollSize(): Float {
        if (_scroller!!.getScrollSize() <= 0 && numberOfRowsInSection != null) {
            measureScrollSize()
        }
        return _scroller!!.getScrollSize()
    }

    // for override
    // scroll to
    open fun scrollTo(positiont: Float) {
        var position: Float = positiont
        if (_scroller!!.javaClass == FlexibleScroller::class.java) {
            val scroller: FlexibleScroller = _scroller as FlexibleScroller
            if (position < 0) {
                position = 0f
            } else {
                if (measureScrollSize() != 0f) {
                    val scrollSize: Float = scroller.getScrollSize()
                    if (position > scrollSize) {
                        position = scrollSize
                    }
                }
            }
            scroller.scrollTo(position)
            scheduleScrollUpdate()
        }
    }

    open fun scrollToWithDuration(position: Float) {
        scrollToWithDuration(position, 0f)
    }

    open fun scrollToWithDuration(position: Float, duration: Float) {
        if (_scroller!!.javaClass == FlexibleScroller::class.java) {
            val scroller: FlexibleScroller = _scroller as FlexibleScroller
            scroller.scrollToWithDuration(position, duration)
            scheduleScrollUpdate()
        }
    }

    // scroll by
    open fun scrollBy(offset: Float) {
        scrollTo(_scroller!!.getScrollPosition() + offset)
    }

    open fun scrollByWithDuration(offset: Float, duration: Float) {
        scrollToWithDuration(_scroller!!.getScrollPosition() + offset, duration)
    }

    // refresh data view (당겨서 새로고침 할때, 새로고침 뷰... 로딩뷰를 여기에 넣으면 됨)
    open fun setRefreshDataView(cell: SMView, triggerSize: Float) {
        setRefreshDataView(cell, triggerSize, -1f)
    }

    open fun setRefreshDataView(cell: SMView, triggerSize: Float, minSize: Float) {
        if (_refreshView != null && _refreshView !== cell) {
            super.removeChild(_refreshView, true)
            _refreshView = null
        }
        _refreshView = cell
        _refreshTriggerSize = triggerSize
        if (minSize < 0) {
            _refreshMinSize = _refreshTriggerSize
        } else {
            _refreshMinSize = minSize
        }
        scheduleScrollUpdate()
    }

    // refresh 끝났음을 알려줘야한다. refresh data view가 들어가야하므로.
    open fun endRefreshData() {
        if (_refreshView == null || _refreshState != RefreshState.REFRESHING) return
        _refreshState = RefreshState.EXIT
        _scroller!!.setHangSize(0f)
        _scroller!!.onTouchUp()
        scheduleScrollUpdate()
    }

    // true이면 그만 부른다.
    open fun endLoadData(bNeedMore: Boolean) {
        if (!bNeedMore) {
            // 더이상 로드할 데이터가 없음
            //        setFooterView(nullptr);
            if (onLoadDataCallback != null) {
                onLoadDataCallbackTemp = onLoadDataCallback
                onLoadDataCallback = null
            }
            if (_footerView != null && _isFooterInList) {
                super.removeChild(_footerView, true)
            }
            _isFooterInList = false
            _justAtLast = false
        }
        _progressLoading = false
        updateData()
    }

    // max scroll velocity
    open fun setMaxScrollVelocity(maxVelocity: Float) {
        var v: Float = maxVelocity
        if (v < AppConst.Config.MIN_VELOCITY) {
            v = AppConst.Config.MIN_VELOCITY + 1
        }
        _maxVelocicy = v
    }

    open fun enableAccelerateScroll(enable: Boolean) {
        _accelScrollEnable = enable
    }

    open fun enableInitRefresh(enable: Boolean) {
        _initRefreshEnable = true
    }

    override fun setHeaderView(headerView: SMView?) {
        super.setHeaderView(headerView)
        scheduleScrollUpdate()
    }

    override fun setFooterView(footerView: SMView?) {
        super.setFooterView(footerView)
        scheduleScrollUpdate()
    }

    open fun getScrollSpeed(): Float {
        return _scroller!!.getScrollSpeed()
    }

    open fun getTotalHeightInSection(section: Int): Float {
        return _column!![section]!!.getLastCursor().getLocation()
    }

    // 페이지 끝까지 scroll 해야 전체 사이즈를 알 수 있기 때문에 fake로 끝까지 가본것 처럼 한다.
    open fun fakeAdvanceLast(index: Int, size: Float) {
        _column!![0]!!.advanceViewLast(IndexPath(index), 0, size)
        _column!![0]!!.retreatViewFirst()
    }

    open fun fakeAdvanceLast2(index: Int, size: Float) {
        _column!![0]!!.advanceViewLast(IndexPath(index), 0, size)
    }


    protected open fun isVertical(): Boolean {
        return _orient == SMTableView.Orientation.VERTICAL
    }

    protected open fun isHorizontal(): Boolean {
        return _orient == SMTableView.Orientation.HORIZONTAL
    }

    protected open fun initFixedColumnInfo(numPages: Int, pageSize: Float, initPage: Int) {
        for (i in 0 until numPages) {
            _column!![0]!!.advanceViewLast(IndexPath(0, 0, i), -1, pageSize)
        }
        if (initPage == 0) {
            _column!![0]!!.rewindViewLastCursor()
        } else {
            val cursor = Cursor(_column!![0]!!.getFirstCursor(initPage))
            _column!![0]!!.setViewCursor(cursor)
            _column!![0]!!.retreatViewLast()
        }
    }


    class ReuseScrapper {
        constructor() {

        }
        private var _numberOfTypes: Int = 0
        private val _key: HashMap<String, Int> = HashMap()
        private val _data: ArrayList<ArrayList<SMView?>> = ArrayList()
        var _internalReuseType: Int = 0
        var _internalReuseNode: SMView? = null

        fun getReuseType(reuseIdentifier: String): Int {
            val reuseType: Int
            val value:Int? = _key[reuseIdentifier]
            if (value == null) {
                // not exist?
                reuseType = _numberOfTypes
                _key[reuseIdentifier] = _numberOfTypes++
                _data.add(java.util.ArrayList<SMView?>())
            } else {
                reuseType = value
            }
            return reuseType
        }

        @JvmOverloads
        fun scrap(reuseType: Int, parent: SMView?, child: SMView?, cleanup: Boolean = true) {
            val queue: ArrayList<SMView?> = _data[reuseType]
            parent?.removeChild(child, cleanup)
            if (child!!.javaClass != _DeleteNode::class.java) {
                queue.add(child)
                _data[reuseType] = queue
            }
        }

        fun popBack(reuseType: Int) {
            val al: java.util.ArrayList<SMView?> = _data.get(reuseType)
            if (al.size > 0) {
                al.removeAt(al.size - 1)
            }
        }

        fun back(reuseType: Int): SMView? {
            val al: java.util.ArrayList<SMView?> = _data.get(reuseType)
            if (al.size > 0) {
                return al.get(al.size - 1)
            }
            return null
        }

        fun clear() {
            for (i in _data.indices) {
                _data.get(i).clear()
            }
        }
    }

    class Item {
        constructor() {
            _indexPath = IndexPath(0, 0)
            _reuseType = 0
            _size = 0f
            _newSize = 0f
            _flags = 0
            _state = null
            _tag = 0
            _reload = false
            _dontReuse = false
        }

        constructor(indexPath: IndexPath, type: Int, size: Float) {
            _indexPath.set(indexPath)
            _reuseType = type
            _size = size
            _newSize = size
            _flags = 0
            _state = null
            _tag = 0
            _reload = false
            _dontReuse = false
        }

        constructor(item: Item) {
            _indexPath.set(item._indexPath)
            _reuseType = item._reuseType
            _size = item._size
            _newSize = item._newSize
            _flags = item._flags
            _state = item._state
            _tag = item._tag
            _reload = item._reload
            _dontReuse = item._dontReuse
        }

        fun get(): Item {
            return this
        }

        var _indexPath: IndexPath = IndexPath(0, 0)
        var _reuseType: Int
        var _tag: Int
        var _size: Float
        var _newSize: Float
        var _flags: Int
        var _reload: Boolean
        var _dontReuse: Boolean
        fun isDeleted(): Boolean {
            return (_flags and ITEM_FLAG_DELETE) != 0
        }

        private var _state: SceneParams?
    }

    class ItemIterator {
        constructor() {
            _data = null
            _curIndex = -1
        }

        constructor(iter: ItemIterator?) {
            setIterator(iter)
        }

        constructor(data: ArrayList<Item>?) {
            setIterator(data)
        }

        constructor(data: ArrayList<Item>, index: Int) {
            setIterator(data, index)
        }

        fun setIterator(iter: ItemIterator?) {
            setIterator(iter?._data, iter?._curIndex ?: 0)
        }

        fun setIterator(data: ArrayList<Item>?) {
            setIterator(data, 0)
        }

        fun setIterator(data: ArrayList<Item>?, index: Int) {
            _data = data
            _curIndex = index
        }

        fun isBegin(): Boolean {
            return _curIndex == 0
        }

        fun isEnd(): Boolean {
            val bEnd: Boolean = _curIndex == _data!!.size
            return bEnd
        }

        fun begin(): ItemIterator {
            this._curIndex = 0
            return this
        }

        fun end(): ItemIterator {
            if (_data != null) {
                this._curIndex = _data!!.size
            } else {
                this._curIndex = 1
            }
            return this
        }

        fun getCurrentIndex(): Int {
            return _curIndex
        }

        @JvmOverloads
        fun inc(before: Boolean = true): ItemIterator {
            if (before) {
                _curIndex++
                return this
            } else {
                val tmp: ItemIterator = ItemIterator(this)
                this._curIndex++
                return tmp
            }
        }

        @JvmOverloads
        fun dec(before: Boolean = true): ItemIterator {
            if (before) {
                _curIndex--
                return this
            } else {
                val tmp: ItemIterator = ItemIterator(this)
                this._curIndex--
                return tmp
            }
        }

        fun getItem(): Item? {
            if ((_data != null) && (_data!!.size > 0) && (_curIndex >= 0) && (_curIndex < _data!!.size)) {
                return _data!![_curIndex]
            }
            return null
        }

        var _curIndex: Int = -1
        var _data: ArrayList<Item>? = null
    }

    class Cursor {
        constructor() {
            _iter = ItemIterator()
            _location = 0f
            _position = 0
        }

        constructor(cursor: Cursor) {
            if (this === cursor) {
                return
            }
            set(cursor)
        }

        fun set(cursor: Cursor) {
            if (this === cursor) {
                return
            }
            this._iter = ItemIterator(cursor.getIterator())
            this._location = cursor._location
            this._position = cursor._position
        }

        fun equal(cursor: Cursor): Boolean {
            return this._position == cursor._position
        }

        fun notequal(cursor: Cursor): Boolean {
            return this._position != cursor._position
        }

        fun inc(before: Boolean): Cursor {
            if (before) {
                if (!isEnd()) {
                    ++_position
                    _location += getItem()!!._size
                    // iter를 다음으로 하나 늘린다.
                    _iter!!.inc(true)
                }
                return this
            } else {
                val tmp: Cursor = Cursor(this)
                if (!isEnd()) {
                    ++_position
                    _location += getItem()!!._size
                    _iter!!.inc(true)
                }
                return tmp
            }
        }

        fun dec(before: Boolean): Cursor {
            if (before) {
                if (!isBegin()) {
                    --_position
                    _iter!!.dec(true)
                    _location -= getItem()!!._size
                }
                return this
            } else {
                val tmp: Cursor = Cursor(this)
                if (!isBegin()) {
                    --_position
                    _iter!!.dec(true)
                    _location -= getItem()!!._size
                }
                return tmp
            }
        }

        fun init(data: java.util.ArrayList<Item>?) {
            _iter = ItemIterator(data).end()
            _position = 0
            _location = 0f
        }

        fun advance(offset: Int): Cursor {
            var offset: Int = offset
            if (offset > 0) {
                while (offset > 0) {
                    this.inc(true)
                    offset--
                }
            } else if (offset < 0) {
                while (offset < 0) {
                    this.dec(true)
                    offset++
                }
            }
            return this
        }

        fun getItem(): Item? {
            return _iter!!.getItem()
        }

        fun getIndexPath(): IndexPath {
            return _iter!!.getItem()!!._indexPath
        }

        fun getLocation(): Float {
            return _location
        }

        fun getLastLocation(): Float {
            if (getItem() != null) {
                return _location + getItem()!!._size
            }
            return _location
        }

        fun offsetLocation(offset: Float) {
            _location += offset
        }

        fun getPosition(): Int {
            return _position
        }

        fun offsetPosition(offset: Int) {
            _position += offset
        }

        fun setPosition(position: Int) {
            _position = position
        }

        fun offset(position: Int, location: Float) {
            _position += position
            _location += location
        }

        fun getIterator(): ItemIterator? {
            return _iter
        }

        fun setIterator(iter: ItemIterator) {
            _iter!!.setIterator(iter._data, iter._curIndex)
        }

        fun incIterator() {
            _iter!!.inc(true)
        }

        fun decIterator() {
            _iter!!.dec(true)
        }

        fun incPosition() {
            ++_position
        }

        fun decPosition() {
            --_position
        }

        fun isBegin(): Boolean {
            return _iter!!.isBegin()
        }

        fun isEnd(): Boolean {
            return _iter!!.isEnd()
        }

        var _iter: ItemIterator? = null
        var _position: Int = 0
        var _location: Float = 0.0f
    }

    class ColumnInfo constructor() {
        fun init(parent: SMTableView?, column: Int) {
            _parent = parent
            _column = column
            _numAliveItem = 0
            _lastIndexPath = IndexPath(0, _column, 0)
            _resizeReserveSize = 0
            _data.clear()
            _firstCursor.init(_data)
            _lastCursor.init(_data)
            _viewFirstCursor.init(_data)
            _viewLastCursor.init(_data)
            _buffer.clear()
        }

        fun advanceViewFirst(): Cursor {
            _viewFirstCursor.dec(true)
            return _viewFirstCursor
        }

        fun advanceViewLast(indexPath: IndexPath, type: Int, size: Float): Cursor {
            return advanceViewLast(indexPath, type, size, 0)
        }

        fun advanceViewLast(indexPath: IndexPath, type: Int, size: Float, flags: Int): Cursor {
            val cursor = Cursor()
            if (_data.size == 0) {
                // 첫번째 데이터
                _data.add(Item(indexPath, type, size))
                _firstCursor.setIterator(ItemIterator(_data))
                _lastCursor.set(_firstCursor)
                _lastCursor.inc(true)
                _viewFirstCursor.set(_firstCursor)
                _viewLastCursor.set(_lastCursor)
                _lastIndexPath.set(indexPath)
                _numAliveItem++
                cursor.set(_viewFirstCursor)
            } else if (_viewLastCursor.isEnd()) {
                // 마지막이면 끝에 계속 Attach
                cursor.set(_viewLastCursor)
                val lastIndex: Int = _viewLastCursor.getIterator()!!.getCurrentIndex()
                _data.add(lastIndex, Item(indexPath, type, size))
                cursor.setIterator(ItemIterator(_data, lastIndex))
                _viewLastCursor.set(cursor)
                _lastCursor.set(_viewLastCursor.inc(true))
                _lastIndexPath.set(indexPath)
                _numAliveItem++
            } else {
                // 처음도 아니고 마지막도 아니면 이미 생성되어 있는 어중간한 넘...
                cursor.set(_viewLastCursor.inc(false))
            }
            return cursor
        }

        fun retreatViewFirst(): Cursor {
            _viewFirstCursor.inc(true)
            return _viewFirstCursor
        }

        fun retreatViewLast(): Cursor {
            return _viewLastCursor.dec(true)
        }

        fun getFirstCursor(): Cursor {
            return _firstCursor
        }

        fun getLastCursor(): Cursor {
            return _lastCursor
        }

        fun getViewFirstCursor(): Cursor {
            return _viewFirstCursor
        }

        fun getViewLastCursor(): Cursor {
            return _viewLastCursor
        }

        fun getFirstCursor(offset: Int): Cursor {
            return _firstCursor.advance(offset)
        }

        fun getLastCursor(offset: Int): Cursor {
            return _lastCursor.advance(offset)
        }

        fun getViewFirstCursor(offset: Int): Cursor {
            return Cursor(_viewFirstCursor).advance(offset)
        }

        fun getViewLastCursor(offset: Int): Cursor {
            return Cursor(_viewLastCursor).advance(offset)
        }

        fun rewindViewLastCursor() {
            _viewLastCursor.set(_firstCursor)
        }

        fun setViewCursor(cursor: Cursor) {
            _viewFirstCursor.set(cursor)
            _viewLastCursor.set(cursor)
            _viewLastCursor.inc(false)
        }

        fun resizeCursor(cursor: Cursor) {
            if (cursor.getItem()!!._size != cursor.getItem()!!._newSize) {
                val item: Item = cursor.getItem()!!
                val deltaSize: Float = item._newSize - item._size
                item._size = item._newSize
                val c:ArrayList<Cursor> = arrayListOf(Cursor(_viewFirstCursor), Cursor(_viewLastCursor), Cursor(_lastCursor))
                for (i in 0..2) {
                    if (c[i]._position > cursor._position) {
                        c[i].offsetLocation(deltaSize)
                    }
                }
                _viewFirstCursor.set(c[0])
                _viewLastCursor.set(c[1])
                _lastCursor.set(c[2])
                resizeCursorBuffer(cursor, deltaSize)
                _resizeReserveSize -= deltaSize.toInt()
            }
        }

        fun markDeleteCursor(cursor: Cursor) {
            if (BuildConfig.DEBUG && cursor.getItem() == null) {
                error("Assertion failed")
            }
            if (_data.size == 0) {
                return
            }
            if (_column != cursor.getIndexPath().getColumn()) {
                val indexPath: IndexPath = IndexPath(cursor.getItem()!!._indexPath)

//                Cursor c;
                // 뒤에서 부터 찾아봄
                if (!_viewLastCursor.isEnd() && _viewLastCursor.getIndexPath()
                        .lessequal(indexPath)
                ) {
//                    c = _viewLastCursor;
                    // 경계점까지 cursor 증가
                    while (_viewLastCursor._position < _lastCursor._position && _viewLastCursor.getIndexPath()
                            .lessequal(indexPath)
                    ) {
                        _viewLastCursor.inc(true)
                    }


                    // 나머지 모든 index 감소시킴
                    while (!_viewLastCursor.getIterator()!!.isEnd()) {
                        val item: Item = _viewLastCursor.getIterator()!!.getItem()!!
                        item._indexPath.dec()
                        _viewLastCursor.getIterator()!!.inc()
                    }
                } else if (!_viewFirstCursor.isEnd() && _viewFirstCursor.getIndexPath()
                        .lessequal(indexPath)
                ) {
//                    c = _viewFirstCursor;
                    // 경계점까지 cursor 증가
                    while (_viewFirstCursor._position < _lastCursor._position && _viewFirstCursor.getIndexPath()
                            .lessequal(indexPath)
                    ) {
                        _viewFirstCursor.inc(true)
                    }


                    // 나머지 모든 index 감소시킴
                    while (!_viewFirstCursor.getIterator()!!.isEnd()) {
                        val item: Item = _viewFirstCursor.getIterator()!!.getItem()!!
                        item._indexPath.dec()
                        _viewFirstCursor.getIterator()!!.inc()
                    }
                } else {
//                    c = _firstCursor;
                    // 경계점까지 cursor 증가
                    while (_firstCursor._position < _lastCursor._position && _firstCursor.getIndexPath()
                            .lessequal(indexPath)
                    ) {
                        _firstCursor.inc(true)
                    }


                    // 나머지 모든 index 감소시킴
                    while (!_firstCursor.getIterator()!!.isEnd()) {
                        val item: Item = _firstCursor.getIterator()!!.getItem()!!
                        item._indexPath.dec()
                        _firstCursor.getIterator()!!.inc()
                    }
                }
            } else {
                // 현재 컬럼에서 삭제
                _numAliveItem--
                if (BuildConfig.DEBUG && _numAliveItem < 0) {
                    error("Assertion failed")
                }

                // Cursor 에 삭제된 아이템 표시
                cursor.getItem()!!._flags = cursor.getItem()!!._flags or ITEM_FLAG_DELETE
                val c: Cursor = Cursor(cursor)
                c.inc(true)

                // 나머지 모든 index 감소시킴
                while (!c.getIterator()!!.isEnd()) {
                    val item: Item = c.getIterator()!!.getItem()!!
                    item._indexPath.dec()
                    c.getIterator()!!.inc()
                }
                if (_numAliveItem == 0) {
                    // 모든 아이템 삭제됨.
                    _lastIndexPath.set(0, 0, 0)
                }
            }

            // lastIndex 세팅
            if (_numAliveItem > 0) {
                val iter: ListIterator<Item> = _data.listIterator(_data.size)
                while (iter.hasPrevious()) {
                    val item: Item = iter.previous()
                    if (!item.isDeleted()) {
                        _lastIndexPath.set(item._indexPath)
                        break
                    }
                }
            }
        }

        fun deleteCursor(cursor: Cursor) {
            deleteCursorBuffer(cursor)
            val c: ArrayList<Cursor> = arrayListOf(_firstCursor, _viewFirstCursor, _viewLastCursor, _lastCursor)
            for (i in 0..3) {
                if (cursor._position < c[i]._position) {
                    c[i].offset(-1, -cursor.getItem()!!._size)
                } else if (cursor._position == c[i]._position) {
                    c[i].incIterator()
                }
            }
            _firstCursor.set(c[0])
            _viewFirstCursor.set(c[1])
            _viewLastCursor.set(c[2])
            _lastCursor.set(c[3])
            _data.remove(cursor.getItem())
        }

        fun markInsertItem(indexPath: IndexPath): Cursor {
            val cursor: Cursor
            if (_data.size > 0) {
                // 다른 컬럼
                // index보다 큰거나 같은 item index + 1

//                Cursor c;
                // 뒤에서 부터 찾아봄
                if (!_viewLastCursor.isEnd() && _viewLastCursor.getIndexPath()
                        .lessequal(indexPath)
                ) {
                    while (_viewLastCursor._position < _lastCursor._position && _viewLastCursor.getIndexPath()
                            .lessthan(indexPath)
                    ) {
                        _viewLastCursor.inc(true)
                    }

                    // 이 위치에 추가
                    cursor = Cursor(_viewLastCursor)

                    // 나머지 모든 index 증시킴
                    while (!_viewLastCursor.getIterator()!!.isEnd()) {
                        val item: Item = _viewLastCursor.getIterator()!!.getItem()!!
                        item._indexPath.inc()
                        _viewLastCursor.getIterator()!!.inc()
                    }
                } else if (!_viewFirstCursor.isEnd() && _viewFirstCursor.getIndexPath()
                        .lessequal(indexPath)
                ) {
//                    c = _viewFirstCursor;
                    while (_viewFirstCursor._position < _lastCursor._position && _viewFirstCursor.getIndexPath()
                            .lessthan(indexPath)
                    ) {
                        _viewFirstCursor.inc(true)
                    }

                    // 이 위치에 추가
                    cursor = Cursor(_viewFirstCursor)

                    // 나머지 모든 index 증가시킴
                    while (!_viewFirstCursor.getIterator()!!.isEnd()) {
                        val item: Item = _viewFirstCursor.getIterator()!!.getItem()!!
                        item._indexPath.inc()
                        _viewFirstCursor.getIterator()!!.inc()
                    }
                } else {
//                    c = _firstCursor;
                    while (_firstCursor._position < _lastCursor._position && _firstCursor.getIndexPath()
                            .lessthan(indexPath)
                    ) {
                        _firstCursor.inc(true)
                    }

                    // 이 위치에 추가
                    cursor = Cursor(_firstCursor)

                    // 나머지 모든 index 증가시킴
                    while (!_firstCursor.getIterator()!!.isEnd()) {
                        val item: Item = _firstCursor.getIterator()!!.getItem()!!
                        item._indexPath.inc()
                        _firstCursor.getIterator()!!.inc()
                    }
                }
                for (i in _data.indices.reversed()) {
                    val item: Item = _data[i]
                    if (!item.isDeleted()) {
                        _lastIndexPath.set(item._indexPath)
                        break
                    }
                }
            } else { // _data.size() == 0
                // 첫번째 새로운 데이터 (최초추가)
                cursor = Cursor(_firstCursor)
            }
            return cursor
        }

        fun insertItem(indexPath: IndexPath, type: Int, estimateSize: Float): Cursor {
            val cursor: Cursor = Cursor(markInsertItem(indexPath))
            if (indexPath.greaterthan(_lastIndexPath)) {
                _lastIndexPath.set(indexPath)
            }

            //cursor.setIterator(_data.insert(cursor.getIterator(), Item(indexPath, type, 0)));
            val lastIndex: Int = cursor.getIterator()!!.getCurrentIndex()
            _data.add(lastIndex, Item(indexPath, type, 0f))
            cursor.setIterator(ItemIterator(_data, lastIndex))
            val c:ArrayList<Cursor> = arrayListOf(_viewFirstCursor, _viewLastCursor, _lastCursor)
            for (i in 0..2) {
                if (c[i]._position >= cursor._position) {
                    c[i].offset(1, cursor.getItem()!!._size)
                }
            }
            _viewFirstCursor.set(c[0])
            _viewLastCursor.set(c[1])
            _lastCursor.set(c[2])
            if (cursor.isBegin()) {
                _firstCursor.set(cursor)
            }
            _numAliveItem++
            insertCursorBuffer(cursor)
            _resizeReserveSize += estimateSize.toInt()
            return cursor
        }

        fun isAtFirst(): Boolean {
            return _viewFirstCursor._position == _firstCursor._position
        }

        fun isAtLast(): Boolean {
            return _viewLastCursor._position == _lastCursor._position
        }

        fun getAliveItemCount(): Int {
            return _numAliveItem
        }

        fun getLastIndexPath(): IndexPath {
            return _lastIndexPath
        }

        fun obtainCursorBuffer(cursor: Cursor): Cursor {
            val cloneCursor: Cursor = Cursor(cursor)
            _buffer.add(cloneCursor)
            return cloneCursor
        }

        fun recycleCursorBuffer(cursor: Cursor) {
            val iter: ListIterator<Cursor> = _buffer.listIterator()
            while (iter.hasNext()) {
                val c: Cursor = iter.next()
                if (c.equal(cursor)) {
                    _buffer.remove(c)
                    break
                }
            }
        }

        fun resizeCursorBuffer(targetCursor: Cursor, deltaSize: Float) {
            val iter: ListIterator<Cursor> = _buffer.listIterator()
            while (iter.hasNext()) {
                val c: Cursor = iter.next()
                if (c._position > targetCursor._position) {
                    c.offsetLocation(deltaSize)
                }
            }
        }

        fun deleteCursorBuffer(targetCursor: Cursor) {
            val item: Item? = targetCursor.getItem()
            if (item != null) {
                val size: Float = item._size
                val iter: ListIterator<Cursor> = _buffer.listIterator()
                while (iter.hasNext()) {
                    val c: Cursor = iter.next()
                    if (c._position > targetCursor._position) {
                        c.offset(-1, -size)
                    } else if (c._position == targetCursor._position) {
                        c.incIterator()
                    }
                }
            }
        }

        fun insertCursorBuffer(targetCursor: Cursor) {
            val item: Item? = targetCursor.getItem()
            if (item != null) {
                val size: Float = item._size
                val iter: ListIterator<Cursor> = _buffer.listIterator()
                while (iter.hasNext()) {
                    val c: Cursor = iter.next()
                    if (c._position >= targetCursor._position) {
                        c.offset(+1, +size)
                    }
                }
            }
        }

        var _data: java.util.ArrayList<Item> = java.util.ArrayList<Item>()
        var _firstCursor: Cursor = Cursor()
        var _lastCursor: Cursor = Cursor()
        var _viewFirstCursor: Cursor = Cursor()
        var _viewLastCursor: Cursor = Cursor()
        var _numAliveItem: Int = 0
        var _lastIndexPath: IndexPath = IndexPath(0, 0)
        var _column: Int = 0
        var _resizeReserveSize: Int = 0
        private var _parent: SMTableView? = null
        private val _buffer: java.util.ArrayList<Cursor> = java.util.ArrayList<Cursor>()
    } // ColumnInfo class


    open class _DeleteNode constructor(director: IDirector) : SMView(director) {}


    // 각종 Animation Action
    open class _BaseAction constructor(director: IDirector) : ActionInterval(director) {
        fun getChild(): SMView? {
            val info: ColumnInfo = _parent?._column!![_col]!!
            val offset: Int = _cursor.getPosition() - info.getViewFirstCursor().getPosition()
            if ((_cursor._position < info.getViewFirstCursor()._position)
                || (_cursor._position >= info.getViewLastCursor()._position)
                || (_parent?.getChildrenCount(_col) ?: 0 <= offset)
            ) {
                return null
            }
            return _parent?._contentView!![_col]!!.getChildren().get(offset)
        }

        override fun startWithTarget(target: SMView?) {
            super.startWithTarget(target)
            _startSize = _cursor.getItem()!!._size
        }

        open fun complete() {}
        protected var _col: Int = 0
        protected var _startSize: Float = 0f
        protected var _cursor: Cursor = Cursor()
        var _parent: SMTableView? = null
    }

    class _DeleteAction constructor(director: IDirector, parent: SMTableView, column: Int, cursor: Cursor) : _BaseAction(director) {
        override fun update(t: Float) {
            val item: Item = _cursor.getItem()!!
            item._newSize = _startSize * (1 - t)
            var child: SMView? = getChild()
            if (child != null) {
                if (_parent!!.onCellResizeCallback != null) {
                    _parent!!.onCellResizeCallback!!.onCellResizeCallback(child, item._newSize)
                }
                if (t < 1) {
                    if (_parent!!.onCellDeleteCallback != null) {
                        _parent!!.onCellDeleteCallback!!.onCellDeleteCallback(child, t)
                    }
                }
            } else {
                child = _parent!!.findFromHolder(item.hashCode())
                if (child != null) {
                    if (_parent!!.onCellResizeCallback != null) {
                        _parent!!.onCellResizeCallback!!.onCellResizeCallback(child, item._newSize)
                    }
                    if (_parent!!.onCellDeleteCallback != null) {
                        _parent!!.onCellDeleteCallback!!.onCellDeleteCallback(child, t)
                    }
                }
            }
            if (t < 1) {
                _parent!!._column!![_col]!!.resizeCursor(_cursor)
                _parent!!.scheduleScrollUpdate()
                _parent!!._animationDirty = true
            } else {
                complete()
            }
        }

        override fun complete() {
            val item: Item = _cursor.getItem()!!
            val childInHolder: SMView? = _parent!!.findFromHolder(item.hashCode())
            if (childInHolder != null) {
                _parent!!._reuseScrapper.scrap(item._reuseType, null, childInHolder)
                _parent!!.eraseFromHolder(item.hashCode())
            }
            _parent!!._column!![_col]!!.resizeCursor(_cursor)
            _parent!!.deleteCursor(_col, _cursor)
            _parent!!._column!![_col]!!.recycleCursorBuffer(_cursor)
            item._tag = 0
            item._flags = 0
            _parent!!.scheduleScrollUpdate()
            _parent!!._animationDirty = true
            if (_parent!!.onCellDeleteCompletionCallback != null) {
                _parent!!.onCellDeleteCompletionCallback!!.onCellDeleteCompletionCallback()
            }
        }

        init {
            _parent = parent
            _col = column
            _cursor = parent._column!![column]!!.obtainCursorBuffer(cursor)
        }
    }

    open class _ResizeAction constructor(director: IDirector, parent: SMTableView, column: Int, cursor: Cursor, newSize: Float) : _BaseAction(director) {
        fun updateResize(t: Float): SMView? {
            val item: Item = _cursor.getItem()!!
            item._newSize = _startSize + (_newSize - _startSize) * t
            _parent!!._column!![_col]!!.resizeCursor(_cursor)
            val child: SMView? = getChild()
            if (child != null) {
                if (_parent!!.onCellResizeCallback != null) {
                    _parent!!.onCellResizeCallback!!.onCellResizeCallback(child, item._newSize)
                }
                if (_insert && _parent!!.onCellInsertCallback != null) {
                    _parent!!.onCellInsertCallback!!.onCellInsertCallback(child, t)
                }
            }
            _parent!!.scheduleScrollUpdate()
            _parent!!._animationDirty = true
            return child
        }

        override fun update(t: Float) {
            if (t < 1) {
                updateResize(t)
            } else {
                complete()
            }
        }

        override fun complete() {
            val item: Item = _cursor.getItem()!!
            val child: SMView? = updateResize(1f)
            if (child == null) {
                // 완료시 화면에 child가 없으면 다음번 add될때 최종 resize한다.
                item._flags = ITEM_FLAG_RESIZE
                if (_insert) {
                    item._flags = item._flags or ITEM_FLAG_INSERT
                }
            } else {
                item._flags = 0
            }
            item._tag = 0
            _parent!!._column!![_col]!!.recycleCursorBuffer(_cursor)
            _parent!!.scheduleScrollUpdate()
            _parent!!._animationDirty = true
            if (_parent!!.onCellResizeCompletionCallback != null) {
                _parent!!.onCellResizeCompletionCallback!!.onCellResizeCompletionCallback(child)
            }
        }

        protected var _newSize: Float = 0f
        protected var _insert: Boolean = false

        init {
            _parent = parent
            _col = column
            _cursor = parent._column!![column]!!.obtainCursorBuffer(cursor)
            _newSize = newSize
            _insert = false
        }
    }

    class _InsertAction constructor(director: IDirector, parent: SMTableView, column: Int, cursor: Cursor, newSize: Float) : _ResizeAction(director, parent, column, cursor, newSize) {
        init {
            _parent = parent
            _col = column
            _cursor = parent._column!![column]!!.obtainCursorBuffer(cursor)
            _newSize = newSize
            _insert = true
        }
    }

    class _DelaySequence constructor(director: IDirector, delay: Float, action: _BaseAction) : Sequence(director) {
        private val _action: _BaseAction
        fun getAction(): _BaseAction {
            return _action
        }

        init {
            initWithTwoActions(
                create(getDirector(), max(0.0f, delay)),
                action
            )
            _action = action
        }
    }

    class _PageJumpAction(director: IDirector, parent: SMTableView, cursor: Cursor, pageSize: Float, fromPage: Int, toPage: Int, direction: Int)  : _BaseAction(director) {
        private var _pageSize: Float = pageSize
        private var _fromPage: Int = fromPage
        private var _toPage: Int = toPage
        private var _direction: Int = direction

        init {
            _parent = parent
            _cursor = parent._column!![0]!!.obtainCursorBuffer(cursor)
        }

        override fun update(t: Float) {
            var position: Float
            if (_direction < 0) {
                position = -_pageSize + _pageSize * t
                for (i in 0..1) {
                    if (i < _parent!!._contentView!![0]!!.getChildrenCount()) {
                        _parent!!.getChildAt(0, i)!!.setPositionX(position)
                    }
                    position += _pageSize
                }
            } else {
                position = -_pageSize * t
                for (i in 0..1) {
                    if (i < _parent!!._contentView!![0]!!.getChildrenCount()) {
                        _parent!!.getChildAt(0, i)!!.setPositionX(position)
                    }
                    position += _pageSize
                }
            }
            val p1: Float = _fromPage * _pageSize
            val p2: Float = _toPage * _pageSize
            val scrollPosition: Float = p1 + t * (p2 - p1)
            _parent!!.onScrollChanged(scrollPosition, 0f)
            if (t >= 1.0) {
                complete()
            }
        }

        override fun complete() {
            var child: SMView? = null
            val index: Int = if (_direction > 0) 0 else 1
            if (index < _parent!!.getChildrenCount(0)) {
                child = _parent!!.getChildAt(0, index)
            }
            if (child != null) {
                val item: Item = _cursor.getItem()!!
                _parent!!.removeChildAndReuseScrap(0, item._reuseType, child, true)
            }
            _parent!!._column!![0]!!.setViewCursor(_cursor)
            _parent!!._column!![0]!!.recycleCursorBuffer(_cursor)
            _parent!!._forceJumpPage = false
            _parent!!._currentPage = _toPage
            _parent!!._scroller!!.setScrollPosition(_pageSize * _toPage)
            _parent!!.scheduleScrollUpdate()
        }


    }

    class FindCursorRet {
        var retCursor:Cursor? = null
        var retBool:Boolean = false
        var retView:SMView? = null

        constructor(cursor:Cursor?, retbool: Boolean) {
            retCursor = cursor
            retView = null
            retBool = retbool
        }
        constructor(cursor:Cursor?, retbool:Boolean, view:SMView?) {
            retCursor = cursor
            retBool = retbool
            retView = view
        }

    }

    private fun removeChildAndReuseScrap(container: Int, reuseType: Int, child: SMView?, cleanup: Boolean) {
        if (reuseType >= 0) {
            _reuseScrapper.scrap(reuseType, _contentView!![container], child, cleanup)
        } else {
            removeChild(container, child!!, cleanup)
        }
    }
}