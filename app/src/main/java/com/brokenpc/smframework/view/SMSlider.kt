package com.brokenpc.smframework.view

import android.view.MotionEvent
import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.UIContainerView
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ShaderNode
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.SMButton
import com.brokenpc.smframework.view.SMCircleView
import com.brokenpc.smframework.view.SMSolidCircleView
import com.brokenpc.smframework.view.SMSolidRectView
import kotlin.math.abs
import kotlin.math.min

class SMSlider(director: IDirector): UIContainerView(director), SMView.OnTouchListener {
    private var _knobButton: KnobButton? = null
    private var _minButton: KnobButton? = null
    private var _maxButton: KnobButton? = null

    private var _knobPoint = Vec2(Vec2.ZERO)
    private var _minPoint = Vec2(Vec2.ZERO)
    private var _maxPoint = Vec2(Vec2.ZERO)

    private var _sliderValue = 0f
    private var _minValue = 0f
    private var _maxValue = 1f

    private var _sliderWidth = 1f
    private var _knobFocused = false
    private var _minFocused = false
    private var _maxFocused = false

    private var _type: Type = Type.ZERO_TO_ONE

    private var _bgLine: SMSolidRectView? = null
    private var _leftLine: SMSolidRectView? = null
    private var _rightLine: SMSolidRectView? = null
    private var _circle: SMCircleView? = null

    companion object {
        class InnerColor(bgLine: Color4F, fgLine: Color4F, knobNormal: Color4F, knobPress: Color4F) {
            var bgLine: Color4F = bgLine
            var fgLine: Color4F = fgLine
            var knobNormal: Color4F = knobNormal
            var knobPress: Color4F = knobPress
        }

        val LIGHT = InnerColor(MakeColor4F(0xdbdcdf, 1f), MakeColor4F(0x222222, 1f), MakeColor4F(0xffffff, 1f), MakeColor4F(0xeeeff1, 1f))
        val DARK = InnerColor(MakeColor4F(0x5e5e5e, 1f), MakeColor4F(0xffffff, 1f), MakeColor4F(0xffffff, 1f), MakeColor4F(0xeeeff1, 1f))
        const val CENTER_RAIDUS = 10f
        const val FLAG_SLIDE_VALUE = 1L
        const val FLAG_CONTENT_SIZE = (1L).shl(1)

        @JvmStatic
        fun create(director: IDirector): SMSlider {
            return create(director, Type.ZERO_TO_ONE)
        }

        @JvmStatic
        fun create(director: IDirector, type: Type): SMSlider {
            return create(director, type, LIGHT)
        }

        @JvmStatic
        fun create(director: IDirector, type: Type, initColor: InnerColor): SMSlider {
            val slider = SMSlider(director)
            slider.initWithType(type, initColor)
            return slider
        }
    }

    enum class Type {
        MINUS_ONE_TO_ONE,
        ZERO_TO_ONE,
        MIN_TO_MAX
    }

    fun initWithType(type: Type, initColor: InnerColor): Boolean {

        super.init()

        _type = type
        when (_type) {
            Type.MINUS_ONE_TO_ONE -> {
                _leftLine = SMSolidRectView.create(getDirector())
                _leftLine!!.setAnchorPoint(Vec2.LEFT_MIDDLE)
                _leftLine!!.setColor(initColor.bgLine)
                addChild(_leftLine)

                _rightLine = SMSolidRectView.create(getDirector())
                _rightLine!!.setAnchorPoint(Vec2.RIGHT_MIDDLE)
                _rightLine!!.setColor(initColor.bgLine)
                addChild(_rightLine)

                _circle = SMCircleView.create(getDirector())
                _circle!!.setContentSize(CENTER_RAIDUS*2, CENTER_RAIDUS*2)
                _circle!!.setLineWidth(4f)
                _circle!!.setAnchorPoint(Vec2.MIDDLE)
                _circle!!.setColor(initColor.fgLine)
                addChild(_circle)
            }
            Type.ZERO_TO_ONE -> {
                _rightLine = SMSolidRectView.create(getDirector())
                _rightLine!!.setAnchorPoint(Vec2.RIGHT_MIDDLE)
                _rightLine!!.setColor(initColor.bgLine)
                addChild(_rightLine)

                _circle = SMCircleView.create(getDirector())
                _circle!!.setContentSize(CENTER_RAIDUS*2, CENTER_RAIDUS*2)
                _circle!!.setLineWidth(4f)
                _circle!!.setAnchorPoint(Vec2.MIDDLE)
                _circle!!.setColor(initColor.fgLine)
                addChild(_circle)
            }
            Type.MIN_TO_MAX -> {
                _rightLine = SMSolidRectView.create(getDirector())
                _rightLine!!.setAnchorPoint(Vec2.RIGHT_MIDDLE)
                _rightLine!!.setColor(initColor.fgLine)
                addChild(_rightLine!!)
            }
        }

        _bgLine = SMSolidRectView.create(getDirector())
        _bgLine?.setColor(initColor.fgLine)
        addChild(_bgLine)

        if (_type==Type.MIN_TO_MAX) {
            _minButton = KnobButtonCreate(getDirector())
            _minButton?.setContentSize(120f, 120f)
            _minButton?.setAnchorPoint(Vec2.MIDDLE)
            _minButton?.setPadding(29f)
            _minButton?.setButtonColor(STATE.NORMAL, initColor.knobNormal)
            _minButton?.setButtonColor(STATE.PRESSED, initColor.knobPress)

            val minShadow = SMSolidCircleView.create(getDirector())
            _minButton?.setBackgroundView(minShadow)
            minShadow.setContentSize(75f, 75f)
            minShadow.setAnchorPoint(Vec2.MIDDLE)
            minShadow.setAntiAliasWidth(20f)
            minShadow.setPosition(60f, 58f)
            _minButton?.setBackgroundColor(0f, 0f, 0f, 0.2f)
            _minButton?.setOnTouchListener(this)
            addChild(_minButton)

            _maxButton = KnobButtonCreate(getDirector())
            _maxButton?.setContentSize(120f, 120f)
            _maxButton?.setAnchorPoint(Vec2.MIDDLE)
            _maxButton?.setPadding(29f)
            _maxButton?.setButtonColor(STATE.NORMAL, initColor.knobNormal)
            _maxButton?.setButtonColor(STATE.PRESSED, initColor.knobPress)

            val maxShadow = SMSolidCircleView.create(getDirector())
            _maxButton?.setBackgroundView(maxShadow)
            maxShadow.setContentSize(75f, 75f)
            maxShadow.setAnchorPoint(Vec2.MIDDLE)
            maxShadow.setAntiAliasWidth(20f)
            maxShadow.setPosition(60f, 58f)
            _maxButton?.setBackgroundColor(0f, 0f, 0f, 0.2f)
            _maxButton?.setOnTouchListener(this)
            addChild(_maxButton)

            setSliderValue(_minValue, _maxValue)
        } else {
            _knobButton = KnobButtonCreate(getDirector())
            _knobButton?.setContentSize(120f, 120f)
            _knobButton?.setAnchorPoint(Vec2.MIDDLE)
            _knobButton?.setPadding(29f)
            _knobButton?.setButtonColor(STATE.NORMAL, initColor.knobNormal)
            _knobButton?.setButtonColor(STATE.PRESSED, initColor.knobPress)

            val shadow = SMSolidCircleView.create(getDirector())
            _knobButton?.setBackgroundView(shadow)
            shadow.setContentSize(75f, 75f)
            shadow.setAnchorPoint(Vec2.MIDDLE)
            shadow.setAntiAliasWidth(20f)
            shadow.setPosition(60f, 58f)
            _knobButton?.setBackgroundColor(0f, 0f, 0f, 0.2f)
            _knobButton?.setOnTouchListener(this)
            addChild(_knobButton)

            setSliderValue(0f)
        }

        return true
    }

    override fun onTouch(view: SMView?, event: MotionEvent): Int {
        val action = event.action
        val point = Vec2(event.x, event.y)

        if (view==null) return TOUCH_FALSE

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (getActionByTag(AppConst.TAG.USER+2)==null) {
                    if (_type==Type.MIN_TO_MAX) {
                        if (view==_minButton) {
                            _minPoint.set(point)
                            _minFocused = true
                        } else {
                            _maxPoint.set(point)
                            _maxFocused = true
                        }
                    } else {
                        _knobPoint.set(point)
                        _knobFocused = true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (view==_knobButton) {
                    if (_knobFocused) {
                        var value:Float
                        if (_type==Type.MINUS_ONE_TO_ONE) {
                            val pt = Vec2(view.getPosition().minus(Vec2(_contentSize.width/2f, _contentSize.height/2f)).add(point).minus(_knobPoint))
                            var dist = pt.x

                            if (dist>_sliderWidth/2f) {
                                dist = _sliderWidth/2f
                            } else if (dist<-_sliderWidth/2f) {
                                dist = -_sliderWidth/2f
                            }
                            value = dist / (_sliderWidth/2f)
                        } else {
                            val pt = Vec2(view.getPosition().add(point).minus(_knobPoint))
                            var dist = pt.x - 50f
                            if (dist<0f) {
                                dist = 0f
                            } else if (dist>_sliderWidth) {
                                dist = _sliderWidth
                            }
                            value = dist / _sliderWidth
                        }
                        if (abs(value)<0.015f) {
                            value = 0f
                        }

                        setSliderValue(value, false)
                    }
                    return TOUCH_INTERCEPT
                } else if (view==_minButton) {
                    if (_minFocused) {
                        var value:Float
                        val pt = Vec2(view.getPosition().add(point).minus(_maxPoint))
                        var dist = pt.x - 50f
                        if (dist<0f) {
                            dist = 0f
                        } else if (dist>_maxButton!!.getPositionX()-110f) {
                            dist = _maxButton!!.getPositionX()-110f
                        }

                        value = dist / _sliderWidth

                        if (abs(value)<0.015f) {
                            value = 0f
                        }

                        setSliderValue(value, _maxValue, false)
                    }
                    return TOUCH_INTERCEPT
                } else if (view==_maxButton) {
                    if (_maxFocused) {
                        var value:Float
                        val pt = Vec2(view.getPosition().add(point).minus(_maxPoint))
                        var dist = pt.x - 50f

                        if (dist<_minButton!!.getPositionX()+10f) {
                            dist = _minButton!!.getPositionX()+10f
                        } else if (dist>_sliderWidth) {
                            dist = _sliderWidth
                        }

                        value = dist / _sliderWidth

                        setSliderValue(_minValue, value, false)
                    }
                    return TOUCH_INTERCEPT
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                _minFocused = false
                _maxFocused = false
                _knobFocused = false
            }
            else -> {
                // nothing to do.
            }
        }

        return TOUCH_FALSE
    }

    override fun dispatchTouchEvent(event: MotionEvent): Int {
        super.dispatchTouchEvent(event)
        return TOUCH_TRUE
    }

    override fun onUpdateOnVisit() {
        if (isUpdate(FLAG_CONTENT_SIZE)) {
            unregisterUpdate(FLAG_CONTENT_SIZE)
            updateLayout()
        }

        if (isUpdate(FLAG_SLIDE_VALUE)) {
            unregisterUpdate(FLAG_SLIDE_VALUE)

            when (_type) {
                Type.MINUS_ONE_TO_ONE -> {
                    val x = _knobButton!!.getPositionX() - _contentSize.width/2
                    if (x>0) {
                        var len = x - (CENTER_RAIDUS-1.5f)
                        if (len<0) {
                            len = 0f
                        }

                        _bgLine!!.setAnchorPoint(Vec2.LEFT_MIDDLE)
                        _bgLine!!.setContentSize(len, 2f)
                        _bgLine!!.setPosition(_contentSize.width/2f + (CENTER_RAIDUS - 1.5f), _contentSize.height/2f)
                    } else {
                        var len = -x - (CENTER_RAIDUS-1.5f)
                        if (len<0) {
                            len = 0f
                        }

                        _bgLine!!.setAnchorPoint(Vec2.RIGHT_MIDDLE)
                        _bgLine!!.setContentSize(len, 2f)
                        _bgLine!!.setPosition(_contentSize.width/2f - (CENTER_RAIDUS - 1.5f), _contentSize.height/2f)
                    }
                }
                Type.ZERO_TO_ONE -> {
                    val x = _knobButton!!.getPositionX() - 50f

                    var len = x - (CENTER_RAIDUS - 1.5f)
                    if (len<0) {
                        len = 0f
                    }

                    _bgLine!!.setAnchorPoint(Vec2.LEFT_MIDDLE)
                    _bgLine!!.setContentSize(len, 2f)
                    _bgLine!!.setPosition(50f + (CENTER_RAIDUS - 1.5f), _contentSize.height/2f)
                }
                Type.MIN_TO_MAX -> {
                    val x = _minButton!!.getPositionX() - 50f
                    var len = _maxButton!!.getPositionX() - x - 50f - (CENTER_RAIDUS - 1.5f) * 2f
                    if (len<0) {
                        len = 0f
                    }

                    _bgLine!!.setAnchorPoint(Vec2.LEFT_MIDDLE)
                    _bgLine!!.setContentSize(len, 2f)
                    _bgLine!!.setPosition(_minButton!!.getPositionX(), _contentSize.height/2f)
                }
            }
        }
    }

    fun updateLayout() {
        _sliderWidth = _contentSize.width - 100f
        when (_type) {
            Type.MINUS_ONE_TO_ONE -> {
                _leftLine!!.setContentSize(_sliderWidth/2f - (CENTER_RAIDUS-1.5f), 2f)
                _leftLine!!.setPosition(50f, _contentSize.height/2f)

                _rightLine!!.setContentSize(_sliderWidth/2f-(CENTER_RAIDUS-1.5f), 2f)
                _rightLine!!.setPosition(_contentSize.width-50f, _contentSize.height/2f)

                _circle!!.setPosition(_contentSize.width/2f, _contentSize.height/2f)
            }
            Type.ZERO_TO_ONE -> {
                _rightLine!!.setContentSize(_sliderWidth-(CENTER_RAIDUS-1.5f), 2f)
                _rightLine!!.setPosition(_contentSize.width-50f, _contentSize.height/2f)

                _circle!!.setPosition(50f, _contentSize.height/2f)
            }
            Type.MIN_TO_MAX -> {
                _rightLine!!.setContentSize(_sliderWidth, 2f)
                _rightLine!!.setPosition(_contentSize.width-50f, _contentSize.height/2f)
            }
        }

        if (_type==Type.MIN_TO_MAX) {
            setKnobPosition(_minValue, _maxValue, true)
        } else {
            setKnobPosition(_sliderValue, true)
        }
    }

    override fun setContentSize(size: Size) {
        super.setContentSize(size)
        registerUpdate(FLAG_CONTENT_SIZE)
    }

    override fun setContentSize(width: Float?, height: Float?) {
        super.setContentSize(width, height)
        registerUpdate(FLAG_CONTENT_SIZE)
    }

    fun setSliderValue(sliderValue: Float) {
        setSliderValue(sliderValue, true)
    }

    fun setSliderValue(sliderValue: Float, immediate: Boolean) {
        if (BuildConfig.DEBUG && _type == Type.MIN_TO_MAX) {
            error("Assertion failed")
        }

        if (_type == Type.MINUS_ONE_TO_ONE) {
            _sliderValue = sliderValue.coerceAtLeast(-1.0f).coerceAtMost(1.0f)
        } else {
            _sliderValue = sliderValue.coerceAtLeast(0.0f).coerceAtMost(1.0f)
        }

        _listener?.onSliderValueChanged(this, _sliderValue)

        setKnobPosition(_sliderValue, immediate)
    }

    fun setSliderValue(minValue: Float, maxValue: Float) {
        setSliderValue(minValue, maxValue, true)
    }

    fun setSliderValue(minValue: Float, maxValue: Float, immediate: Boolean) {
        if (BuildConfig.DEBUG && _type!=Type.MIN_TO_MAX) {
            error("Assertion failed")
        }

        _minValue = minValue.coerceAtLeast(0.0f).coerceAtMost(1.0f)
        _maxValue = maxValue.coerceAtLeast(0.0f).coerceAtMost(1.0f)

        _listener?.onSliderValueChanged(this, _minValue, _maxValue)

        setKnobPosition(_minValue, _maxValue, immediate)
    }

    fun setKnobPosition(sliderValue: Float) {
        setKnobPosition(sliderValue, true)
    }

    fun setKnobPosition(sliderValue: Float, immediate: Boolean) {
        if (BuildConfig.DEBUG && _type==Type.MIN_TO_MAX) {
            error("Assertion failed")
        }

        var x: Float
        if (_type==Type.MINUS_ONE_TO_ONE) {
            x = sliderValue * _sliderWidth/2f
            _knobButton?.setPosition(_contentSize.width/2f+x, _contentSize.height/2f, if (sliderValue==0f){true}else{immediate})
        } else {
            x = sliderValue * _sliderWidth
            _knobButton?.setPosition(50f+x, _contentSize.height/2f, if (sliderValue==0f){true}else{immediate})
        }

        registerUpdate(FLAG_SLIDE_VALUE)
    }

    fun setKnobPosition(minValue: Float, maxValue: Float) {
        setKnobPosition(minValue, maxValue, true)
    }

    fun setKnobPosition(minValue: Float, maxValue: Float, immediate: Boolean) {
        if (BuildConfig.DEBUG && _type!=Type.MIN_TO_MAX) {
            error("Assertion")
        }

        val minX = minValue * _sliderWidth
        val maxX = maxValue  * _sliderWidth

        _minButton!!.setPosition(50f+minX, _contentSize.height/2f, if (minValue==0f){true}else{immediate})
        _maxButton!!.setPosition(50f+maxX, _contentSize.height/2f, if (maxValue==0f){true}else{immediate})

        registerUpdate(FLAG_SLIDE_VALUE)
    }

    fun getSliderValue(): Float {return _sliderValue}
    fun getMinValue(): Float {return _minValue}
    fun getMaxValue(): Float {return _maxValue}

    fun KnobButtonCreate(director: IDirector): KnobButton {
        val knob:KnobButton = KnobButton(director)
        knob.initWithStyle(SMButton.STYLE.SOLID_CIRCLE)
        return knob
    }

    class KnobButton(director: IDirector): SMButton(director) {
        override fun onSmoothUpdate(flags: Long, dt: Float) {
            if (flags.and(VIEWFLAG_POSITION)>0) {
                if (getParent() is SMSlider) {
                    (getParent() as SMSlider).updateKnob()
                }
            }
        }
    }

    fun updateKnob() {registerUpdate(FLAG_SLIDE_VALUE)}

    interface OnSliderListener {
        fun onSliderValueChanged(slider:SMSlider, value: Float)
        fun onSliderValueChanged(slider: SMSlider, minValue: Float, maxValue: Float)
    }
    private var _listener:OnSliderListener? = null
    fun setOnSliderListener(l: OnSliderListener) {_listener = l}
}