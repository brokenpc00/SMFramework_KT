package com.interpark.smframework.base.transition

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMScene
import com.interpark.smframework.base.types.Mat4
import com.interpark.smframework.base.types.SEL_SCHEDULE

open class SwipeBack(director:IDirector) : BaseSceneTransition(director) {
    protected var _isCanceled:Boolean = false
    companion object {
        @JvmStatic
        fun create(director: IDirector, scene: SMScene): SwipeBack? {
            val t:SwipeBack = SwipeBack(director)
            return if (t.initWithDuration(0f, scene)) t else null
        }
    }

    override fun updateProgress(progress: Float) {
        if (_lastProgress==progress) return

        if (_isInSceneOnTop) {
            _inScene?.onTransitionProgress(Transition.SWIPE_IN, getTag(), progress)
            _outScene?.onTransitionProgress(Transition.PAUSE, getTag(), progress)
        } else {
            _inScene?.onTransitionProgress(Transition.RESUME, getTag(), progress)
            _outScene?.onTransitionProgress(Transition.SWIPE_OUT, getTag(), progress)
        }
        _lastProgress = progress
    }

    override fun draw(m: Mat4, flags: Int) {
        val progress = (_outScene!!.getPositionX()-getDirector().getWinSize().width/2) / getDirector().getWinSize().width
        updateProgress(progress)

        if (_menuDrawContainer!=null) {
            // exist drawable another menu!!!
        }

        super.draw(m, flags)
    }

    val cancelFunc: SEL_SCHEDULE = object : SEL_SCHEDULE {
        override fun scheduleSelector(t: Float) {
            cancelNewScene(t)
        }
    }

    override fun cancel() {
        // comeback outscene
        _isCanceled = true

        // stop out going
        _outScene!!.setVisible(true)
        _outScene!!.setPosition(getDirector().getWinSize().width/2f, getDirector().getWinSize().height/2f)
        _outScene!!.setScale(1f)
        _outScene!!.setRotation(0f)
        _outScene!!.onEnterTransitionDidFinish()

        // stop come in
        _inScene!!.setVisible(false)
        _inScene!!.setPosition(getDirector().getWinSize().width/2f, getDirector().getWinSize().height/2f)
        _inScene!!.setScale(1f)
        _inScene!!.setRotation(0f)

        schedule(cancelFunc)
    }

    override fun isNewSceneEnter(): Boolean { return false }

    fun cancelNewScene(t: Float) {
        unschedule(cancelFunc)
        getDirector().replaceScene(_inScene!!)
        getDirector().pushScene(_outScene!!)

        _inScene!!.setVisible(true)
    }

    override fun onEnter() {
        TransitionSceneOnEnter()

        getDirector().setTouchEventDispatcherEnable(true)

        if (_isInSceneOnTop) {
            _outScene?.onTransitionStart(Transition.PAUSE, getTag())
            _inScene?.onTransitionStart(Transition.SWIPE_IN, getTag())
        } else {
            _outScene?.onTransitionStart(Transition.SWIPE_OUT, getTag())
            _inScene?.onTransitionStart(Transition.RESUME, getTag())
        }

        val inMenu:Boolean = _inScene!!.isMainMenuEnable()
        val outMenu:Boolean = _outScene!!.isMainMenuEnable()

        if (outMenu) {
            if (inMenu) {
                _menuDrawType = MenuDrawType.OO
            } else {
                _menuDrawType = MenuDrawType.OX
            }
        } else {
            if (inMenu) {
                _menuDrawType = MenuDrawType.XO
            } else {
                _menuDrawType = MenuDrawType.XX
            }
        }

        if (_menuDrawContainer!=null) {
            // exist drawable another menu
        }
    }

    override fun onExit() {
        SMSceneOnExit()

        getDirector().setTouchEventDispatcherEnable(true)

        if (_isCanceled) {
            if (_isInSceneOnTop) {
                _inScene?.onTransitionComplete(Transition.SWIPE_OUT, getTag())
                _outScene?.onTransitionComplete(Transition.RESUME, getTag())
                _outScene?.onEnterTransitionDidFinish()
            } else {
                _inScene?.onTransitionComplete(Transition.PAUSE, getTag())
                _outScene?.onTransitionComplete(Transition.SWIPE_IN, getTag())
                _outScene?.onEnterTransitionDidFinish()
            }
            _inScene!!.onExit()
        } else {
            if (_isInSceneOnTop) {
                _inScene?.onTransitionComplete(Transition.SWIPE_IN, getTag())
                _outScene?.onTransitionComplete(Transition.PAUSE, getTag())
            } else {
                _outScene?.onTransitionComplete(Transition.SWIPE_OUT, getTag())
                _inScene?.onTransitionComplete(Transition.RESUME, getTag())
            }
            _outScene!!.onExit()
        }

        if (_menuDrawContainer!=null) {
            // exist drawable another menu
        }
    }

    override fun sceneOrder() {
        _isInSceneOnTop = false
    }
}