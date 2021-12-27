package com.brokenpc.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.tweenfunc
import com.brokenpc.smframework.view.SMLabel

class SMToastBar(director: IDirector): SMView(director) {
    private var _colorInit = false
    private var _labelIndex = -1
    private var _label: ArrayList<SMLabel?> = ArrayList(2)
    private var _textContainer: SMView? = null
    private var _callback: ToastBarCallback? = null
    private var _clipRect = Rect(Rect.ZERO)
    private val _toastBgColor = Color4F(Color4F.TRANSPARENT)

    companion object {
        const val FONT_SIZE = 38f
        const val SHOW_TIME = 0.2f
        const val HIDE_TIME = 0.2f
        const val TRANS_TIME = 0.5f
        const val MOVE_TIME = 0.3f
        const val TEXT_CHANGE_TIME = 0.2f
        const val MIN_HEIGHT = 195.0f
        const val PADDING = 30.0f

        @JvmStatic
        fun create(director: IDirector, callback: ToastBarCallback): SMToastBar {
            val bar = SMToastBar(director)
            bar.initWithCallback(callback)
            return bar
        }
    }

    init {
        _label.add(null)
        _label.add(null)
    }

    fun setMessage(message: String, color: Color4F, duration: Float) {

        setVisible(true)

        _labelIndex = (_labelIndex+1) % 2

        if (_label[_labelIndex] == null) {
            val label = SMLabel.create(getDirector(), message, FONT_SIZE, Color4F.WHITE)
            label.setAnchorPoint(0.5f, 0.5f)
            _textContainer?.addChild(label)
            _label[_labelIndex] = label
        } else {
            _label[_labelIndex]!!.setText(message)
            _label[_labelIndex]!!.setVisible(true)
        }

        val reqHeight = MIN_HEIGHT.coerceAtLeast(_label[_labelIndex]!!.getContentSize().height + PADDING*2)
        _label[_labelIndex]!!.setPosition(_contentSize.width/2f, reqHeight/2f)

        if (_label[1-_labelIndex]==null) {
            setContentSize(_contentSize.width, reqHeight)
            setPositionY(reqHeight)
            _textContainer?.setPosition(_contentSize.width/2f, _contentSize.height/2f)

            val a = TransformAction.create(getDirector())
            a.toPositoinY(reqHeight).setTweenFunc(tweenfunc.TweenType.Cubic_EaseOut)
            a.setTimeValue(SHOW_TIME, 0f)
            runAction(a)
        } else {
            val l1 = _label[1-_labelIndex] // previous label
            val l2 = _label[_labelIndex] // current label

            l1?.stopAllActions()
            l2?.stopAllActions()

            _textContainer?.stopAllActions()
            stopAllActions()

            // hide previous label
            if (l1?.isVisible() == true) {
                val hide = TransformAction.create(getDirector())
                hide.toAlpha(0f).invisibleOnFinish()
                hide.setTimeValue(TEXT_CHANGE_TIME, 0f)
                l1?.runAction(hide)
            }

            // show current label
            if (l2?.getAlpha()?:0f < 1f) {
                val show = TransformAction.create(getDirector())
                show.toAlpha(1f)
                show.setTimeValue(TEXT_CHANGE_TIME, 0f)
                l2?.runAction(show)
            }

            // move label position
            if (_textContainer?.getPositionY()!=reqHeight/2f) {
                val moveY = TransformAction.create(getDirector())
                moveY.toPositoinY(reqHeight/2f)
                moveY.setTimeValue(TEXT_CHANGE_TIME, 0f)
                _textContainer?.runAction(moveY)
            }

            // move bar position
            if (getPositionY()!=reqHeight) {
                val moveY = TransformAction.create(getDirector())
                moveY.toPositoinY(reqHeight).setTweenFunc(tweenfunc.TweenType.Back_EaseOut)
                moveY.setTimeValue(MOVE_TIME, 0f)
                runAction(moveY)
            }
        }

        setBgColor(color)

        if (isScheduled(_timeout)) {
            unschedule(_timeout)
        }

        scheduleOnce(_timeout, duration)

        val action = TransformAction.create(getDirector())
        action.toAlpha(1f).toScale(1f).setTweenFunc(tweenfunc.TweenType.Back_EaseOut)
        action.setTimeValue(0.2f, 0.1f)
        _label[_labelIndex]?.setAlpha(0f)
        _label[_labelIndex]?.setScale(0.8f)
        _label[_labelIndex]?.runAction(action)
    }

    private var _timeout = object : SEL_SCHEDULE {
        override fun scheduleSelector(t: Float) {
            onTimeOut(t)
        }
    }

    fun setBgColor(color: Color4F) {
        if (color.equal(_toastBgColor)) return

        _toastBgColor.set(color)

        if (!_colorInit) {
            _colorInit = true
            setBackgroundColor(_toastBgColor)
        } else {
            setBackgroundColor(_toastBgColor, 0.25f)
        }
    }

    override fun visit(parentTransform: Mat4, parentFlags: Int) {
        if (!_visible) return

//        val reqHeight = MIN_HEIGHT.coerceAtLeast(_label[_labelIndex]?.getContentSize()?.height?:0f + PADDING*2f)
//        setScissorRect(Rect(0f, reqHeight, _contentSize.width, getPositionY()))
        setScissorRect(Rect(0f, 0f, _contentSize.width, getPositionY()))

        super.visit(parentTransform, parentFlags)
    }

    protected fun initWithCallback(callback: ToastBarCallback): Boolean {
        _callback = callback

        val s = getDirector().getWinSize()
        setContentSize(s.width, 0f)

        _textContainer = SMView.create(getDirector())
        _textContainer!!.setAnchorPoint(Vec2.MIDDLE)
        _textContainer!!.setCascadeAlphaEnable(true)
        addChild(_textContainer!!)


        setVisible(false)

        setScissorEnable(true)

        return true
    }

    private fun onTimeOut(a: Float) {
        stopAllActions()

        val moveY = TransformAction.create(getDirector())
        moveY.toPositoinY(0f).setTweenFunc(tweenfunc.TweenType.Cubic_EaseOut).runFuncOnFinish(object : TransformAction.TransformFunc {
            override fun onFinish(target: SMView?, tag: Int) {
                onHideComplete(target, tag)
            }
        })
        moveY.setTimeValue(HIDE_TIME, 0f)
        runAction(moveY)
    }

    private fun onHideComplete(target: SMView?, tag: Int) {
        _colorInit = false

        for (i in 0 until 2) {
            if (_label[i]!=null) {
                _textContainer?.removeChild(_label[i])
                _label[i] = null
            }
        }

        setVisible(false)

        _callback?.onToastBarHide(this)
    }


    interface ToastBarCallback {
        fun onToastBarHide(bar: SMToastBar)
    }

    override fun setContentSize(size: Size) {
        _textContainer?.setContentSize(size)
        super.setContentSize(size)
    }

    override fun setContentSize(width: Float?, height: Float?) {
        _textContainer?.setContentSize(width, height)
        super.setContentSize(width, height)
    }

}