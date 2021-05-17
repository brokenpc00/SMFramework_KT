package com.interpark.smframework.view.Sticker

import android.util.Log
import android.view.MotionEvent
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.shape.ShapeConstant
import com.brokenpc.smframework.base.sprite.GridSprite
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.TransformAction
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.view.SMButton
import com.brokenpc.smframework.view.SMRoundRectView
import com.brokenpc.smframework.view.SMSolidCircleView
import com.interpark.smframework.view.RingWave
import com.interpark.smframework.view.RingWave2
import kotlin.math.*

class StickerControlView(director: IDirector): SMView(director), SMView.OnClickListener, SMView.OnTouchListener {
    private lateinit var _uiView: SMView
    private lateinit var _borderRect: SMRoundRectView
    private lateinit var _sizeButton: SMButton
    private lateinit var _utilButton: SMButton

    private var _targetView: SMView? = null
    private val _targetSize = Size(Size.ZERO)
    private val _grabPt = Vec2(Vec2.ZERO)
    private var _reset = false

    private var _listener: StickerControlListener? = null
    private var _utilButtonMode = -1
    private var _sizeButtonIndicator: SMView? = null
//    private var _highlightSizeButton = false
//
//    fun highlightSizeButton() {_highlightSizeButton = true}

    companion object {
        private const val SIZE_BTN_TAG = 100
        private const val BORDER_MARGIN = 45.0f
        private const val UTILBUTTON_ID_DELETE = 2000
        private val WAVECOLOR = Color4F(1f, 1f, 1f, 0.5f)

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
//        _uiView.setBackgroundColor(Color4F(0f, 1f, 0f, 0.4f))
        addChild(_uiView)

        // border line
        _borderRect = SMRoundRectView.create(getDirector(), 4.0f, ShapeConstant.LineType.SOLID, 2.0f)
        _borderRect.setAnchorPoint(Vec2.MIDDLE)
        _borderRect.setCornerRadius(20f)
        _borderRect.setLineColor(MakeColor4F(0xe6e6e9, 1f))
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
        if (view==null) return TOUCH_FALSE

        val action = event.action
        val point = Vec2(event.x, event.y)
        if (action==MotionEvent.ACTION_DOWN) {
            val size = view.getContentSize()
            RingWave.show(getDirector(), view, size.width/2f, size.height/2f, 200f, 0.25f, 0.0f, WAVECOLOR)
        }

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                _grabPt.set(point)
                return TOUCH_FALSE
            }
            MotionEvent.ACTION_MOVE -> {
                // _targetView : sticker
                // this : control
                // view : size & rotate button
                // _uiView : view's parent

                val touchPoint = Vec2(view.getPosition().minus(BORDER_MARGIN).add(point).minus(_grabPt))
                val ppt = _uiView.convertToWorldSpace(touchPoint).minus(_uiView.convertToWorldPos(Vec2.ZERO))
                val rot = atan2(ppt.y, ppt.x)
                val tsize = _targetView!!.getContentSize()
                val ww = tsize.width
                val hh = tsize.height
                var baseDist = sqrt(ww*ww + hh*hh)
                val baseRot = atan2(-hh, ww)
                val newRot = get360Degree(rot-baseRot)
//                _sizeButton.setPosition(touchPoint)
                Log.i("STICKER", "[[[[[ rotate : $newRot")
                _targetView!!.setRotation(newRot)

                val dist = touchPoint.length()
                var canvasScale = 1f
                var p = _targetView!!.getParent()
                while (p!=null) {
                    canvasScale *= p.getScale()
                    p = p.getParent()
                }

                var controlScale = 1f
                p = getParent()
                while (p!=null) {
                    controlScale *= p.getScale()
                    p = p.getParent()
                }

                baseDist *= canvasScale / controlScale

                var scale = dist / baseDist
                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
                    scale = ((1+ BORDER_MARGIN) / tsize.width).coerceAtLeast((1+ BORDER_MARGIN) / tsize.height)
                }

                _targetView!!.setScale(scale)
//                val O = _uiView.getPosition()
//                val curTouchPoint = view.getPosition().minus(BORDER_MARGIN).add(point).minus(_grabPt).divide(2f)
////                val A = _uiView.convertToWorldSpace(curTouchPoint)
//                val cornerPoint = Vec2(0f, _uiView.getContentSize().height)
//                val ppt = _uiView.convertToWorldPos(curTouchPoint).minus(_uiView.convertToWorldPos(cornerPoint))
//
//                val dist = curTouchPoint.length()
//
//                val rot = atan2(ppt.y, ppt.x)
//
//                val tsize = _targetView!!.getContentSize()
//                val ww = tsize.width/2f
//                val hh = tsize.height/2f
//                val baseRot = atan2(-hh, ww)
//
//                val rotate = toDegrees(rot-baseRot)
//
//                Log.i("STICKER", "[[[[[ rotate : $rotate")
//                _targetView!!.setRotation(rotate)

//                (cos theta = (a^2+b^2-c^2)/(2ab)
//                var oldRotate = _targetView!!.getRotation()
//                if (oldRotate>360f) oldRotate = 0f
//                oldRotate += 1f
//                Log.i("STICKER", "[[[[[ rotate : $oldRotate")
//                _targetView!!.setRotation(oldRotate)
                return TOUCH_TRUE
////                val pt = view.getPosition().minus(Vec2(BORDER_MARGIN, BORDER_MARGIN)).add(point).minus(_grabPt)
//                val diff = point.minus(_grabPt)
//                val pt = view.getPosition().add(point).minus(_grabPt).minus(Vec2(BORDER_MARGIN/2f, BORDER_MARGIN/2f)).multiply(0.5f)
//                val dist = pt.length()
//
////                val ppt = _uiView.convertToWorldSpace(pt).minus(_uiView.convertToWorldSpace(_utilButton.getPosition()))
//                val ppt = _uiView.convertToWorldSpace(pt).minus(_uiView.convertToWorldSpace(_utilButton.getPosition()))
//                val rot = atan2(ppt.y, ppt.x)
////                val dx = (view.getPosition().x-point.x-BORDER_MARGIN-_grabPt.x)/2f
////                val dy = (view.getPosition().y-point.y- BORDER_MARGIN-_grabPt.y)/2f
////                val rot = atan2(dy, dx)
//
//                val tsize = _targetView!!.getContentSize()
//                val ww = tsize.width/2f
//                val hh = tsize.height/2f
//                var baseDist = sqrt(ww*ww + hh*hh)
//                var baseRot = atan2(-hh, ww)
//
//                var canvasScale = 1f
//                var p = _targetView!!.getParent()
//                while (p!=null) {
//                    canvasScale *= p.getScale()
//                    p = p.getParent()
//                }
//
//                var controlScale = 1f
//                p = getParent()
//                while (p!=null) {
//                    controlScale *= p.getScale()
//                    p = p.getParent()
//                }
//
//                baseDist *= canvasScale / controlScale
//
//                var scale = dist / baseDist
//                if (scale * tsize.width <= BORDER_MARGIN || scale * tsize.height <= BORDER_MARGIN) {
//                    scale = ((1+ BORDER_MARGIN) / tsize.width).coerceAtLeast((1+ BORDER_MARGIN) / tsize.height)
//                }
//
//                _targetView!!.setScale(scale)
//                var rotate = toDegrees(rot-baseRot)
//                rotate = getShortestAngle(rotate)
//                val newRotate = -get360Degree(rot-baseRot)
////                val newRot = -toDegrees(rot-baseRot)
////                val rotate = get360Degree(newRot)
//                Log.i("STICKER", "[[[[[ rot : $rot, baseRot : $baseRot, rotate : $rotate, newRotate  $newRotate")
////                _targetView!!.setRotation(rotate)
//                return TOUCH_TRUE
            }
        }

        return TOUCH_FALSE
    }

    fun getShortestAngle(degrees: Float): Float {
        return (((degrees%360)+540)%360)-180
    }

    fun get360Degree(degrees: Float): Float {
        return ((if (degrees >= 0) degrees else {(2*PI + degrees)}).toFloat() * 180/PI).toFloat()
    }

    fun linkStickerView(view: SMView?) {
        if (_targetView!=view) {
            _targetView = view

            if (view!=null) {
                val viewSize = Size(view!!.getContentSize())
                _sizeButton.setPosition(viewSize.width, 0f)
                _utilButton.setPosition(0f, viewSize.height)
                val targetPosition = Vec2(_targetView!!.getParent()!!.convertToWorldSpace(_targetView!!.getPosition()))
                val position = convertToNodeSpace(targetPosition)
                _uiView.setPosition(position)
            }

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

//                    if (_highlightSizeButton) {
//                        _highlightSizeButton = false
//
//                        val ringWave = RingWave2.create(getDirector(), 90f, 153f)
//                        ringWave.setColor(MakeColor4F(0xff9a96, 1f))
//                        _sizeButton.addChild(ringWave)
//                        _sizeButtonIndicator = ringWave
//                    }
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

        var localScale = getScale()
        var localRotation = getRotation()
        if (localRotation!=0f) {
            Log.i("STICKER", "[[[[[ rotate : $localRotation")
        }
        var p = getParent()
        while (p!=null) {
            localScale *= p.getScale()
            localRotation += p.getRotation()

            p = p.getParent()
        }
        if (localRotation!=0f) {
            Log.i("STICKER", "[[[[[ rotate : $localRotation")
        }

        val targetPosition = Vec2(_targetView!!.getParent()!!.convertToWorldSpace(_targetView!!.getPosition()))
        var targetScale = _targetView!!.getScale()
        var targetRotation = _targetView!!.getRotation()
        if (targetRotation!=0f) {
            Log.i("STICKER", "[[[[[ rotate : $localRotation")
        }

        p = _targetView!!.getParent()
        while (p!=null) {
            targetScale *= p.getScale()
            targetRotation += p.getRotation()
            p = p.getParent()
        }

        val scale = targetScale / localScale
        val rotation = targetRotation - localRotation
        val position = convertToNodeSpace(targetPosition)

        val size = _targetView!!.getContentSize().multiply(scale)

        if (_reset || size.width!=_targetSize.width || size.height != _targetSize.height) {
            _reset = false
            _targetSize.set(size)

            val viewSize = size.add(Size(BORDER_MARGIN, BORDER_MARGIN))
            _uiView.setContentSize(viewSize)
            _borderRect.setContentSize(viewSize)
            _borderRect.setPosition(viewSize.divide(2f))
//            _sizeButton.setPosition(viewSize)
//            _utilButton.setPosition(Vec2.ZERO)
            _sizeButton.setPosition(viewSize.width, 0f)
            _utilButton.setPosition(0f, viewSize.height)
        }

        _uiView.setPosition(position)
//        tttt++
//        if (tttt>360) tttt = 0f
//        _uiView.setRotation(tttt)
//        _uiView.setRotation(rotation)
//        _utilButton.setRotation(-rotation)
    }

    var tttt = 0f
}