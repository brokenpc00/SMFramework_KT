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

open class SMButton : UIContainerView {

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
            for (i in 0 until stateToInt(STATE.MAX)) {
                target.add(null)
            }
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
                for (i in 0 until stateToInt(STATE.MAX)) {
                    targetColor.add(null)
                }
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
            val sc = srcColor?: Color4F(Color4F.TRANSPARENT)
            val dc = dstColor?:if (_style==STYLE.DEFAULT) Color4F(Color4F.WHITE) else Color4F(sc)
            val rc = sc.multiply(srcAlpha).add(dc.multiply(dstAlpha))
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

    fun initWithStyle(style: STYLE):Boolean {
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

    override fun setContentSize(width: Float?, height: Float?) {
        super.setContentSize(Size(width, height))
    }

    fun setButtonColor(state: STATE, color: Color4B) {
        setButtonColor(state, Color4F(color))
    }

    fun setButtonColor(state: STATE, color: Color4F) {
        if (_buttonColor==null) {
            _buttonColor = ArrayList(stateToInt(STATE.MAX))
            for (i in 0 until stateToInt(STATE.MAX)) {
                _buttonColor!!.add(null)
            }
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
            for (i in 0 until stateToInt(STATE.MAX)) {
                _iconColor!!.add(null)
            }
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
            for (i in 0 until stateToInt(STATE.MAX)) {
                _textColor!!.add(null)
            }
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
            for (i in 0 until stateToInt(STATE.MAX)) {
                _outlineColor!!.add(null)
            }
        }
        _outlineColor!![stateToInt(state)] = Color4F(color)
        registerUpdate(FLAG_OUTLINE_COLOR)
    }

    fun setButton(state: STATE, view: SMView?) {
        if (_buttonView==null && view!=null) {
            _buttonView = ArrayList(stateToInt(STATE.MAX))
            for (i in 0 until stateToInt(STATE.MAX)) {
                _buttonView!!.add(null)
            }

            _buttonView!![0] = null
            _buttonView!![1] = null

            if (_buttonColor==null) {
                _buttonColor = ArrayList(stateToInt(STATE.MAX))
                for (i in 0 until stateToInt(STATE.MAX)) {
                    _buttonColor!!.add(null)
                }
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

    fun setOutlineWidth(lineWidth: Float) {
        if (_shapeOutlineWidth==lineWidth) return

        if (lineWidth>0f) {
            val outlineView:SMShapeView? = when (_style) {
                STYLE.RECT, STYLE.SOLID_RECT -> {
                    SMRectView(getDirector())
                }
                STYLE.CIRCLE, STYLE.SOLID_CIRCLE -> {
                    SMCircleView(getDirector())
                }
                STYLE.ROUNDEDRECT, STYLE.SOLID_ROUNDEDRECT -> {
                    SMRoundRectView(getDirector(), 1f, ShapeConstant.LineType.SOLID)
                }
                else -> {
                    null
                }
            }

            if (outlineView!=null) {
                outlineView.setAnchorPoint(Vec2.MIDDLE)
                outlineView.setPosition(getContentSize().width/2f, getContentSize().height/2f)

                if (_buttonView==null) {
                    _buttonView = ArrayList(stateToInt(STATE.MAX))
                    for (i in 0 until stateToInt(STATE.MAX)) {
                        _buttonView!!.add(null)
                    }

                    _buttonView!![0] = null
                    _buttonView!![1] = null

                    if (_outlineColor==null) {
                        _outlineColor = ArrayList(stateToInt(STATE.MAX))
                        for (i in 0 until stateToInt(STATE.MAX)) {
                            _outlineColor!!.add(null)
                        }

                        _outlineColor!![0] = null
                        _outlineColor!![1] = null
                    }
                }

                setStateView(_buttonView!!, STATE.PRESSED, outlineView, AppConst.ZOrder.BUTTON_PRESSED, _outlineColor!!)

                registerUpdate(FLAG_CONTENT_SIZE)

                if (_outlineColor==null || _outlineColor!![0]==null) {
                    setOutlineColor(STATE.NORMAL, Color4F(0f, 0f, 0f, 1f))
                }
            }
        } else {
            setButton(STATE.PRESSED, null)
        }

        _shapeOutlineWidth = lineWidth
        registerUpdate(FLAG_SHAPE_STYLE)
    }

    fun setIconAlign(align: ICONALIGN) {
        _align = align
        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setIcon(state: STATE, view: SMView?) {
        if (_iconView==null && view!=null) {
            _iconView = ArrayList(stateToInt(STATE.MAX))
            for (i in 0 until stateToInt(STATE.MAX)) {
                _iconView!!.add(null)
            }

            _iconView!![0] = null
            _iconView!![1] = null

            if (_iconColor==null) {
                _iconColor = ArrayList(stateToInt(STATE.MAX))
                for (i in 0 until stateToInt(STATE.MAX)) {
                    _iconColor!!.add(null)
                }

                _iconColor!![0] = null
                _iconColor!![1] = null
            }
        }

        if (_iconView!=null) {
            setStateView(_iconView!!, state, view, if (state==STATE.NORMAL){ AppConst.ZOrder.BUTTON_ICON_NORMAL }else {AppConst.ZOrder.BUTTON_PRESSED},_iconColor!! )

            registerUpdate(FLAG_TEXT_ICON_POSITION)
            registerUpdate(FLAG_ICON_COLOR)
        }
    }

    fun setIcon(state: STATE, imageFileName: String) {
        if (imageFileName.isEmpty()) return

        val imageView = SMImageView.create(getDirector(), imageFileName)
        imageView.setAnchorPoint(Vec2.MIDDLE)
        imageView.setPosition(getContentSize().width/2f, getContentSize().height/2f)
        setIcon(state, imageView)
    }

    fun setText(text: String) {
        if (_textLabel==null) {
            setText(text, AppConst.DEFAULT_VALUE.FONT_SIZE)
        } else {
            if (_textLabel!!.getText()!=text) {
                _textLabel!!.setText(text)
            }
        }

        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setText(text: String, fontSize: Float) {
        if (_textLabel==null) {
            _textLabel = SMLabel.create(getDirector(), text, fontSize, Color4F.BLACK)
            _textLabel!!.setAnchorPoint(Vec2.MIDDLE)
            _uiContainer.addChild(_textLabel!!, AppConst.ZOrder.BUTTON_TEXT)
            if (_textColor==null || _textColor!![0]==null) {
                setTextColor(STATE.NORMAL, Color4F.BLACK)
            }
        } else {
            if (_textLabel!!.getText()!=text) {
                _textLabel!!.setText(text)
            }
        }

        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setUnderline() {
        _textUnderline = SMSolidRectView.create(getDirector())
        addChild(_textUnderline!!)
    }

    fun setIconPadding(padding: Float) {
        _iconPadding = padding
        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setIconScale(scale: Float) {
        _iconScale = scale
        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setTextScale(scale: Float) {
        _textScale = scale
        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setIconOffset(offset: Vec2) {
        _iconOffset.set(offset)
        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setTextOffset(offset: Vec2) {
        _textOffset.set(offset)
        registerUpdate(FLAG_TEXT_ICON_POSITION)
    }

    fun setShapeCornerRadius(radius: Float) {
        if (_style==STYLE.DEFAULT || _style==STYLE.RECT || _style==STYLE.SOLID_RECT) return

        _shapeRadius = radius
        registerUpdate(FLAG_SHAPE_STYLE)
    }

    fun setShapeCornerQuadrant(quadrant: Int) {
        if (_style==STYLE.DEFAULT) return

        _shapeQuardrant = quadrant
        registerUpdate(FLAG_SHAPE_STYLE)
    }

    fun setShapeAntiAliasWidth(width: Float) {
        if (_style==STYLE.DEFAULT) return

        _shapeAntiAliasWidth = width
        registerUpdate(FLAG_SHAPE_STYLE)
    }

    fun getButtonColor(state: STATE): Color4F? {return _buttonColor?.get(stateToInt(state))}

    fun getIconColor(state: STATE): Color4F? {return _iconColor?.get(stateToInt(state))}

    fun getTextColor(state: STATE): Color4F? {return _textColor?.get(stateToInt(state))}

    fun getOutlineColor(state: STATE): Color4F? {return _outlineColor?.get(stateToInt(state))}

    fun getButtonView(state: STATE): SMView? {
        return _buttonView?.get(stateToInt(state))
    }

    fun getIconView(state: STATE): SMView? {
        return _iconView?.get(stateToInt(state))
    }

    fun getTextLabel(): SMLabel? {return _textLabel}

    fun setPushDownRotate(rotate: Float) {_pushDownRotate = rotate}

    fun setPushDownOffset(offset: Vec2) {_pushDownOffset.set(offset)}

    fun setPushDownScale(scale: Float) {_pushDownScale = scale}

    override fun onUpdateOnVisit() {
        if (_updateFlags==0L) return

        if (getAlpha()==0f) return

        if (isUpdate(FLAG_CONTENT_SIZE)) {
            registerUpdate(FLAG_TEXT_ICON_POSITION)

            if (_buttonView!=null) {
                val size = _uiContainer.getContentSize()
                val center = Vec2(size.width/2f, size.height/2f)


                for (i in 0 until 2) {
                    val view = _buttonView!![i]
                    view?.setPosition(center)
                    view?.setContentSize(size)
                }
            }

            unregisterUpdate(FLAG_CONTENT_SIZE)
        }

        if (isUpdate(FLAG_TEXT_ICON_POSITION)) {
            var isContainedText = false
            val textSize = Size(Size.ZERO)
            if (_textLabel!=null) {
                textSize.set(_textLabel!!.getContentSize().width*_textScale, _textLabel!!.getContentSize().height*_textScale)
                isContainedText = true
            }

            var isContainedIcon = false
            val iconSize = Size(Size.ZERO)
            if (_iconView!=null) {
                for (i in 0 until 2) {
                    if (_iconView!![i]!=null) {
                        val s = Size(_iconView!![i]!!.getContentSize().width*_iconScale, _iconView!![i]!!.getContentSize().height*_iconScale)
                        iconSize.set(iconSize.width.coerceAtLeast(s.width), iconSize.height.coerceAtLeast(s.height))
                        isContainedIcon = true
                    }
                }
            }

            val size = _uiContainer.getContentSize()
            val center = Vec2(size.width/2f, size.height/2f)
            val textPosition = center
            val iconPosition = center
            var width = 0f
            var height = 0f

            // icon & text 공통
            if (isContainedText && isContainedIcon) {
                when (_align) {
                    ICONALIGN.LEFT -> {
                        width = textSize.width + _iconPadding + iconSize.width
                        iconPosition.x = (size.width-width+iconSize.width)/2f
                        textPosition.x = (size.width+width-textSize.width)/2f
                    }
                    ICONALIGN.RIGHT -> {
                        width = textSize.width + _iconPadding + iconSize.width
                        iconPosition.x = (size.width+width-iconSize.width)/2f
                        textPosition.x = (size.width-width+textSize.width)/2f
                    }
                    ICONALIGN.TOP -> {
                        height = textSize.height + _iconPadding + iconSize.height
                        iconPosition.y = (size.height+height-iconSize.height)/2f
                        textPosition.y = (size.height-height+textSize.height)/2f
                    }
                    ICONALIGN.BOTTOM -> {
                        height = textSize.height + _iconPadding + iconSize.height
                        iconPosition.y = (size.height-height+iconSize.height)/2f
                        textPosition.y = (size.height+height-textSize.height)/2f
                    }
                    else -> {

                    }
                }
            }

            // text
            if (isContainedText) {
                _textLabel?.setPosition(textPosition.x+_textOffset.x, textPosition.y+_textOffset.y)
                _textLabel?.setScale(_textScale)
                if (_textUnderline!=null) {
                    val extend = Size(0f, textSize.height/2f)
                    _textUnderline?.setPosition(textPosition.x+_textOffset.x, textPosition.y+_textOffset.y-extend.height)
                    _textUnderline?.setContentSize(Size(textSize.width, AppConst.DEFAULT_VALUE.LINE_WIDTH))
                }
            }

            // icon
            if (isContainedIcon) {
                for (i in 0 until 2) {
                    if (_iconView!![i]!=null) {
                        _iconView!![i]!!.setPosition(iconPosition.x+_iconOffset.x, iconPosition.y+_iconOffset.y)
                        _iconView!![i]!!.setScale(_iconScale)
                    }
                }
            }

            unregisterUpdate(FLAG_TEXT_ICON_POSITION)
        }

        if (isUpdate(FLAG_BUTTON_COLOR.or(FLAG_ICON_COLOR).or(FLAG_TEXT_COLOR).or(FLAG_OUTLINE_COLOR))) {
            // ToDo... color update
            unregisterUpdate(FLAG_BUTTON_COLOR.or(FLAG_ICON_COLOR).or(FLAG_TEXT_COLOR).or(FLAG_OUTLINE_COLOR))
        }

        if (isUpdate(FLAG_SHAPE_STYLE)) {
            if (_style!=STYLE.DEFAULT && _buttonView!=null) {
                for (i in 0 until 2) {
                    val view = _buttonView!![i]
                    if (view!=null) {
                        if (view is SMShapeView) {
                            val shape = view as SMShapeView
                            shape.setCornerRadius(_shapeRadius)
                            shape.setAntiAliasWidth(_shapeAntiAliasWidth)
                            if (i==0) {
                                shape.setLineWidth(_shapeLineWidth)
                            } else {
                                shape.setLineWidth(_shapeOutlineWidth)
                            }
                            shape.setConerQuadrant(_shapeQuardrant)
                        }
                    }
                }
            }

            unregisterUpdate(FLAG_SHAPE_STYLE)
        }

        onUpdateStateTransition(STATE.NORMAL, _buttonStateValue)
    }
}