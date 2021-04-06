package com.interpark.app.scene

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Size
import com.interpark.app.menu.MenuBar

class SMMenuTransitionScene(director: IDirector): SMScene(director) {
    protected var _menuBar:MenuBar? = null
    protected var _menuTitle:String = ""
    protected var _prevMenuTitle:String = ""
    protected val _menuBarButton:ArrayList<MenuBar.MenuType> = ArrayList(2)
    protected var _prevManuBarButton:ArrayList<MenuBar.MenuType> = ArrayList()
    protected var _swipeStarted: Boolean = false
    protected val _menubarListener:MenuBar.MenuBarListener = object : MenuBar.MenuBarListener {
        override fun func1(view: SMView): Boolean {
            return onMenuBarClick(view)
        }

        override fun func2() {
            onMenuBarTouch()
        }
    }
    protected var _fromMenuType = MenuBar.MenuType.NONE
    protected var _toMenuType = MenuBar.MenuType.NONE

    companion object {
        @JvmStatic
        fun create(director: IDirector, menuBar: MenuBar): SMMenuTransitionScene {
            val scene = SMMenuTransitionScene(director)
            scene.initWithMenuBar(menuBar)
            return scene
        }
    }


    init {

    }

    fun onMenuBarClick(view: SMView?): Boolean {
        return false
    }

    fun onMenuBarTouch() {

    }

    protected fun initWithMenuBar(menuBar: MenuBar): Boolean {
        return initWithMenuBar(menuBar, SwipeType.BACK)
    }

    protected fun initWithMenuBar(menuBar: MenuBar, type: SwipeType): Boolean {
        super.initWithSceneParams(null, type)

        val size = Size(getDirector().getWidth(), getDirector().getHeight())

        _fromMenuType = menuBar.getMenuButtonType()

        _menuBar = menuBar
        if (_menuBar!=null) {
            val layer = getDirector().getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI)
            if (layer!=null) {
                _menuBar!!.changeParent(layer)

                _prevMenuTitle = _menuBar!!.getText()
                _prevManuBarButton = _menuBar!!.getButtonTypes()

                _menuBar!!.setTextTransitionType(MenuBar.TextTransition.FADE)
                _toMenuType = when (getSwipeType()) {
                    SwipeType.MENU -> {
                        MenuBar.MenuType.MENU
                    }
                    SwipeType.DISMISS -> {
                        MenuBar.MenuType.CLOSE
                    }
                    else -> {
                        MenuBar.MenuType.BACK
                    }
                }
                _menuBar!!.setMenuButtonType(_toMenuType, false)
                _menuBar!!.setTextTransitionType(MenuBar.TextTransition.FADE)
                _menuBar!!.setMenuBarListener(null)

                return true
            }
        }

        return false
    }

    fun setMenuBarTitle(title: String) {_menuTitle = title}

    fun setMenuBarButton(button1: MenuBar.MenuType) {
        setMenuBarButton(button1, MenuBar.MenuType.NONE)
    }

    fun setMenuBarButton(button1: MenuBar.MenuType, button2: MenuBar.MenuType) {
        _menuBarButton[0] = button1
        _menuBarButton[1] = button2
    }

    override fun onTransitionStart(type: Transition, tag: Int) {
        if (type==Transition.IN) {
            _menuBar?.setText(_menuTitle, false)
            _menuBar?.setTwoButton(_menuBarButton[0], _menuBarButton[1], false)
        }

        if (type==Transition.OUT || type==Transition.SWIPE_OUT) {
            if (_menuBar==null) return

            val layer = getDirector().getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI)?:return

            _menuBar!!.changeParent(layer)

            if (type==Transition.OUT) {
                _menuBar!!.setTextTransitionType(MenuBar.TextTransition.FADE)
                _menuBar!!.setText(_prevMenuTitle, false)
                _menuBar!!.setMenuButtonType(_fromMenuType, false, false)
                _menuBar!!.setButtonTransitionType(MenuBar.ButtonTransition.FADE)

                val numButtons = _prevManuBarButton.size
                when (numButtons) {
                    1 -> {
                        _menuBar!!.setOneButton(_prevManuBarButton[0], false, false)
                    }
                    2 -> {
                        _menuBar!!.setTwoButton(_prevManuBarButton[0], _prevManuBarButton[1], false, false)
                    }
                    else -> {
                        _menuBar!!.setOneButton(MenuBar.MenuType.NONE, false, false)
                    }
                }
            } else {
                _menuBar!!.setTextTransitionType(MenuBar.TextTransition.SWIPE)
                _menuBar!!.setText(_prevMenuTitle, false)
                _menuBar!!.setMenuButtonType(_fromMenuType, false, true)
                _menuBar!!.setButtonTransitionType(MenuBar.ButtonTransition.FADE)

                val numButtons = _prevManuBarButton.size
                when (numButtons) {
                    1 -> {
                        _menuBar!!.setOneButton(_prevManuBarButton[0], true, true)
                    }
                    2 -> {
                        _menuBar!!.setTwoButton(_prevManuBarButton[0], _prevManuBarButton[1], true, true)
                    }
                    else -> {
                        _menuBar!!.setOneButton(MenuBar.MenuType.NONE, true, true)
                    }
                }
                _menuBar!!.onSwipeStart()
            }
        }
    }

    override fun onTransitionProgress(type: Transition, tag: Int, progress: Float) {
        if (type==Transition.SWIPE_OUT) {
            _menuBar?.onSwipeUpdate(progress)
            _swipeStarted = true
        }
    }

    override fun onTransitionComplete(type: Transition, tag: Int) {
        var menuBarReturn = false

        if (_swipeStarted && _menuBar!=null) {
            if (type==Transition.SWIPE_OUT) {
                _menuBar!!.onSwipeComplete()
            } else if (type==Transition.RESUME) {
                _menuBar!!.onSwipeCancel()
                menuBarReturn = true
            }
        }

        _swipeStarted = false

        if (type==Transition.IN || menuBarReturn) {
            val layer = getDirector().getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI) ?: return

            val children = layer.getChildren()
            for (child in children) {
                if (child==_menuBar && _menuBar!=null) {
                    child.changeParent(this)
                    _menuBar!!.setMenuBarListener(_menubarListener)
                    break
                }
            }
        }
    }

    override fun onTransitionReplaceSceneDidFinish() {
        val layer = getDirector().getSharedLayer(IDirector.SharedLayer.BETWEEN_SCENE_AND_UI) ?: return

        val children = layer.getChildren()
        for (child in children) {
            if (child==_menuBar) {
                _menuBar?.changeParent(this)
                break
            }
        }

        _menuBar?.setMenuBarListener(_menubarListener)
    }
}