package com.brokenpc.smframework

import com.brokenpc.smframework.IDirector.SIDE_MENU_STATE
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.EdgeSwipeLayerForSideMenu
import com.brokenpc.smframework.view.SMLabel
import com.brokenpc.smframework.view.SMTableView
import com.brokenpc.smframework.view.SMTableView.NumberOfRowsInSection
import com.brokenpc.smframework.view.SMTableView.CellForRowAtIndexPath

class SideMenu(director:IDirector) : SMView(director), CellForRowAtIndexPath {

    private var _contentView: SMView
    private var _sideMenuTableView: SMTableView
    private var _swipeLayer: EdgeSwipeLayerForSideMenu? = null
    private var _state = SIDE_MENU_STATE.CLOSE
    private var _listener: SideMenu.SIDE_MENU_LISTENER? = null
    private var _lastPosition = 0.0f
    var _callback: MENU_OPEN_CLOSE? = null

    companion object {
        const val SIDE_MENU_COUNT:Int = 20
        private var _instance: SideMenu? = null

        @JvmStatic
        fun GetSideMenu(): SideMenu {
            if (_instance == null) {
                _instance = SideMenu(SMDirector.getDirector())
            }
            return _instance!!
        }

        @JvmStatic
        fun OpenMenu(mainScene: SMScene?) {
            OpenMenu(mainScene, null)
        }

        @JvmStatic
        fun OpenMenu(mainScene: SMScene?, callback: MENU_OPEN_CLOSE?) {
            GetSideMenu()._callback = callback
            GetSideMenu()._swipeLayer?.open(false)
        }

        @JvmStatic
        fun CloseMenu() {
            CloseMenu(null)
        }

        @JvmStatic
        fun CloseMenu(callback: MENU_OPEN_CLOSE?) {
            GetSideMenu()._callback = callback
            GetSideMenu()._swipeLayer?.close(false)
        }
    }

    init {
        setVisible(false)

        setPosition(Vec2(-AppConst.SIZE.LEFT_SIDE_MENU_WIDTH, 0f))
        setContentSize(Size(AppConst.SIZE.LEFT_SIDE_MENU_WIDTH, getDirector().getHeight().toFloat()))
        setAnchorPoint(Vec2(0, 0))

        val s: Size = director.getWinSize()
        _contentView = create(director, 0f, 0f, getContentSize().width, getContentSize().height)
        _contentView.setBackgroundColor(Color4F(Color4B(0xf4, 0xf5, 0xf6, 0xff)))
        addChild(_contentView)


        _sideMenuTableView = SMTableView.createMultiColumn(director, SMTableView.Orientation.VERTICAL, 1, 0f, 0f, getContentSize().width, getContentSize().height)!!
        _sideMenuTableView.numberOfRowsInSection = object : NumberOfRowsInSection {
            override fun numberOfRowsInSection(section: Int): Int {
                // menu count
                return SIDE_MENU_COUNT
            }
        }
        _sideMenuTableView.cellForRowAtIndexPath = this
        _contentView.addChild(_sideMenuTableView)
        _sideMenuTableView.setEnabled(true)
        _sideMenuTableView.enableAccelerateScroll(true)
        _sideMenuTableView.setScrollLock(false)
    }

    interface SIDE_MENU_LISTENER {
        fun onSideMenuSelect(tag: Int)
        fun onSideMenuVisible(visible: Boolean)
    }

    interface MENU_OPEN_CLOSE {
        fun onMenuClose()
    }



    fun clearMenu() {
        _instance!!.removeFromParent()
        _instance = null
    }



    protected fun menuClick(view: SMView?) {}

    fun setSwipeLayer(swipeLayer: EdgeSwipeLayerForSideMenu) {
        _swipeLayer = swipeLayer
    }

    fun setSideMenuListener(listener: SIDE_MENU_LISTENER?) {
        _listener = listener
    }

    fun setOpenPosition(position: Float) {
        var f = 0f
        if (position >= _contentSize.width) {
            // 완전 열림
            if (_state !== SIDE_MENU_STATE.OPEN) {
                _state = SIDE_MENU_STATE.OPEN
                _callback?.onMenuClose()
                if (!isVisible()) {
                    setVisible(true)
                }
            }
            f = 1.0f
        } else if (position <= 0) {
            // 완전 닫힘
            if (_state !== SIDE_MENU_STATE.CLOSE) {
                _state = SIDE_MENU_STATE.CLOSE
                _swipeLayer?.closeComplete()
                _callback?.onMenuClose()
                if (isVisible()) {
                    setVisible(false)
                }
            }
            f = 0.0f
        } else {
            // 이동중
            if (_state !== SIDE_MENU_STATE.MOVING) {
                _state = SIDE_MENU_STATE.MOVING
                if (!isVisible()) {
                    setVisible(true)
                }
            }
            f = position / _contentSize.width
            if (f < 0) f = 0f else if (f > 1) f = 1f
        }
        val x = -0.3f * (1.0f - f) * _contentSize.width
        setPositionX(x)
        if (_sideMenuUpdateCallback != null) {
            _sideMenuUpdateCallback!!.onSideMenuUpdateCallback(_state, position)
        }
        _lastPosition = position
    }

    fun getOpenPosition(): Float {
        return _lastPosition
    }

    fun getState(): SIDE_MENU_STATE? {
        return _state
    }

    interface SIDE_MENU_UPDATE_CALLBACK {
        fun onSideMenuUpdateCallback(
            state: SIDE_MENU_STATE,
            position: Float
        )
    }

    var _sideMenuUpdateCallback: SIDE_MENU_UPDATE_CALLBACK? = null

    override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
        val index = indexPath.getIndex()
        val cellID = "SIDE MENU : $index"
        val convertView = _sideMenuTableView.dequeueReusableCellWithIdentifier(cellID)
        var cell: SideMenuCell
        val s = getContentSize()
        if (convertView != null) {
            cell = convertView as SideMenuCell
        } else {
            cell = SideMenuCell(getDirector())
            cell.setContentSize(Size(s.width, 150))
            cell.setPosition(Vec2(0, 0))
            cell.setAnchorPoint(Vec2(0, 0))
            cell._contentView = create(getDirector(), 0f, 0f, s.width, 150f)
            cell.addChild(cell._contentView!!)

            //(IDirector director, String text, float fontSize, float textColorR, float textColorG, float textColorB, float textColorA) {
            cell._title = SMLabel.create(getDirector(), cellID, 45f, Color4F(0f, 0f, 1f, 1f))
            if (cell._title!=null) {
                cell._title!!.setAnchorPoint(Vec2.MIDDLE)
                cell._title!!.setPosition(Vec2(s.width / 2, 75))
                cell._contentView!!.addChild(cell._title!!)
            }
        }
        return cell
    }

    inner class SideMenuCell(director: IDirector) : SMView(director) {

        var _parentMenu: SideMenu? = null
        var _contentView: SMView? = null
        var _title: SMLabel? = null
    }
}