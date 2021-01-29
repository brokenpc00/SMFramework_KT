package com.brokenpc.smframework.view

import android.os.Build
import android.view.MotionEvent
import com.brokenpc.app.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.UIContainerView
import com.brokenpc.smframework.base.shape.ShapeConstant
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.transition.StateTransitionAction
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.AppConst

class SMButton : UIContainerView {

    constructor(director: IDirector) : super(director) {
        setPosition(0f, 0f)
        setAnchorPoint(0f, 0f)
        setContentSize(0f, 0f)
        _style = STYLE.DEFAULT
        setTag(0)
    }

    constructor(director: IDirector, tag: Int, style: STYLE, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float) : super(director) {
        setPosition(x, y)
        setAnchorPoint(anchorX, anchorY)
        setContentSize(width, height)
        setTag(tag)

        setClickable(true)
        initWithStyle(style)
    }

    enum class STYLE {
        DEFAULT, RECT, ROUNDEDRECT, CIRCLE, SOLID_RECT, SOLID_ROUNDEDRECT, SOLID_CIRCLE
    }
    enum class ICONALIGN {
        CENTER, LEFT, RIGHT, TOP, BOTTOM
    }

    private var _smoothFlags:Long = 0L
    private var _style:STYLE = STYLE.DEFAULT
    private var _align:ICONALIGN = ICONALIGN.CENTER

    private var _textLabel:SMLabel? = null
    private var _iconColor:ArrayList<Color4F?>? = null
    private var _textColor:ArrayList<Color4F?>? = null
    private var _buttonColor:ArrayList<Color4F?>? = null
    private var _outlineColor:ArrayList<Color4F?>? = null

    private var _buttonView:ArrayList<SMView?>? = null
    private var _iconView:ArrayList<SMView?>? = null

    private var _pushDownRotate:Float = 0f

    private val _pushDownOffset:Vec2 = Vec2(0f, 0f)
    private var _textUnderline:SMSolidRectView? = null

    private var _pushDownScale:Float = 1f
    private var _iconScale:Float = 1f
    private var _textScale:Float = 1f
    private var _iconPadding:Float = 0f

    private val _iconOffset:Vec2 = Vec2(0f, 0f)
    private val _textOffset:Vec2 = Vec2(0f, 0f)

    private var _shapeRadius:Float = 0f
    private var _shapeQuardrant:Int = 0
    private var _shapeLineWidth:Float = 1f
    private var _shapeAntiAliasWidth:Float = 1f
    private var _shapeOutlineWidth:Float = 1f

    private var _buttonPressAction:StateTransitionAction? = null
    private var _buttonReleaseAction:StateTransitionAction? = null

    private var _buttonPressAnimationTime:Float = 0f
    private var _buttonReleaseAnimationTime:Float = 0f
    private var _buttonStateValue:Float = 0f

    private var _buttonPressActionTime:Float = AppConst.Config.BUTTON_STATE_CHANGE_NORMAL_TO_PRESS_TIME
    private var _buttonReleaseActionTime:Float = AppConst.Config.BUTTON_STATE_CHANGE_PRESS_TO_NORMAL_TIME

    companion object {
        const val FLAG_CONTENT_SIZE:Long = 1
        const val FLAG_BUTTON_COLOR:Long = FLAG_CONTENT_SIZE.shr(1)
        const val FLAG_ICON_COLOR:Long = FLAG_CONTENT_SIZE.shr(2)
        const val FLAG_TEXT_COLOR:Long = FLAG_CONTENT_SIZE.shr(3)
        const val FLAG_OUTLINE_COLOR:Long = FLAG_CONTENT_SIZE.shr(4)
        const val FLAG_TEXT_ICON_POSITION:Long = FLAG_CONTENT_SIZE.shr(5)
        const val FLAG_SHAPE_STYLE:Long = FLAG_CONTENT_SIZE.shr(6)

        @JvmStatic
        fun create(director: IDirector):SMButton {return create(director, 0)}

        @JvmStatic
        fun create(director: IDirector, tag:Int):SMButton {return create(director, tag, STYLE.DEFAULT)}

        @JvmStatic
        fun create(director: IDirector, tag: Int, style: STYLE):SMButton {return create(director, tag, style, 0f, 0f)}

        @JvmStatic
        fun create(director: IDirector, tag: Int, style: STYLE, x:Float, y:Float):SMButton {return create(director, tag, style, x, y, 0f, 0f)}

        @JvmStatic
        fun create(director: IDirector, tag: Int, style: STYLE, x: Float, y: Float, width:Float, height:Float):SMButton {return create(director, tag, style, x, y, width, height, 0f, 0f)}

        @JvmStatic
        fun create(director: IDirector, tag: Int, style: STYLE, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float):SMButton {
            return SMButton(director, tag, style, x, y, width, height, anchorX, anchorY)
        }
    }

    private fun stateToInt(state:SMView.STATE):Int {
        return when(state) {
            STATE.PRESSED -> 1
            STATE.MAX -> 2
            else -> 0
        }
    }

    private fun setStateColor(target:ArrayList<Color4F?>?, state: STATE, color: Color4F) {
        var target:ArrayList<Color4F?>? = target
        if (target==null) {
            target = ArrayList(stateToInt(STATE.MAX))
        }
        target[stateToInt(state)] = Color4F(color)
    }

    private fun getTargetView(isButton: Boolean):ArrayList<SMView?>? {
        return if (isButton) { _buttonView } else { _iconView }
    }

    private fun setTargetView(target:ArrayList<SMView?>?, isButton: Boolean) {
        if (isButton) {
            _buttonView = target
        } else {
            _iconView = target
        }
    }

    private fun getTargetColor(colorType:Int):ArrayList<Color4F?>? {
        return when (colorType) {
            1->_buttonColor
            2->_iconColor
            else->_outlineColor
        }
    }

    private fun setTargetColor(colors: ArrayList<Color4F?>?, colorType:Int) {
        when (colorType) {
            1->_buttonColor = colors
            2->_iconColor = colors
            else->_outlineColor = colors
        }
    }

    private fun setStateView(target: ArrayList<SMView?>, state: SMView.STATE, view: SMView?, localZOrder:Int, targetColor:ArrayList<Color4F?>) {
        val currentStateView = target[stateToInt(state)]
        if (currentStateView!=view) {
            if (currentStateView!=null) {
                _uiContainer.removeChild(currentStateView)
            }
            if (view!=null) {
                _uiContainer.addChild(view, localZOrder)
            }
            target[stateToInt(state)] = view
        }
    }

    private fun setStateView(isButton: Boolean, state: STATE, view: SMView?, localZOrder: Int, colorType: Int) {
        // color type 1: Button, 2: Icon, 3: Outline
        var target:ArrayList<SMView?>? = getTargetView(isButton)
        var targetColor:ArrayList<Color4F?>? = getTargetColor(colorType)
        if (target==null && view!=null) {
            target = ArrayList(stateToInt(STATE.MAX))
            if (targetColor==null) {
                targetColor = ArrayList(stateToInt(STATE.MAX))
            }
        }

        val currentStateView:SMView? = target?.get(stateToInt(state))

        if (currentStateView!=view) {
            if (currentStateView!=null) {
                _uiContainer.removeChild(currentStateView)
            }
            if (view!=null) {
                _uiContainer.addChild(view)
            }
        }
        target?.set(stateToInt(state), view)


        setTargetView(target, isButton)
        setTargetColor(targetColor, colorType)
    }

    override fun onStateChangeNormalToPress(event: MotionEvent) {
        if (_pushDownOffset.x!=0.0f || _pushDownOffset.y!=0.0f) {
            _uiContainer.setAnimOffset(_pushDownOffset)
        }

        if (_pushDownScale!=1.0f) {
            _uiContainer.setAnimScale(_pushDownScale)
        }

        if (_pushDownRotate!=0.0f) {
            _uiContainer.setAnimRotate(_pushDownRotate)
        }

        if (_buttonPressAction==null) {
            _buttonPressAction = StateTransitionAction.create(getDirector(), STATE.PRESSED)
            _buttonPressAction?.setTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS)
        }

        if (getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_DELAY)!=null) {
            stopAction(getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_DELAY)!!)
        }

        if (getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS)==null) {
            _buttonPressAction?.setDuration(_buttonPressActionTime)
            runAction(_buttonPressAction!!)
        }
    }

    override fun onStateChangePressToNormal(event: MotionEvent) {
        _uiContainer.setAnimOffset(Vec2.ZERO)
        _uiContainer.setAnimScale(1.0f)
        _uiContainer.setAnimRotate(0.0f)

        if (_buttonReleaseAction==null) {
            _buttonReleaseAction = StateTransitionAction.create(getDirector(), STATE.NORMAL)
            _buttonReleaseAction?.setTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL)
        }

        if (getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS)!=null) {
            val action = getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_NORMAL_TO_PRESS) as StateTransitionAction
            val minTime:Float = action.getDuration()*0.3f - action.getElapsed()
            if (minTime>0) {
                _buttonReleaseAction?.setDuration(_buttonReleaseActionTime)

                val sequence: Sequence = Sequence.Companion.createWithTwoActions(getDirector(), DelayTime.create(getDirector(), minTime), _ReleaseActionStarter(getDirector()))!!
                sequence.setTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_DELAY)
                runAction(sequence)
                return
            }
        }

        if (getActionByTag(AppConst.TAG.ACTION_VIEW_STATE_CHANGE_PRESS_TO_NORMAL)==null) {
            _buttonReleaseAction?.setDuration(_buttonReleaseActionTime)
            runAction(_buttonReleaseAction!!)
        }
    }

    inner class _ReleaseActionStarter(director: IDirector) : ActionInstant(director) {
        override fun update(dt: Float) {
            val btn = _target as SMButton
            btn.runAction(btn._buttonReleaseAction!!)
        }
    }

    fun viewColorChange(srcView:SMView?, dstView:SMView?, srcColor:Color4F?, dstColor:Color4F?, t: Float) {
        var srcAlpha = 1.0f-t
        var dstAlpha = t
        if (dstView!=null) {
            if (srcColor!=null) {
                srcAlpha *= srcColor.a
                if (srcView!=null) {
                    val tintColor:Color4F = Color4F(srcColor)
                    tintColor.a = srcAlpha
                    srcView.setColor(tintColor)
                }
            }
            if (dstColor!=null) {
                dstAlpha *= dstColor.a
                val tintColor = Color4F(dstColor)
                tintColor.a = dstAlpha
                dstView.setColor(tintColor)
            }
            srcView?.setVisible(srcAlpha>0f)
            dstView.setVisible(dstAlpha>0f)
        } else if (srcView!=null) {
            val sc = Color4F(srcColor?: Color4F.TRANSPARENT)
            val dc = Color4F(dstColor?:if (_style==STYLE.DEFAULT) Color4F.WHITE else sc)
            val rc = Color4F(sc.multiply(srcAlpha).add(dc.multiply(dstAlpha)))
            srcView.setColor(rc)
        }
    }

    fun onUpdateStateTransition(state: STATE, t: Float) {
        _buttonStateValue = t
        if (_buttonView!=null) {
            if (_style==STYLE.DEFAULT) {
                viewColorChange(_buttonView!![0], _buttonView!![1], _buttonColor!![0], _buttonColor!![1], t)
            } else {
                if (_buttonColor!=null) {
                    viewColorChange(_buttonView!![0], null, _buttonColor!![0], _buttonColor!![1], t)
                }
                if (_outlineColor!=null) {
                    viewColorChange(_buttonView!![0], null, _outlineColor!![0], _outlineColor!![1], t)
                }
            }
        }

        if (_iconView!=null) {
            viewColorChange(_iconView!![0], _iconView!![1], _iconColor!![0], _iconColor!![1], t)
        }

        if (_textLabel!=null) {
            viewColorChange(_textLabel, null, _textColor!![0], _textColor!![1], t)
            if (_textUnderline!=null) {
                viewColorChange(_textUnderline, null, _textColor!![0], _textColor!![1], t)
            }
        }
    }

    protected fun initWithStyle(style: STYLE):Boolean {
        _style = style
        var buttonView:SMView? = when(_style) {
            STYLE.RECT -> SMRectView(getDirector())
            STYLE.SOLID_RECT -> SMSolidRectView(getDirector())
            STYLE.ROUNDEDRECT -> SMRoundRectView(getDirector(), 1.0f, ShapeConstant.LineType.SOLID)
            STYLE.SOLID_ROUNDEDRECT -> SMSolidRoundRectView(getDirector())
            STYLE.CIRCLE -> SMCircleView(getDirector())
            STYLE.SOLID_CIRCLE -> SMSolidCircleView(getDirector())
            else -> null
        }

        if (buttonView!=null) {
            _shapeRadius = 0.0f
            _shapeLineWidth = AppConst.DEFAULT_VALUE.LINE_WIDTH
            _shapeAntiAliasWidth = 1.0f
            _shapeOutlineWidth = AppConst.DEFAULT_VALUE.LINE_WIDTH

            buttonView.setBackgroundColor(Color4F.TRANSPARENT)
            buttonView.setAnchorPoint(Vec2.MIDDLE)
            buttonView.setPosition(getContentSize().width/2.0f, getContentSize().height/2.0f)
            buttonView.setContentSize(getContentSize())

            setButton(STATE.NORMAL, buttonView)
            setButtonColor(STATE.NORMAL, Color4F.WHITE)
        }

        return true
    }

    override fun isClickable(): Boolean {
        return true
    }

    override fun setContentSize(size: Size) {
        super.setContentSize(size)
        registerUpdate(FLAG_CONTENT_SIZE)
    }

    override fun setContentSize(width: Float, height: Float) {
        super.setContentSize(Size(width, height))
    }

    fun setButtonColor(state: STATE, color: Color4B) {
        setButtonColor(state, Color4F(color))
    }

    fun setButtonColor(state: STATE, color: Color4F) {
        if (_buttonColor==null) {
            _buttonColor = ArrayList(stateToInt(STATE.MAX))
        }

        _buttonColor!![stateToInt(state)] = Color4F(color)
        registerUpdate(FLAG_BUTTON_COLOR)
    }

    fun setIconColor(state: STATE, color: Color4B) {
        setIconColor(state, Color4F(color))
    }

    fun setIconColor(state: STATE, color: Color4F) {
        if (_iconColor==null) {
            _iconColor = ArrayList(stateToInt(STATE.MAX))
        }
        _iconColor!![stateToInt(state)] = Color4F(color)
        registerUpdate(FLAG_ICON_COLOR)
    }

    fun setTextColor(state: STATE, color: Color4B) {
        setTextColor(state, Color4F(color))
    }

    fun setTextColor(state: STATE, color: Color4F) {
        if (_textColor==null) {
            _textColor = ArrayList(stateToInt(STATE.MAX))
        }
        _textColor!![stateToInt(state)] = Color4F(color)
        registerUpdate(FLAG_TEXT_COLOR)
    }

    fun setOutlineColor(state: STATE, color: Color4B) {
        setOutlineColor(state, color)
    }

    fun setOutlineColor(state: STATE, color: Color4F) {
        if (_outlineColor==null) {
            _outlineColor = ArrayList(stateToInt(STATE.MAX))
        }
        _outlineColor!![stateToInt(state)] = Color4F(color)
        registerUpdate(FLAG_OUTLINE_COLOR)
    }

    fun setButton(state: STATE, view: SMView?) {
        if (_buttonView==null && view!=null) {
            _buttonView = ArrayList(stateToInt(STATE.MAX))
            _buttonView!![0] = null
            _buttonView!![1] = null

            if (_buttonColor==null) {
                _buttonColor = ArrayList(stateToInt(STATE.MAX))
                _buttonColor!![0] = null
                _buttonColor!![1] = null
            }
        }

        view?.setAnchorPoint(Vec2.MIDDLE)
        setStateView(_buttonView!!, state, view, if (state==STATE.NORMAL) AppConst.ZOrder.BUTTON_NORMAL else AppConst.ZOrder.BUTTON_PRESSED, _buttonColor!!)

        if (view!=null) {
            registerUpdate(FLAG_CONTENT_SIZE)
            registerUpdate(FLAG_BUTTON_COLOR)
        }
    }

    fun setButton(state: STATE, imageFileName: String) {
        if (BuildConfig.DEBUG && _style!=STYLE.DEFAULT) {
            error("Style must be DEFAULT!!!")
        }

        if (imageFileName.isEmpty()) return

        val sprite = BitmapSprite.createFromFile(getDirector(), imageFileName, false, null, 0) ?: return

        val imageView: SMImageView

    }

}