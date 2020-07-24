package com.interpark.smframework.base.transition


import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMScene
import com.interpark.smframework.base.types.Mat4
import com.interpark.smframework.base.types.SEL_SCHEDULE
import com.interpark.smframework.base.types.Size
import com.interpark.smframework.base.types.Vec2

open class TransitionScene(director:IDirector) : SMScene(director) {

    protected var _lastProgress:Float = -1f
    protected var _inScene:SMScene? = null
    protected var _outScene:SMScene? = null
    protected var _duration:Float = 0f
    protected var _isInSceneOnTop:Boolean = false
    protected var _isSendCleanupToScene:Boolean = false

    fun getLastProgress():Float {return _lastProgress}

    companion object {
        @JvmStatic
        fun create(director: IDirector, t: Float, scene: SMScene):TransitionScene {
            val tScene:TransitionScene = TransitionScene(director)
            tScene.initWithDuration(t, scene)

            return tScene
        }
    }

    init {
        val size = Size(director.getWidth().toFloat(), director.getHeight().toFloat())
        setAnchorPoint(Vec2.MIDDLE)
        setPosition(Vec2(size.width/2, size.height/2))
        setContentSize(size)
    }

    enum class Orientation {
        LEFT_OVER,
        RIGHT_OVER,
        UP_OVER,
        DOWN_OVER
    }

    fun enumToInt(orientation:Orientation):Int {
        return if (orientation==Orientation.LEFT_OVER || orientation==Orientation.UP_OVER) 0 else 1
    }


    fun initWithDuration(t:Float, scene: SMScene): Boolean {
        if (super.init()) {
            _duration = t

            _inScene = scene
            _outScene = getDirector().getRunningScene()

            if (_outScene==null) {
                _outScene = SMScene.create(getDirector())
                _outScene!!.onEnter()
            }

            sceneOrder()

            return true
        }

        return false
    }

    protected open fun sceneOrder() {_isInSceneOnTop = true}

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)

        if (_isInSceneOnTop) {
            _outScene?.visit(m, flags)
            _inScene?.visit(m, flags)
        } else {
            _inScene?.visit(m, flags)
            _outScene?.visit()
        }
    }

    fun finish() {
        // clean up
        _inScene?.setVisible(true)
        _inScene?.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2)
        _inScene?.setScale(1f)
        _inScene?.setRotation(0f)

        _outScene?.setVisible(false)
        _outScene?.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2)
        _outScene?.setScale(1f)
        _outScene?.setRotation(0f)

        schedule(newSceneSchedule)
    }

    private val newSceneSchedule: SEL_SCHEDULE = object : SEL_SCHEDULE {
        override fun scheduleSelector(t: Float) {
            setNewScene(t)
        }
    }

    protected fun setNewScene(t:Float) {
        unschedule(newSceneSchedule)

        _isSendCleanupToScene = getDirector().isSendCleanupToScene()

        getDirector().replaceScene(_inScene!!)

        _inScene?.onTransitionReplaceSceneDidFinish()
        _outScene?.setVisible(true)
    }

    fun hideOutShowIn() {
        _inScene?.setVisible(true)
        _outScene?.setVisible(false)
    }

    fun getInScene():SMScene? {return _inScene}
    fun getDuration():Float {return _duration}

    fun TransitionSceneOnEnter() {
        SMSceneOnEnter()

        getDirector().setTouchEventDispatcherEnable(false)

        _outScene?.onExitTransitionDidStart()
        _inScene?.SMSceneOnEnter()
    }

    override fun onEnter() {
        super.onEnter()

        getDirector().setTouchEventDispatcherEnable(false)

        _outScene?.onExitTransitionDidStart()
        _inScene?.onEnter()
    }

    fun TransitionSceneOnExit() {
        SMSceneOnExit()
        getDirector().setTouchEventDispatcherEnable(true)

        _outScene?.SMSceneOnExit()
        _inScene?.onEnterTransitionDidFinish()
    }

    override fun onExit() {
        super.onExit()
        getDirector().setTouchEventDispatcherEnable(true)

        _outScene?.onExit()
        _inScene?.onEnterTransitionDidFinish()
    }

    override fun cleanup() {
        super.cleanup()

        if (_isSendCleanupToScene) {
            _outScene?.cleanup()
        }
    }
}