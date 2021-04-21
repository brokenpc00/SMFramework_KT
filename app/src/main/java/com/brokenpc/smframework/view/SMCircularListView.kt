package com.interpark.smframework.view

import android.view.MotionEvent
import android.view.VelocityTracker
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.scroller.FinityScroller
import com.brokenpc.smframework.base.scroller.InfinityScroller
import com.brokenpc.smframework.base.scroller.SMScroller
import com.brokenpc.smframework.base.types.TransformAction
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.tweenfunc
import com.interpark.smframework.base.types.ICircularCell
import cz.msebera.android.httpclient.cookie.SM
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class SMCircularListView(director: IDirector): SMView(director), SMScroller.ALIGN_CALLBACK {

    private var _reuseScrapper: ReuseScrapper? = null
    private var _velocityTracker: VelocityTracker? = null
    private var _scroller: SMScroller? = null
    private var _config = Config()

    private var _lastScrollPosition = 0f
    private var _lastMotionX = 0f
    private var _lastMotionY = 0f
    private var _deltaScroll = 0f
    private var _deleteIndex = -1
    private var _inScrollEvent = false
    private var _touchFocused = false
    private var _needUpdate = false
    private var _actionLock = false
    private var _fillWithCellsFirstTime = false
    private var _currentPage = -1
    private var _firstMotionTime = 0f
    var cellDeleteUpdate: CellDeleteUpdate? = null
    var scrollAlignedCallback: ScrollAlignedCallback? = null
    var numberOfRows: NumberOfRows? = null
    var pageScrollCallback: PageScrollCallback? = null
    var initFillWithCells: InitFillWithCells? = null
    var positionCell: PositionCell? = null
    var cellForRowsAtIndex: CellForRowsAtIndex? = null

    companion object {
        const val ACTION_TAG_CONSUME = 1000
        const val ACTION_TAG_POSITION = 1001

        @JvmStatic
        fun sortFunc(cells: ArrayList<ICircularCell>) {
            cells.sortWith {l, r -> if (l.getCellPosition() > r.getCellPosition()) -1 else if (l.getCellPosition()>r.getCellPosition()) 1 else 0}
        }
    }

    fun setCircularConfig(config: Config) {_config = config}

    fun dequeueReusableCellWithIdentifier(identifier: String): SMView? {
        _reuseScrapper?._internalReuseIdentifier = identifier
        _reuseScrapper?._internalReuseNode = _reuseScrapper?.back(identifier)
        return _reuseScrapper?._internalReuseNode
    }

    interface CellForRowsAtIndex {
        fun cellForRowsAtIndex(index: Int): SMView?
    }

    fun getIndexForCell(cell: SMView): Int {
        if (numberOfRows==null) return -1

        val children = getChildren()
        for (child in children) {
            if (cell==child && child is ICircularCell) {
                val iCell = child as ICircularCell

                if (iCell!=null) {
                    return convertToIndex(iCell.getCellIndex(), numberOfRows!!.numberOfRows())
                }
            }
        }
        return -1
    }

    interface PositionCell {
        fun positionCell(cell: SMView, position: Float, created: Boolean)
    }

    interface InitFillWithCells {
        fun initFillWithCells()
    }

    interface PageScrollCallback {
        fun pageScrollCallback(pagePosition: Float)
    }

    fun deleteCell(target: SMView, deleteDt: Float, deleteDelay: Float, positionDt: Float, positionDelay: Float): Boolean {
        return deleteCell(getIndexForCell(target), deleteDt, deleteDelay, positionDt, positionDelay)
    }

    fun deleteCell(targetIndex: Int, deleteDt: Float, deleteDelay: Float, positionDt: Float, positionDelay: Float): Boolean {
        val numRows = numberOfRows!!.numberOfRows()

        if (numRows<=1 || targetIndex<0 || targetIndex>=numRows) return false

        _deleteIndex = convertToIndex(targetIndex, numRows)

        var children = getChildren()

        if (children.size==0) return false

        val cells:ArrayList<ICircularCell> = ArrayList()

        for (child in children) {
            if (child is ICircularCell) {
                val iCell = child as ICircularCell
                if (iCell!=null) {
                    cells.add(iCell)
                }
            }
        }

        sortFunc(cells)

        var realIndex:Int = cells[0].getCellIndex()
        var index:Int = convertToIndex(realIndex, numRows)
        val diff:Int = realIndex - index

        var deleteCount = 0
        for (iCell in cells) {
            val idx = convertToIndex(iCell.getCellIndex(), numRows)
            if (idx==targetIndex) {
                iCell.markDelete()
                deleteCount++
            }

            iCell.setCellIndex(iCell.getCellIndex()-diff-deleteCount)
        }

        val scrollPosition = _scroller!!.getScrollPosition()
        val maxScrollSize = _config.cellSize*numRows + _config.preloadPadding*2f
        val adjPosition = scrollPosition - _config.anchorPosition - _config.preloadPadding
        val norPosition = if (adjPosition>0) {adjPosition % maxScrollSize} else {(maxScrollSize - abs(adjPosition % maxScrollSize)) % maxScrollSize}

        _scroller?.setScrollPosition(norPosition)

        var count = deleteCount
        val lastCell = cells[cells.size-1]
        realIndex = lastCell.getCellIndex()+2
        var position = lastCell.getCellPosition() + _config.cellSize

        while (count>0) {
            index = convertToIndex(realIndex, numRows)

            if (index!=targetIndex) {
                _reuseScrapper!!._internalReuseIdentifier = ""
                _reuseScrapper!!._internalReuseNode = null

                val cell = cellForRowsAtIndex?.cellForRowsAtIndex(index)
                if (cell!=null && cell is ICircularCell) {
                    val iCell = cell as ICircularCell
                    iCell.setCellIndex(realIndex)
                    iCell.setReuseIdentifier(_reuseScrapper!!._internalReuseIdentifier)
                    addChild(cell)

                    if (_reuseScrapper!!._internalReuseNode==cell) {
                        _reuseScrapper!!.popBack(_reuseScrapper!!._internalReuseIdentifier)
                    }

                    iCell.setCellPosition(position)
                    if (positionCell!=null) {
                        positionCell!!.positionCell(cell, position, true)
                    }

                    position += _config.cellSize
                }

                _reuseScrapper!!._internalReuseIdentifier = ""
                _reuseScrapper!!._internalReuseNode = null

                if (cell==null) break

                count--
            }

            realIndex++
        }

        children = getChildren()
        cells.clear()

        for (child in children) {
            if (child is ICircularCell) {
                val iCell = child as ICircularCell
                cells.add(iCell)
            }
        }

        sortFunc(cells)

        position = cells[0].getCellPosition()

        val deleteCells:ArrayList<SMView> = ArrayList()
        val remainCells:ArrayList<SMView> = ArrayList()

        for (iCell in cells) {
            index = convertToIndex(iCell.getCellIndex(), numRows)
            val child = iCell as SMView
            if (iCell.isDeleted()) {
                iCell.setAniSrc(iCell.getCellPosition())
                iCell.setAniDst(position)
                iCell.setAniIndex(iCell.getCellIndex())
                position += _config.cellSize

                remainCells.add(child)
            } else {
                deleteCells.add(child)
            }
        }

        val deleteAction = CellsActionCreate(getDirector())
        deleteAction.setTag(ACTION_TAG_CONSUME)
        deleteAction.setTargetCells(deleteCells)
        deleteAction.setTimeValue(deleteDt, deleteDelay)
        runAction(deleteAction)

        val positionAction = CellsActionCreate(getDirector())
        positionAction.setTag(ACTION_TAG_POSITION)
        positionAction.setTargetCells(remainCells)
        positionAction.setTimeValue(positionDt, positionDelay)
        runAction(positionAction)

        _actionLock = true
        return true
    }

    interface NumberOfRows {
        fun numberOfRows(): Int
    }

    interface ScrollAlignedCallback {
        fun scrollAlignedCallback(aligned: Boolean, index: Int, force: Boolean)
    }

    interface CellDeleteUpdate {
        fun cellDeleteUpdate(cell: SMView, dt: Float)
    }

    fun stop() {
        stop(true)
    }

    fun stop(align: Boolean) {
        unscheduleScrollUpdate()

        _scroller?.onTouchDown()
        if (align) {
            _scroller?.onTouchUp()
        }
    }

    fun setScrollPosition(position: Float) {
        _scroller?.setScrollPosition(position)
        if (numberOfRows!=null) {
            positionChildren(position, numberOfRows!!.numberOfRows())
        }
    }

    fun scrollByWithDuration(distance: Float, dt: Float) {
        if (abs(distance)>0f){
            if (_scroller is InfinityScroller) {
                (_scroller as InfinityScroller).scrollByWithDuration(distance, dt)
                scheduleScrollUpdate()
            }
        }
    }

    fun runFling(velocity: Float) {
        _scroller?.onTouchFling(velocity, 0)
        scheduleScrollUpdate()
    }

    fun getAlignedIndex(): Int {
        var scrollPosition = 0f
        if (_scroller!=null) {
            scrollPosition = (_scroller!!.getNewScrollPosition().toInt()*10)/10f
        }

        return if (scrollPosition==0f) {(scrollPosition/_config.cellSize).toInt()} else {floor(scrollPosition/_config.cellSize).toInt()}
    }

    fun updateData() {scheduleScrollUpdate()}

    override fun dispatchTouchEvent(event: MotionEvent): Int {
        if (_actionLock) return TOUCH_TRUE

        val action = event.action

        val x = event.x
        val y = event.y
        val point = Vec2(x, y)

        if (!_inScrollEvent && _scroller?.isTouchable() == true) {
            if (action==MotionEvent.ACTION_DOWN && _scroller!!.getState()!=SMScroller.STATE.STOP) {
                scheduleScrollUpdate()
            }
            val ret = super.dispatchTouchEvent(event)
            if (ret== TOUCH_INTERCEPT) return ret
        }

        if (_velocityTracker==null) {
            _velocityTracker = VelocityTracker.obtain()
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                _inScrollEvent = false
                _touchFocused = true

                _lastMotionX = x
                _lastMotionY = y
                _firstMotionTime = _director!!.getGlobalTime()

                _scroller!!.onTouchDown()

                _velocityTracker!!.addMovement(event)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                _touchFocused = false
                if (_inScrollEvent) {
                    _inScrollEvent = false

                    var vx = _velocityTracker!!.getXVelocity(0)
                    var vy = _velocityTracker!!.getYVelocity(0)

                    if (isVertical()) {
                        if (abs(vy)>200f) {
                            if (abs(vy)>_config.maxVelocity) {
                                vy = signum(vy) * _config.maxVelocity
                            } else if (_config.minVelocity>0 && abs(vy)<_config.minVelocity) {
                                vy = signum(vy) * _config.minVelocity
                            }
                            _scroller!!.onTouchFling(-vy, _currentPage)
                        } else {
                            _scroller!!.onTouchUp()
                        }
                    } else {
                        if (abs(vx)>200f) {
                            if (abs(vx)>_config.maxVelocity) {
                                vx = signum(vx) * _config.maxVelocity
                            } else if (_config.minVelocity>0 && abs(vx)<_config.minVelocity) {
                                vx = signum(vx) * _config.minVelocity
                            }
                            _scroller!!.onTouchFling(-vx, _currentPage)
                        } else {
                            _scroller!!.onTouchUp()
                        }
                    }
                    scheduleScrollUpdate()
                } else {
                    _scroller!!.onTouchUp()
                    scheduleScrollUpdate()
                }

                _velocityTracker!!.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                _velocityTracker!!.addMovement(event)

                var deltaX:Float
                var deltaY:Float

                if (!_inScrollEvent) {
                    deltaX = x - _lastMotionX
                    deltaY = y - _lastMotionY
                } else {
                    deltaX = point.x - _touchPrevPosition.x
                    deltaY = point.y - _touchPrevPosition.y
                }

                if (_touchFocused && !_inScrollEvent) {
                    val ax = x - _lastMotionX
                    val ay = y - _lastMotionY

                    // check at first scroll event time
                    val dir = getDirection(ax, ay)
                    if (isVertical()) {
                        if ((dir==Direction.UP || dir==Direction.DOWN) && abs(ay) > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true
                        }
                    } else {
                        if ((dir==Direction.LEFT || dir==Direction.RIGHT) && abs(ax) > AppConst.Config.SCROLL_TOLERANCE) {
                            _inScrollEvent = true
                        }
                    }

                    if (_inScrollEvent) {
                        if (_touchMotionTarget!=null) {
                            cancelTouchEvent(_touchMotionTarget, event)
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
            else -> {
                // do nothing
            }
        }

        if (_inScrollEvent) {
            return TOUCH_INTERCEPT
        }

        return TOUCH_TRUE
    }

    fun initWithConfig(config: Config): Boolean {
        _config = config

        _reuseScrapper = ReuseScrapper()

        if (_config.circular) {
            _scroller = InfinityScroller(getDirector())
        } else {
            _scroller = FinityScroller(getDirector())
        }

        _scroller!!.setCellSize(_config.cellSize)
        _scroller!!.setWindowSize(_config.windowSize)
        _scroller!!.setScrollMode(_config.scrollMode)
        _scroller!!.onAlignCallback = this

        _lastScrollPosition = _scroller!!.getScrollPosition()

        scheduleScrollUpdate()

        return true
    }

    protected fun scheduleScrollUpdate() {registerUpdate(VIEWFLAG_POSITION)}
    protected fun unscheduleScrollUpdate() {unregisterUpdate(VIEWFLAG_POSITION)}

    override fun onUpdateOnVisit() {
        if (_contentSize.width<=0f || _contentSize.height<=0f || _actionLock) return

        val numRows = numberOfRows!!.numberOfRows()

        _scroller!!.setScrollSize(_config.cellSize * numRows)

        val updated = _scroller!!.update()

        val scrollPosition = _scroller!!.getScrollPosition()

        positionChildren(scrollPosition, numRows)

        if (!updated) {
            unscheduleScrollUpdate()
        }

        _deltaScroll = _lastScrollPosition - scrollPosition

        _lastScrollPosition = scrollPosition

        if (!_fillWithCellsFirstTime) {
            _fillWithCellsFirstTime = true
            initFillWithCells?.initFillWithCells()
        }
    }

    protected fun isVertical():Boolean {return _config.orient==Orientation.VERTICAL}
    protected fun isHorizontal():Boolean {return _config.orient==Orientation.HORIZONTAL}

    fun convertToIndex(realIndex: Int, numRows: Int): Int {
        return if (realIndex>=0) {realIndex % numRows } else {(numRows - abs(realIndex%numRows))%numRows}
    }

    fun onCellAction(tag: Int, cells: ArrayList<SMView>, dt: Float, complete: Boolean) {
        if (tag == ACTION_TAG_CONSUME) {
            if (complete) {
                for (cell in cells) {
                    removeChild(cell)
                }
            } else {
                if (cellDeleteUpdate!=null) {
                    for (cell in cells) {
                        cellDeleteUpdate!!.cellDeleteUpdate(cell, dt)
                    }
                }
            }
        } else if (tag == ACTION_TAG_POSITION) {
            if (complete) {
                for (cell in cells) {
                    if (cell is ICircularCell) {
                        val iCell = cell as ICircularCell
                        iCell.setCellIndex(iCell.getAniIndex())
                    }
                }

                val scrollPosition = _config.cellSize * convertToIndex(_deleteIndex, numberOfRows!!.numberOfRows())
                _scroller!!.setScrollPosition(scrollPosition)

                scrollAlignedCallback?.scrollAlignedCallback(true, _deleteIndex, true)

                _actionLock = false
            } else {
                val t = tweenfunc.cubicEaseOut(dt)
                for (cell in cells) {
                    if (cell is ICircularCell) {
                        val iCell = cell as ICircularCell
                        val x = interpolation(iCell.getAniSrc(), iCell.getAniDst(), t)
                        iCell.setCellPosition(x)
                        positionCell?.positionCell(cell, x, false)
                    }
                }
            }
        }
    }

    override fun onAlignCallback(aligned: Boolean) {
        if (scrollAlignedCallback!=null) {
            if (aligned) {
                _currentPage = getAlignedIndex()

                if (!_config.circular) {
                    if (_currentPage<0) {
                        _currentPage = 0
                    } else if (numberOfRows!=null && _currentPage>=numberOfRows!!.numberOfRows()) {
                        _currentPage = numberOfRows!!.numberOfRows() - 1
                    }
                }
                scrollAlignedCallback!!.scrollAlignedCallback(aligned, _currentPage, false)
            } else {
                scrollAlignedCallback!!.scrollAlignedCallback(aligned, 0, false)
            }
        }
    }

    fun getListAnchorX(): Float {return _config.anchorPosition}

    fun positionChildren(s: Float, numRows: Int) {
        var scrollPosition = (s*10).toInt()/10.0f

        val maxScrollSize = _config.cellSize * numRows + _config.preloadPadding*2f
        val adjPosition = scrollPosition - _config.anchorPosition - _config.preloadPadding
        var norPosition = if (adjPosition>=0) {adjPosition % maxScrollSize} else {(maxScrollSize - abs(adjPosition % maxScrollSize))%maxScrollSize}

        val xx = -(norPosition % _config.cellSize)

        var start = if (adjPosition>=0) {(adjPosition/_config.cellSize).toInt()} else { floor(adjPosition/_config.cellSize).toInt()}
        var end = start + ceil(_config.windowSize/_config.cellSize).toInt()
        if (xx + _config.cellSize * (end-start) < _config.windowSize) {
            end++
        }

        var first = end
        var last = start

        val children = getChildren()
        val tmpList = ArrayList<SMView?>(children)

        for (child in tmpList) {
            if (child is ICircularCell) {
                val iCell = child as ICircularCell
                if (!iCell.isDeleted()) {
                    val realIndex = iCell.getCellIndex()
                    if (realIndex in start..end) {
                        val x = xx + (realIndex - start)  * _config.cellSize
                        iCell.setCellPosition(x)

                        positionCell?.positionCell(child, x, false)

                        first = first.coerceAtMost(realIndex)
                        last = last.coerceAtLeast(realIndex)
                    } else {
                        val reuseIndentifier = iCell.getCellIdentifier()
                        if (reuseIndentifier.isEmpty()) {
                            removeChild(child, true)
                        } else {
                            _reuseScrapper?.scrap(reuseIndentifier, this, child, true)
                        }
                    }
                }
            }
        }
        tmpList.clear()

        while (first>start) {
            val realIndex = first-1

            if (!_config.circular && (realIndex<0 || realIndex>=numRows)) {
                first--
                last = last.coerceAtLeast(realIndex+1)
                continue
            }

            val index = convertToIndex(realIndex, numRows)

            _reuseScrapper!!._internalReuseIdentifier = ""
            _reuseScrapper!!._internalReuseNode = null

            val cell = cellForRowsAtIndex!!.cellForRowsAtIndex(index)
            if (cell is ICircularCell) {
                val iCell = cell as ICircularCell
                iCell.setCellIndex(realIndex)
                iCell.setReuseIdentifier(_reuseScrapper!!._internalReuseIdentifier)
                addChild(cell)

                if (_reuseScrapper!!._internalReuseNode!=null && _reuseScrapper!!._internalReuseNode==cell) {
                    _reuseScrapper!!.popBack(_reuseScrapper!!._internalReuseIdentifier)
                }

                val x = xx + (realIndex-start) * _config.cellSize
                iCell.setCellPosition(x)

                positionCell?.positionCell(cell, x, true)
            }

            _reuseScrapper!!._internalReuseIdentifier = ""
            _reuseScrapper!!._internalReuseNode = null

            if (cell==null) break

            first--
            last = last.coerceAtLeast(realIndex+1)
        }

        while (last+1<end) {
            val realIndex = last + 1

            if (!_config.circular && (realIndex<0 || realIndex>=numRows)) {
                last++
                continue
            }

            val index = convertToIndex(realIndex, numRows)

            _reuseScrapper!!._internalReuseIdentifier = ""
            _reuseScrapper!!._internalReuseNode = null

            val cell = cellForRowsAtIndex?.cellForRowsAtIndex(index)
            if (cell is ICircularCell) {
                val iCell = cell as ICircularCell

                iCell.setCellIndex(realIndex)
                iCell.setReuseIdentifier(_reuseScrapper!!._internalReuseIdentifier)
                addChild(cell)

                if (_reuseScrapper!!._internalReuseNode!=null && _reuseScrapper!!._internalReuseNode==cell) {
                    _reuseScrapper!!.popBack(_reuseScrapper!!._internalReuseIdentifier)
                }

                val x = xx + (realIndex-start) * _config.cellSize
                iCell.setCellPosition(x)

                positionCell?.positionCell(cell, x, true)
            }

            _reuseScrapper!!._internalReuseIdentifier = ""
            _reuseScrapper!!._internalReuseNode = null

            if (cell==null) break

            last++
        }

        if (pageScrollCallback!=null) {
            val position:Float = (((_config.anchorPosition+norPosition)/_config.cellSize)%numRows)
            pageScrollCallback!!.pageScrollCallback(position)
        }
    }

    fun CellsActionCreate(director: IDirector): CellsAction {
        val action = CellsAction(director)
        action.initWithDuration(0f)
        return action
    }

    class CellsAction(director: IDirector): TransformAction(director) {

        var _cells:ArrayList<SMView>? = null

        override fun onUpdate(dt: Float) {
            super.onUpdate(dt)
            if (_target is SMCircularListView) {
                (_target as SMCircularListView).onCellAction(getTag(), _cells!!, dt, false)
            }
        }

        override fun onEnd() {
            if (_target is SMCircularListView) {
                (_target as SMCircularListView).onCellAction(getTag(), _cells!!, 1f, false)
            }
        }

        fun setTargetCells(cells: ArrayList<SMView>) {_cells = cells}
    }

    enum class Orientation {
        VERTICAL,
        HORIZONTAL
    }

    class Config() {
        var orient: Orientation = Orientation.HORIZONTAL
        var circular = true
        var cellSize = 0f
        var windowSize = 0f
        var anchorPosition = 0f
        var preloadPadding = 0f
        var maxVelocity = 5000f
        var minVelocity = 0f
        var scrollMode = SMScroller.ScrollMode.BASIC
    }

    fun CellPositionActionCreate(director: IDirector): CellPositionAction {
        val action = CellPositionAction(director)
        action.initWithDuration(0f)
        return action
    }

    class CellPositionAction(director: IDirector): TransformAction(director) {
        var _cells:ArrayList<SMView>? = null

        fun setTargetCells(cells: ArrayList<SMView>) {_cells = cells}

        override fun onUpdate(dt: Float) {
            super.onUpdate(dt)

            if (_cells==null) return

            val listView = _target as SMCircularListView


            for (cell in _cells!!) {
                listView.cellDeleteUpdate?.cellDeleteUpdate(cell, dt)
            }
        }

        override fun onEnd() {
            super.onEnd()

            if (_cells==null) return

            val listView = _target as SMCircularListView
            for (cell in _cells!!) {
                listView.removeChild(cell)
            }
        }
    }

    class ReuseScrapper() {
        var _numberOfTypes = 0
        var _key:HashMap<String, Int?> = HashMap()
        var _data:ArrayList<ArrayList<SMView>> = ArrayList()

        var _internalReuseNode:SMView? = null
        var _internalReuseIdentifier:String = ""

        fun getReuseType(reuseIdentifire: String): Int {
            var reuseType = -1

            val value = _key[reuseIdentifire]
            if (value==null) {
                reuseType = _numberOfTypes
                _key[reuseIdentifire] = _numberOfTypes
                _data.add(ArrayList())
            } else {
                reuseType = value as Int
            }

            return reuseType
        }

        fun scrap(reuseIdentifire: String, parent: SMView, child: SMView) {
            scrap(reuseIdentifire, parent, child, true)
        }
        fun scrap(reuseIdentifire: String, parent: SMView?, child: SMView, cleanup: Boolean) {
            val reuseType:Int = getReuseType(reuseIdentifire)

            val queue: ArrayList<SMView> = _data[reuseType]
            parent?.removeChild(child, cleanup)

            queue.add(child)
        }

        fun popBack(reuseIdentifire: String) {
            val reuseType = getReuseType(reuseIdentifire)
            val lastIndex = _data[reuseType].size-1
            _data[reuseType].removeAt(lastIndex)
        }

        fun back(reuseIdentifire: String): SMView? {
            val reuseType = getReuseType(reuseIdentifire)
            if (_data[reuseType].size>0) {
                val lastIndex = _data[reuseType].size-1
                return _data[reuseType][lastIndex]
            }

            return null
        }
    }
}