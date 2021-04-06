package com.brokenpc.app.scene

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.SideMenu
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.IndexPath
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.SMTableView
import com.interpark.app.menu.MenuBar

class HelloBrokenpcScene(director:IDirector) : SMScene(director), SMTableView.CellForRowAtIndexPath, SMTableView.NumberOfRowsInSection, SMView.OnClickListener {
    private var _mainScene:HelloBrokenpcScene? = null
    private lateinit var _tableView:SMTableView
    private lateinit var _contentView:SMView
    private lateinit var _menuBar:MenuBar
    private val _nameName:ArrayList<String> = ArrayList()

    companion object {
        const val _scenetTitle = "SMFrame Lib."
        @JvmStatic
        fun create(director: IDirector, params: SceneParams, type: SwipeType): HelloBrokenpcScene {
            val scene = HelloBrokenpcScene(director)
            scene.initWithSceneParams(params, type)
            return scene
        }
    }

    protected val _menubarListener = object : MenuBar.MenuBarListener {
        override fun func1(view: SMView): Boolean {
            return onMenuClick(view)
        }

        override fun func2() {

        }
    }

    fun onMenuClick(view: SMView?): Boolean {
        return when (MenuBar.intToMenuType(view?.getTag()?:0)) {
            MenuBar.MenuType.MENU -> {
                SideMenu.OpenMenu(this)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun init(): Boolean {
        super.init()


        val s = getContentSize()

        _mainScene = this

        _menuBar = MenuBar.create(getDirector())
        _menuBar.setMenuButtonType(MenuBar.MenuType.MENU, true)
        _menuBar.setText(_scenetTitle, true)
        _menuBar.setColorSet(MenuBar.ColorSet.WHITE_TRANSLUCENT, true)
        _menuBar.setMenuBarListener(_menubarListener)
        addChild(_menuBar)

        _contentView = SMView.create(getDirector(), 0f, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT)
        _contentView.setBackgroundColor(Color4F.WHITE)
        addChild(_contentView)


        _tableView = SMTableView.create(getDirector(), SMTableView.Orientation.VERTICAL, 0f, 0f, s.width, s.height)!!
        _tableView.setTag(999)
        _tableView.numberOfRowsInSection = object : SMTableView.NumberOfRowsInSection {
            override fun numberOfRowsInSection(section: Int): Int {
                return 10
            }
        }
        _tableView.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
            override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                val index = indexPath.getIndex()
                var convertView = _tableView.dequeueReusableCellWithIdentifier("CELL")
                val cell: SMView
                if (convertView!=null) {
                    cell = convertView
                } else {
                    val s = getDirector().getWinSize()
                    cell = create(getDirector(), 0, 0f, 0f, s.width, 100f)
                    val line = SMView.create(getDirector(), 1, 0f, 99f, s.width, 2f)
                    line.setBackgroundColor(MakeColor4F(0xadafb3, 1f))
                    cell.addChild(line)
                }

                return cell
            }
        }
        _contentView.addChild(_tableView)
        _tableView.setScissorEnable(true)

        return true
    }

    override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
        TODO("Not yet implemented")
    }

    override fun numberOfRowsInSection(section: Int): Int {
        TODO("Not yet implemented")
    }

    override fun onClick(view: SMView?) {
        TODO("Not yet implemented")
    }

    override fun onTransitionStart(t: Transition, tag: Int) {
        if (t==Transition.IN) {
            if (getSwipeType()==SwipeType.MENU) {
                _menuBar.setMenuButtonType(MenuBar.MenuType.MENU, false)
            } else {
                _menuBar.setMenuButtonType(MenuBar.MenuType.BACK, false)
            }
            _menuBar.setColorSet(MenuBar.ColorSet.WHITE_TRANSLUCENT, false)
            _menuBar.setText(_scenetTitle, false)
        }
    }

    override fun onTransitionComplete(t: Transition, tag: Int) {
        if (t==Transition.RESUME) {
            bringMenuBarFromLayer()
        } else {

        }
    }

    fun bringMenuBarFromLayer() {
        val layer = getDirector().getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI) ?: return

        for (child in layer.getChildren()) {
            if (child==_menuBar) {
                _menuBar.changeParent(this)
                break
            }
        }

        _menuBar.setMenuBarListener(_menubarListener)
    }

}