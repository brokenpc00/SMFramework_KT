package com.brokenpc.smframework.base.transition

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.view.SMSolidRectView


open class BaseSceneTransition(director:IDirector) : TransitionScene(director) {

    enum class MenuDrawType {
        OO, OX, XO, XX
    }

    var _menuDrawType:MenuDrawType = MenuDrawType.OO
    var _menuDrawContainer:SMView? = null
    var _dimLayer:SMSolidRectView? = null

    inner class ProgressUpdater(director: IDirector, t:Float) : DelayBaseAction(director) {
        init {
            setTimeValue(t, 0f)
        }

        override fun onUpdate(t: Float) {
            (_target as BaseSceneTransition).updateProgress(t)
        }

        override fun onEnd() {
            (_target as BaseSceneTransition).updateComplete()
        }
    }

    companion object {
        const val DEFAULT_DELAY_TIME:Float = 0.1f
        const val MaxDimRatio:Float = 0.4f

    }

    protected open fun isNewSceneEnter():Boolean {return false}

    open fun getInAction():FiniteTimeAction? {return null}

    open fun getOutAction():FiniteTimeAction? {return null}

    open fun isDimLayerEnable():Boolean {return true}


    fun getOutScene():SMScene? {return _outScene}

    fun baseTransitionDraw(m:Mat4, flags:Int) {
        if (isDimLayerEnable() && _lastProgress>0f && _dimLayer==null) {
            _dimLayer = SMSolidRectView(getDirector())
            _dimLayer!!.setContentSize(getDirector().getWidth().toFloat(), getDirector().getHeight().toFloat())
            _dimLayer!!.setAnchorPoint(Vec2.MIDDLE)
            _dimLayer!!.setPosition(getDirector().getWinSize().width/2, getDirector().getWinSize().height/2)
            _dimLayer!!.setColor(Color4F.TRANSPARENT)
        }

        if (_isInSceneOnTop) {
            _outScene?.visit(m, flags)

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.OX) {
                _menuDrawContainer!!.setVisible(true)
                _menuDrawContainer!!.visit(m, flags)
            }

            if (_lastProgress>0f && _lastProgress<1f && _dimLayer!=null) {
                val alpha = MaxDimRatio*_lastProgress
                _dimLayer!!.setColor(0f, 0f, 0f, alpha)
                _dimLayer!!.visit(m, flags)
            }

            _inScene!!.visit(m, flags)

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.XO) {
                _menuDrawContainer!!.setVisible(true)
                _menuDrawContainer!!.visit(m, flags)
            }
        } else {
            _inScene?.visit(m, flags)

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.XO) {
                _menuDrawContainer!!.setVisible(true)
                _menuDrawContainer!!.visit(m, flags)
            }

            if (_lastProgress>0f && _lastProgress<1f && _dimLayer!=null) {
                _dimLayer!!.setColor(0f, 0f, 0f, MaxDimRatio*(1f-_lastProgress))
                _dimLayer!!.visit(m, flags)
            }

            _outScene!!.visit(m, flags)

            if (_menuDrawContainer!=null && _menuDrawType==MenuDrawType.OX) {
                _menuDrawContainer!!.setVisible(true)
                _menuDrawContainer!!.visit(m, flags)
            }
        }
    }

    override fun draw(m: Mat4, flags: Int) {
        baseTransitionDraw(m, flags)
    }

    override fun onEnter() {
        super.onEnter()

        val inMenu:Boolean = _inScene!!.isMainMenuEnable()
        val outMenu:Boolean = _outScene!!.isMainMenuEnable()

        _menuDrawType = if (outMenu) {
            if (inMenu) {
                MenuDrawType.OO
            } else {
                MenuDrawType.OX
            }
        } else {
            if (inMenu) {
                MenuDrawType.XO
            } else {
                MenuDrawType.XX
            }
        }

        if (_menuDrawType == MenuDrawType.OX || _menuDrawType == MenuDrawType.XO) {
            // Todo another menu... Make If you need to another menu
        }


        var inAction:FiniteTimeAction? = getInAction()
        var outAction:FiniteTimeAction? = getOutAction()

        if (inAction==null) {
            inAction = DelayTime.create(getDirector(), _duration)
        }

        if (_isInSceneOnTop) {
            val seq:Sequence = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), inAction, null)!!
            _inScene?.runAction(seq)
            if (outAction!=null) {
                var seq2:Sequence = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), outAction, null)!!
                _outScene?.runAction(seq2)
            }

            runAction(Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), ProgressUpdater(getDirector(), _duration), CallFunc.create(getDirector(), object : PERFORM_SEL {
                override fun performSelector() {
                    finish()
                }
            }), null)!!)
        } else {
            _inScene?.runAction(inAction!!)

            if (outAction!=null) {
                val seq2:Sequence = Sequence.create(getDirector(), DelayTime.create(getDirector(), DEFAULT_DELAY_TIME), outAction, null)!!
                _outScene?.runAction(seq2)
            }

            runAction(Sequence.create(getDirector(), ProgressUpdater(getDirector(), _duration), CallFunc.create(getDirector(), object : PERFORM_SEL {
                override fun performSelector() {
                    finish()
                }
            }), null)!!)
        }

        if (!isNewSceneEnter()) {
            val outScene:SMScene? = _outScene
            val inScene:SMScene? = _inScene
            inScene?.onSceneResult(outScene, outScene?.getSceneParams())
        }

        if (_isInSceneOnTop) {
            _outScene?.onTransitionStart(Transition.PAUSE, getTag())
            _inScene?.onTransitionStart(Transition.IN, getTag())
        } else {
            _outScene?.onTransitionStart(Transition.OUT, getTag())
            _inScene?.onTransitionStart(Transition.RESUME, getTag())
        }
    }

    override fun onExit() {
        super.onExit()

        // touch enable
        getDirector().setTouchEventDispatcherEnable(true)

        if (_isInSceneOnTop) {
            _outScene?.onTransitionComplete(Transition.PAUSE, getTag())
            _inScene?.onTransitionComplete(Transition.SWIPE_IN, getTag())
        } else {
            _outScene?.onTransitionComplete(Transition.SWIPE_OUT, getTag())
            _inScene?.onTransitionComplete(Transition.RESUME, getTag())
        }
        _outScene?.onExit()
        _inScene?.onEnterTransitionDidFinish()

        if (_menuDrawContainer!=null) {
            // ToDo... 나중에 메뉴관련 추가
        }
    }

    protected open fun updateProgress(progress:Float) {
        if (_lastProgress!=progress) {
            if (_isInSceneOnTop) {
                _inScene?.onTransitionProgress(Transition.IN, getTag(), progress)
                _outScene?.onTransitionProgress(Transition.PAUSE, getTag(), progress)
            } else {
                _inScene?.onTransitionProgress(Transition.RESUME, getTag(), progress)
                _outScene?.onTransitionProgress(Transition.OUT, getTag(), progress)
            }
            _lastProgress = progress
        }
    }

    protected open fun updateComplete() {
        if (_isInSceneOnTop) {
            _outScene?.onTransitionComplete(Transition.PAUSE, getTag())
            _inScene?.onTransitionComplete(Transition.IN, getTag())
        } else {
            _outScene?.onTransitionComplete(Transition.OUT, getTag())
            _inScene?.onTransitionComplete(Transition.RESUME, getTag())
        }

        _inScene?.onEnterTransitionDidFinish()
    }
}