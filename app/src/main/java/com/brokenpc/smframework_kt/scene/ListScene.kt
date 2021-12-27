package com.brokenpc.app.scene

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.base.transition.SlideInToTop
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.IndexPath
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.SMLabel
import com.brokenpc.smframework.view.SMTableView
import com.brokenpc.app.menu.MenuBar
import com.brokenpc.smframework.view.SMRoundLine

class ListScene(director: IDirector) : SMMenuTransitionScene(director), SMTableView.CellForRowAtIndexPath, SMTableView.NumberOfRowsInSection {

    private lateinit var _tableView:SMTableView
    private lateinit var _contentView:SMView
    private var _menuNames:ArrayList<String> = ArrayList()
    private var _sceneType = 0

    companion object {
        @JvmStatic
        fun create(director: IDirector, menuBar: MenuBar): ListScene {
            return create(director, menuBar, null)
        }

        @JvmStatic
        fun create(director: IDirector, menuBar: MenuBar, params: SceneParams?): ListScene {
            val scene = ListScene(director)
            scene.initWithParams(menuBar, params)
            return scene
        }
    }

    fun initWithParams(menuBar: MenuBar, params: SceneParams?): Boolean {
        super.initWithMenuBar(menuBar)

        _sceneParam = params
        getRootView().setBackgroundColor(Color4F.XEEEFF1)
        setMenuBarTitle(_sceneParam?.getString("MENU_NAME")?:"SUB_VIEW")


        val s = getDirector().getWinSize()

        _contentView = create(getDirector(), 0, 0f, AppConst.SIZE.MENUBAR_HEIGHT, s.width, s.height-AppConst.SIZE.MENUBAR_HEIGHT)
        addChild(_contentView)

        _sceneType = _sceneParam?.getInt("SCENE_TYPE")?:0
        when (_sceneType) {
            1 -> {
                _menuNames.add("IMAGE VIEW")
                _menuNames.add("ZOOM VIEW")
                _menuNames.add("PAGE VIEW")
                _menuNames.add("CIRCULAR LIST VIEW")
                _menuNames.add("TABLE VIEW")
                _menuNames.add("KENBURNS")
                _menuNames.add("WAVE & PULSE")
                _menuNames.add("STENCIL VIEW")
                _menuNames.add("STICKER VIEW")
                _menuNames.add("SWIPE VIEW")
                _menuNames.add("ANIMATION1")
            }
            2 -> {
                _menuNames.add("LABEL")
                _menuNames.add("BUTTON")
                _menuNames.add("SOLID BUTTON")
                _menuNames.add("SLIDER")
                _menuNames.add("PROGRESS")
                _menuNames.add("LOADING")
            }
            else -> {
                _menuNames.add("DOT")
                _menuNames.add("LINE")
                _menuNames.add("RECT")
                _menuNames.add("ROUNDED RECT")
                _menuNames.add("CIRCLE")
                _menuNames.add("SOLID RECT")
                _menuNames.add("SOLID REOUNDED RECT")
                _menuNames.add("SOLID CIRCLE")
                _menuNames.add("SOLID TRIANGLE")
            }
        }


        _tableView = SMTableView.Companion.createMultiColumn(getDirector(), SMTableView.Orientation.VERTICAL, 1, 0f, 0f, s.width, _contentView.getContentSize().height)!!
        _tableView.cellForRowAtIndexPath = this
        _tableView.numberOfRowsInSection = this

        _tableView.setScissorEnable(true)

        _contentView.addChild(_tableView)
        _contentView.setLocalZOrder(-10)

        return true
    }


    override fun cellForRowAtIndexPath(indexPath: IndexPath): SMView {
        val index = indexPath.getIndex()
        val cellID = "CELL${index}"
        val s = _tableView.getContentSize()
        var cell = _tableView.dequeueReusableCellWithIdentifier(cellID)
        if (cell==null) {
            cell = create(getDirector(), 0, 0f, 0f, s.width, 250f)
            cell!!.setBackgroundColor(Color4F.WHITE)

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
                    params.putString("MENU_NAME", _menuNames[index])
                    var scene:SMScene? = null

                    when (_sceneType) {
                        0 -> {
                            // shapes
                            params.putInt("SHAPE_TYPE", index)

                            scene = ShapeDisplayScene.create(getDirector(), _menuBar!!, params)
                        }
                        1 -> {
                            // views
                            params.putInt("VIEW_TYPE", index)

                            scene = ViewDisplayScene.create(getDirector(), _menuBar!!, params)
                        }
                        2 -> {
                            // controls
                            params.putInt("CONTROL_TYPE", index)
                            _menuBar?.showToast("Control type : ${_menuNames[index]} Not Yet.", Color4F.TOAST_RED, 2.0f)
                        }
                        else -> {
                            // etc
                            _menuBar?.showToast("Anything type : ${_menuNames[index]} Not Yet.", Color4F.TOAST_RED, 2.0f)
                        }
                    }

                    if (scene!=null) {
                        val top = SlideInToTop.create(getDirector(), 0.3f, scene)
                        getDirector().pushScene(top!!)
                    }
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

    override fun numberOfRowsInSection(section: Int): Int {
        return _menuNames.size
    }

    override fun onMenuBarClick(view: SMView?): Boolean {
        val type = MenuBar.intToMenuType(view?.getTag()?:0)
        when (type) {
            MenuBar.MenuType.BACK -> {
                finishScene()
                return true
            }
        }

        return false
    }

}