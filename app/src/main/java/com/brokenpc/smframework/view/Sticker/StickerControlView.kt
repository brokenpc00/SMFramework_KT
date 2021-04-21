package com.interpark.smframework.view.Sticker

import android.view.MotionEvent
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.shape.ShapeConstant
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.TransformAction
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.view.SMButton
import com.brokenpc.smframework.view.SMRoundRectView
import com.brokenpc.smframework.view.SMSolidCircleView
import com.interpark.smframework.view.RingWave
import com.interpark.smframework.view.RingWave2

class StickerControlView(director: IDirector): SMView(director), SMView.OnClickListener, SMView.OnTouchListener {
    private lateinit var _uiView: SMView
    private lateinit var _borderRect: SMRoundRectView
    private lateinit var _sizeButton: SMButton
    private lateinit var _utilButton: SMButton

    private var _targetView: SMView? = null
    private val _targetSize = Size(Size.ZERO)
    private val _grabPt = Vec2(Vec2.ZERO)
    private val _deltaPt = Vec2(Vec2.ZERO)
    private var _reset = false

    private var _listener: StickerControlListener? = null
    private var _utilButtonMode = 0
    private var _sizeButtonIndicator: SMView? = null
    private var _highlightSizeButton = false

    companion object {
        private const val SIZE_BTN_TAG = 100
        private const val BORDER_MARGIN = 45.0f
        private const val UTILBUTTON_ID_DELETE = 2000

        private val MENU_BUTTON_A = MakeColor4F(0xffffff, .7f)
        private val MENU_BUTTON_B = MakeColor4F(0x222222, .7f)
        private val MENU_OUTLINE_A = MakeColor4F(0x222222, .7f)
        private val MENU_OUTLINE_B = MakeColor4F(0xffffff, .7f)

        private val WAVE_COLOR = MakeColor4F(0xffffff, .5f)

        private const val UTIL_BUTTON_MODE_NONE = -1
        private const val UTIL_BUTTON_MODE_REMOVE = 1

        @JvmStatic
        fun create(director: IDirector): StickerControlView {
            val view = StickerControlView(director)
            view.init()
            return view
        }
    }

    override fun init(): Boolean {
        if (!super.init()) {
            return false
        }

        _uiView = create(getDirector(), 0, 0f, 0f, 15f, 15f)
        _uiView.setAnchorPoint(Vec2.MIDDLE)
        _uiView.setIgnoreTouchBounds(true)
        // for test color
        _uiView.setBackgroundColor(Color4F(0f, 1f, 0f, 0.4f))
        addChild(_uiView)

        // border line
        _borderRect = SMRoundRectView.create(getDirector(), 6.0f, ShapeConstant.LineType.DASH, 3f)
        _borderRect.setAnchorPoint(Vec2.MIDDLE)
        _borderRect.setCornerRadius(30f)
        _borderRect.setLineColor(MakeColor4F(0xe6e6e6, 1f))
        _uiView.addChild(_borderRect)

        // size button
        _sizeButton = SMButton.create(getDirector(), SIZE_BTN_TAG, SMButton.STYLE.SOLID_CIRCLE, 0f, 0f, 210f, 210f, 0.5f, 0.5f)
        _sizeButton.setPadding(45.0f)
        _sizeButton.setButtonColor(STATE.NORMAL, Color4F.WHITE)
        _sizeButton.setButtonColor(STATE.PRESSED, Color4F(0.9f, 0.9f, 0.9f, 1.0f))
        _sizeButton.setOutlineWidth(7.5f)
        _sizeButton.setOutlineColor(STATE.NORMAL, MakeColor4F(0xe6e6e9, 1.0f))
        _sizeButton.setIcon(STATE.NORMAL, "images/size_arrow.png")
        _sizeButton.setIconColor(STATE.NORMAL, MakeColor4F(0x222222, 1.0f))

        val shadow = SMSolidCircleView.create(getDirector())
        _sizeButton.setBackgroundView(shadow)
        shadow.setContentSize(120f, 120f)
        shadow.setAnchorPoint(Vec2.MIDDLE)
        shadow.setAntiAliasWidth(30f)
        shadow.setPosition(135f, 105f)
        _sizeButton.setBackgroundColor(Color4F(0f, 0f, 0f, 0.15f))
        _sizeButton.setOnTouchListener(this)
        _uiView.addChild(_sizeButton)

        // trash button
        _utilButton = SMButton.create(getDirector(), 0, SMButton.STYLE.SOLID_CIRCLE, 0f, 0f, 210f, 210f, 0.5f, 0.5f)
        _utilButton.setPadding(45f)
        val shadow2 = SMSolidCircleView.create(getDirector())
        _utilButton.setBackgroundView(shadow2)
        shadow2.setContentSize(120f, 120f)
        shadow2.setAnchorPoint(Vec2.MIDDLE)
        shadow2.setAntiAliasWidth(30f)
        shadow2.setPosition(135f, 105f)
        _utilButton.setBackgroundColor(Color4F(0f, 0f, 0f, 0.15f))
        _utilButton.setOnClickListener(this)
        _uiView.addChild(_utilButton)

        _uiView.setVisible(false)

        _utilButtonMode = UTIL_BUTTON_MODE_NONE

        return true
    }

    interface StickerControlListener {
        fun onStickerMenuClick(sticker: SMView?, menuId: Int)
    }

    fun setStickerControlListener(l: StickerControlListener) {_listener = l}

    override fun onClick(view: SMView?) {
        _listener?.onStickerMenuClick(_targetView, view?.getTag()?:-1)
    }

    fun startGeineRemove(view: SMView?) {
        if (view!=null && view==_targetView) {
            val dst = convertToNodeSpace(_targetView!!.convertToWorldSpace(Vec2.ZERO))
            val size = _utilButton.getContentSize()
            val src = convertToNodeSpace(_utilButton.convertToWorldSpace(Vec2(size.width/2f, size.height/2f)))
            WasteBasketActionView.showForUtil(getDirector(), this, src, dst)

//            if (view is Sticker) {
//                val sticker = view as Sticker
//                if (sticker.getSprite() is GridSprite) {
//                    val sprite = sticker.getSprite() as GridSprite
//
//                }
//            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?, view: SMView, checkBounds: Boolean): Int {
        val ret = super.dispatchTouchEvent(event, view, checkBounds)
        if (checkBounds && event!!.action == MotionEvent.ACTION_DOWN && view==_uiView) {
            if (_sizeButtonIndicator!=null) {
                val action = TransformAction.create(getDirector())
                action.toAlpha(0f).removeOnFinish()
                action.setTimeValue(0.5f, 0.0f)
                _sizeButtonIndicator!!.runAction(action)
                _sizeButtonIndicator = null
            }
        }

        return ret
    }

    override fun onTouch(view: SMView?, event: MotionEvent): Int {
        val action = event.action
        val point = Vec2(event.x, event.y)

        if (view==null) return TOUCH_TRUE

        if (action==MotionEvent.ACTION_DOWN) {
            val size = view!!.getContentSize()
            RingWave.show(getDirector(), view!!, size.width/2f, size.height/2f, 200f, 0.25f, 0.0f, WAVE_COLOR)
        }


        when (action) {
            MotionEvent.ACTION_DOWN -> {
                _grabPt.set(point)
                return TOUCH_FALSE
            }
            MotionEvent.ACTION_MOVE -> {
                val pt = (view.getPosition().minus(Vec2(BORDER_MARGIN, BORDER_MARGIN).add(point).minus(_grabPt))).multiply(0.5f)
                val dist = pt.length()

                // ToDo.... Sticker Moving Action.
                return TOUCH_TRUE
            }
        }

        return TOUCH_FALSE
    }

    fun linkStickerView(view: SMView?) {
        if (_targetView!=view) {
            _targetView = view
            if (view!=null) {
                _uiView.setVisible(true)

                var type = Sticker.ControlType.NONE
                if (view is Sticker) {
                    val sticker = view as Sticker
                    type = sticker.getControlType()
                    if (type==Sticker.ControlType.DELETE) {
                        if (sticker.isRemovable()) {
                            _utilButton.setVisible(true)
                            if (_utilButtonMode!= UTIL_BUTTON_MODE_REMOVE) {
                                _utilButtonMode = UTIL_BUTTON_MODE_REMOVE
                                _utilButton.setButtonColor(STATE.NORMAL, MakeColor4F(0xff683a, 1f))
                                _utilButton.setButtonColor(STATE.PRESSED, MakeColor4F(0xff683a, 1f))
                                _utilButton.setIcon(STATE.NORMAL, "images/delete_full.png")
                                _utilButton.setIconColor(STATE.NORMAL, Color4F.WHITE)
                                _utilButton.setIconColor(STATE.PRESSED, Color4F.WHITE)
                                _utilButton.setTag(UTILBUTTON_ID_DELETE)
                            }
                        } else {
                            _utilButton.setVisible(false)
                        }
                    }

                    _reset = true

                    registerUpdate(USER_VIEW_FLAG(1L))

                    if (_highlightSizeButton) {
                        _highlightSizeButton = false

                        val ringWave = RingWave2.create(getDirector(), 90f, 153f)
                        ringWave.setColor(MakeColor4F(0xff9a96, 1f))
                        _sizeButton.addChild(ringWave)
                        _sizeButtonIndicator = ringWave
                    }
                }
            } else {
                _uiView.setVisible(false)
                _utilButton.setVisible(false)

                if (_sizeButtonIndicator!=null) {
                    _sizeButton.removeChild(_sizeButtonIndicator)
                    _sizeButtonIndicator = null
                }
            }
        }
    }

    override fun onUpdateOnVisit() {
        if (_targetView==null) return

        val localScale = getScreenScale()
        val localRotation = getScreenAngle()

        val targetPosition = _targetView!!.convertToWorldSpace(Vec2.ZERO)
        val position = _uiView.convertToLocalPos(targetPosition)

        val targetScale = _targetView!!.getScreenScale()
        val targetRotation = _targetView!!.getScreenAngle()

        val scale = targetScale / localScale
        val rotation = targetRotation / localRotation

        val size = _targetView!!.getContentSize().multiply(scale)

        if (_reset || size.width!=_targetSize.width || size.height != _targetSize.height) {
            _reset = false
            _targetSize.set(size)

            val viewSize = size.add(Size(BORDER_MARGIN, BORDER_MARGIN))
            _uiView.setContentSize(viewSize)
            _borderRect.setContentSize(viewSize)
            _borderRect.setPosition(viewSize.width/2f, viewSize.height/2f)
            _sizeButton.setPosition(viewSize.width, viewSize.height)
            _utilButton.setPosition(0f, 0f)
        }

        _uiView.setPosition(position)
        _uiView.setRotation(rotation)
    }
}