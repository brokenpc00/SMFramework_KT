package com.brokenpc.app.scene

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.SideMenu
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.transition.SlideInToLeft
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.IndexPath
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.SMLabel
import com.brokenpc.smframework.view.SMTableView
import com.interpark.app.menu.MenuBar
import com.interpark.app.scene.ListScene
import com.interpark.smframework.view.SMRoundLine

class HelloBrokenpcScene(director:IDirector) : SMScene(director), SMTableView.CellForRowAtIndexPath, SMTableView.NumberOfRowsInSection, SMView.OnClickListener {
    private var _mainScene:HelloBrokenpcScene? = null
    private lateinit var _tableView:SMTableView
    private lateinit var _contentView:SMView
    private lateinit var _menuBar:MenuBar
    private val _menuNames:ArrayList<String> = ArrayList()

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

        _menuNames.add("Shapes.")
        _menuNames.add("Views.")
        _menuNames.add("Controls.")
        _menuNames.add("Etcetera.")


        _tableView = SMTableView.create(getDirector(), SMTableView.Orientation.VERTICAL, 0f, 0f, s.width, s.height)!!
        _tableView.setTag(999)
        _tableView.numberOfRowsInSection = object : SMTableView.NumberOfRowsInSection {
            override fun numberOfRowsInSection(section: Int): Int {
                return _menuNames.size
            }
        }
        _tableView.cellForRowAtIndexPath = object : SMTableView.CellForRowAtIndexPath {
            override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
                val index = indexPath.getIndex()
                val cellID = "CELL$index"
                val s = _tableView.getContentSize()
                var cell = _tableView.dequeueReusableCellWithIdentifier(cellID)
                if (cell==null) {
                    cell = create(getDirector(), 0, 0f, 0f, s.width, 250f)!!

                    val str = _menuNames[index]
                    val title = SMLabel.create(getDirector(), str, 80f, MakeColor4F(0x222222, 1f))
                    title.setAnchorPoint(Vec2.MIDDLE)
                    title.setPosition(s.width/2f, cell!!.getContentSize().height/2f)
                    cell.addChild(title)

                    val line = SMRoundLine.create(getDirector())
                    line.setBackgroundColor(MakeColor4F(0xdbdcdf, 1f))
                    line.setLineWidth(4f)
                    line.line(40f, 246f, s.width-40f, 246f)
                    line.setLengthScale(1f)
                    cell.addChild(line)
                    cell.setTag(index)

                    cell.setOnClickListener(object : OnClickListener{
                        override fun onClick(view: SMView?) {
                            val index = view!!.getTag()

                            val params = SceneParams()
                            params.putInt("SCENE_TYPE", index)
                            params.putString("MENU_NAME", _menuNames[index])
                            val scene = ListScene.create(getDirector(), _menuBar, params)
                            val left = SlideInToLeft.create(getDirector(), 0.3f, scene)
                            getDirector().pushScene(left!!)
                        }
                    })

                    cell!!.setOnStateChangeListener(object : OnStateChangeListener{
                        override fun onStateChange(view: SMView?, state: STATE?) {
                            if (state==STATE.PRESSED) {
                                view?.setBackgroundColor(Color4F.XEEEFF1, 0.15f)
                            } else {
                                view?.setBackgroundColor(Color4F.WHITE, 0.15f)
                            }
                        }
                    })
                }

                return cell
            }
        }
        _contentView.addChild(_tableView)
        _tableView.setScissorEnable(true)

        _contentView.setLocalZOrder(-10)
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