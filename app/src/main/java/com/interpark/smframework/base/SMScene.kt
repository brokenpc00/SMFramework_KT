package com.brokenpc.smframework.base

import android.content.Intent
import com.brokenpc.app.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.transition.*
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.util.BackPressable

open class SMScene(director: IDirector) : SMView(director), BackPressable {

    companion object {
        const val STATE_CREATING:Int = -1
        const val STATE_ACTIVATE:Int = 0
        const val STATE_PAUSED:Int = 1
        const val STATE_STARTING:Int = 2
        const val STATE_FINISHING:Int = 3
        const val STATE_PAUSING:Int = 4
        const val STATE_RESUMING:Int = 5

        @JvmStatic
        fun create(director: IDirector, param: SceneParams?=null, swipeType: SwipeType=SwipeType.NONE):SMScene {
            val scene = SMScene(director)
            scene.initWithSceneParams(param, swipeType)
            return scene
        }
    }

    private var _state:Int = STATE_CREATING

    protected var _sceneResult:SceneParams? = null
    protected var _sceneParam:SceneParams? = null

    protected var _rootView:SMView = SMView(director, 0f, 0f, director.getWidth().toFloat(), director.getHeight().toFloat())
    protected var _swipeType:SwipeType = SwipeType.NONE
    protected var _mainMenuEnable:Boolean = false

    enum class SwipeType {
        NONE, MENU, BACK, DISMISS
    }

    enum class Transition {
        IN, OUT, PAUSE, RESUME, SWIPE_IN, SWIPE_OUT
    }

    protected fun initWithSceneParams(param: SceneParams?, type: SwipeType): Boolean {
        setContentSize(_rootView.getContentSize())
        super.addChild(_rootView)

        _swipeType = type
        if (param!=null) {
            _sceneParam = param
        }

        if (init()) {
            setCascadeAlphaEnable(true)
            return true
        }

        return false
    }

    init {
        setAnchorPoint(Vec2.MIDDLE)
        setPosition(Vec2(director.getWinSize().width/2, director.getWinSize().height/2))
        setContentSize(director.getWidth().toFloat(), director.getHeight().toFloat())
    }

    fun getRootView():SMView {return _rootView}
    fun getSwipeType():SwipeType {return _swipeType}

    override fun addChild(child: SMView) {
        val size:Size = Size(getDirector().getWidth().toFloat(), getDirector().getHeight().toFloat())
        setContentSize(size)
        _rootView.addChild(child)
    }

    override fun addChild(child: SMView, zOrder: Int) {
        _rootView.addChild(child, zOrder)
    }

    override fun addChild(child: SMView, zOrder: Int, name: String) {
        _rootView.addChild(child, zOrder, name)
    }

    override fun addChild(child: SMView, zOrder: Int, tag: Int) {
        _rootView.addChild(child, zOrder, tag)
    }

    override fun getChildByName(name: String): SMView? {
        return _rootView.getChildByName(name)
    }

    override fun removeChild(child: SMView?) {
        _rootView.removeChild(child)
    }

    override fun removeChild(child: SMView?, cleanup: Boolean) {
        _rootView.removeChild(child, cleanup)
    }

    override fun removeChildByTag(tag: Int) {
        _rootView.removeChildByTag(tag)
    }

    protected fun setRootView(newRootView:SMView?) {
        if (newRootView==null || newRootView==_rootView) return

        super.removeChild(_rootView)
        super.addChild(newRootView)
        _rootView = newRootView
    }

    fun isMainMenuEnable():Boolean {return _mainMenuEnable}
    fun setMainMenuEnable(enable:Boolean) {_mainMenuEnable=enable}

    fun setSceneResult(result:SceneParams?) {
        _sceneResult = result
    }

    fun getSceneParams():SceneParams? {return _sceneParam}

    fun onSceneResult(fromScene:SMScene?, result:SceneParams?) {}

    open fun onTransitionProgress(t:Transition, tag: Int, progress:Float) {}
    open fun onTransitionStart(t:Transition, tag:Int) {}
    open fun onTransitionComplete(t:Transition, tag: Int) {}
    open fun onTransitionReplaceSceneDidFinish() {}
    open fun canSwipe(point:Vec2, type:SwipeType):Boolean {return true}

    open fun onExitBackground() {}
    open fun onEnterForground() {}

    fun SMSceneOnEnter() {
        setState(STATE_ACTIVATE)
        SMViewOnEnter()
    }

    override fun onEnter() {
        setState(STATE_ACTIVATE)
        super.onEnter()
    }

    fun SMSceneOnExit() {
        if (_state != STATE_FINISHING) {
            setState(STATE_PAUSED)
            SMViewOnExit()
        }
    }

    override fun onExit() {
        if (_state!= STATE_FINISHING) {
            setState(STATE_PAUSED)
            onPause()
            super.onExit()
        }
    }

    fun startScene(scene: SMScene) {
        var transition:TransitionScene? = null

        when (scene.getSwipeType()) {
            SwipeType.NONE, SwipeType.BACK -> {
                transition = SlideInToLeft.create(getDirector(), AppConst.SceneTransitionTime.NORMAL, scene)
            }
            SwipeType.DISMISS -> {
                transition = SlideInToTop.create(getDirector(), AppConst.SceneTransitionTime.NORMAL, scene)
            }
            else -> {}
        }

        if (transition!=null) {
            getDirector().pushScene(transition)
        }
    }

    fun setState(state:Int) {
        _state = state
    }

    open fun finishScene() {
        finishScene(null)
    }

    open fun finishScene(params: SceneParams?) {
        val scene = getDirector().getPreviousScene() ?: return
        setSceneResult(params)
        var transition: TransitionScene? = null
        when (_swipeType) {
            SwipeType.MENU -> {
                if (BuildConfig.DEBUG) {
                    error("Assertion failed")
                }
            }
            SwipeType.NONE, SwipeType.BACK -> {
                transition = SlideOutToRight.create(
                    getDirector(),
                    AppConst.SceneTransitionTime.NORMAL,
                    scene
                )
            }
            SwipeType.DISMISS -> {
                transition = SlideOutToBottom.create(
                    getDirector(),
                    AppConst.SceneTransitionTime.NORMAL,
                    scene
                )
            }
        }
        transition!!.setTag(getTag())
        getDirector().popSceneWithTransition(transition)
    }

    open fun isActivate(): Boolean {
        return isInitialized() && _state == STATE_ACTIVATE
    }

    override fun onBackPressed(): Boolean {
        return if (_director!!.getPreviousScene() != null) {
            finishScene()
            true
        } else {
            removeFromParent()
            _director!!.popScene()
            false
        }
    }

    open fun getSceneResult(): SceneParams? {
        return _sceneResult
    }

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}


}