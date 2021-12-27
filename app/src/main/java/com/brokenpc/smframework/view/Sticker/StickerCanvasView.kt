package com.brokenpc.smframework.view.Sticker

import android.os.Build
import android.view.MotionEvent
import android.view.VelocityTracker
import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.base.types.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class StickerCanvasView(director: IDirector): SMView(director), MultiTouchController.Companion.MultiTouchObjectCanvas<SMView> {

    private lateinit var _controller: MultiTouchController<SMView>
    private var _selectedView: SMView? = null
    private lateinit var _velocityTracker: VelocityTracker

    private var _trackFlyEvent = false
    private var _flyRemovable = true

    private var _lastTouchPoint:MultiTouchController.Companion.PointInfo = MultiTouchController.Companion.PointInfo()
    private var _listener:StickerCanvasListener? = null


    companion object {

        const val MIN_FLY_TOLERANC = 6000f
        const val FLY_DURATION= 0.3f


        fun create(director: IDirector): StickerCanvasView {
            val view = StickerCanvasView(director)
            view.init()
            return view
        }
    }

    override fun init(): Boolean {

        if (!super.init()) {
            return false
        }

        _controller = MultiTouchController(this)
        _velocityTracker = VelocityTracker.obtain()

        return true
    }

    fun setRemoveAfterFlying(enable: Boolean) {_flyRemovable = enable}

    fun setChildPosition(view: SMView, position: Int) {
        val children = getChildren()

        if (BuildConfig.DEBUG && !children.contains(view)) {
            error("Assertion Failed - addChild First")
        }

        var zorder = 0

        val iter = children.iterator()
        while (iter.hasNext()) {
            val child = iter.next()
            if (child==_bgView) continue

            if (child==view) {
                view.setLocalZOrder(position)
                continue
            }

            if (zorder==-position) {
                zorder--
            }

            child.setLocalZOrder(zorder--)
        }

        sortAllChildren()
    }

    fun bringChildToTop(view: SMView) {
        val children = getChildren()

        if (BuildConfig.DEBUG && !children.contains(view)) {
            error("Assertion Failed")
        }

        var zorder = -1

        val iter = children.listIterator()
        while (iter.hasNext()) {
            val child = iter.next()

            if (child==_bgView || child==view) continue

            child.setLocalZOrder(zorder--)
        }

        view.setLocalZOrder(0)
        sortAllChildren()
    }

    fun sendChildToBack(view: SMView) {
        val children = getChildren()

        if (BuildConfig.DEBUG && !children.contains(view)) {
            error("Assertion Failed")
        }

        var zorder = 0
        val iter = children.iterator()
        while (iter.hasNext()) {
            val child = iter.next()

            if (child==_bgView || child==view) continue

            child.setLocalZOrder(zorder--)
        }

        view.setLocalZOrder(zorder)
        sortAllChildren()
    }

    fun aboveView(view: SMView, aboveView: SMView?) {
        if (aboveView==null) {
            bringChildToTop(view)
            return
        }

        val children = getChildren()

        if (BuildConfig.DEBUG && !children.contains(view)) {
            error("Assertion Failed")
        }

        if (!children.contains(aboveView) || view==aboveView) {
            return
        }

        var zorder = 0
        var target = 0

        val iter = children.iterator()
        while (iter.hasNext()) {
            val child = iter.next()

            if (child==_bgView || child==view) continue

            if (child==aboveView) {
                target = zorder
                zorder--
            }

            child.setLocalZOrder(zorder--)
        }

        view.setLocalZOrder(target)
        sortAllChildren()
    }

    fun belowView(view: SMView, belowView: SMView?) {
        if (belowView==null) {
            sendChildToBack(view)
            return
        }

        val children = getChildren()

        if (BuildConfig.DEBUG && !children.contains(view)) {
            error("Assertion Failed")
        }

        if (!children.contains(belowView) || view==belowView) {
            return
        }

        var zorder = 0
        var target = 0

        val iter = children.iterator()
        while (iter.hasNext()) {
            val child = iter.next()

            if (child==_bgView || child==view) continue

            child.setLocalZOrder(zorder--)

            if (child==belowView) {
                target = zorder
                zorder--
            }
        }

        view.setLocalZOrder(target)
        sortAllChildren()
    }

    override fun addChild(child: SMView?, zOrder: Int, name: String) {
        super.addChild(child, zOrder, name)
        setSelectedSticker(null)
    }

    override fun removeChild(child: SMView?, cleanup: Boolean) {
        if (child!=null && child==_selectedView) {
            performSelected(_selectedView!!, false)
            _selectedView = null
        }

        super.removeChild(child, cleanup)
    }

    fun setSelectedSticker(view: SMView?): Boolean {
        if (view==null) {
            if (_selectedView!=null) {
                performSelected(_selectedView!!, false)
                _selectedView = null
            }
            return true
        } else if (view!=_selectedView) {
            val children = getChildren()
            if (children.contains(view)) {
                if (_selectedView!=null) {
                    performSelected(_selectedView!!, false)
                }
                _selectedView = view
                performSelected(view!!, true)
                return true
            }
        }

        return false
    }

    fun getSelectedSticker(): SMView? {return _selectedView}

    override fun dispatchTouchEvent(event: MotionEvent): Int {
        val ret = super.dispatchTouchEvent(event)
        val action = event.action
        val mode = _controller.getMode()

        if (action==MotionEvent.ACTION_UP) {
            _listener?.onStickerTouch(_selectedView, MotionEvent.ACTION_UP)
        }

        if (_controller.onTouchEvent(event)) {
            if (mode==MultiTouchController.MODE_NOTHING &&  action==MotionEvent.ACTION_DOWN) {
                _listener?.onStickerTouch(_selectedView, MotionEvent.ACTION_DOWN)
            }

            _velocityTracker.addMovement(event)

            if (mode==MultiTouchController.MODE_DRAG && action==MotionEvent.ACTION_MOVE) {
                if (!_trackFlyEvent) {
                    if (_selectedView is RemovableSticker) {
                        val removable = _selectedView as RemovableSticker
                        if (removable.isRemovable()) {
                            val point = _controller.getCurrentPoint()
                            val dist = point.getDistance(_lastTouchPoint)
                            if (dist>AppConst.Config.SCROLL_TOLERANCE) {
                                _trackFlyEvent = true
                            }
                        }
                    } else {
                        val point = _controller.getCurrentPoint()
                        val dist = point.getDistance(_lastTouchPoint)
                        if (dist>AppConst.Config.SCROLL_TOLERANCE) {
                            _trackFlyEvent = true
                        }
                    }
                }
            }
            return TOUCH_INTERCEPT
        } else if (mode!=MultiTouchController.MODE_NOTHING && action==MotionEvent.ACTION_UP) {
            if (mode==MultiTouchController.MODE_DRAG) {
                if (_trackFlyEvent) {
                    _trackFlyEvent = false

                    val vx = _velocityTracker.getXVelocity(0)
                    val vy = _velocityTracker.getYVelocity(0)

                    val radian = atan2(vy, vx)
                    var degrees = toDegrees(radian)
                    val speed = sqrt(vx*vx + vy*vy)

                    degrees = (degrees+360.0f) % 360.0f

                    if (speed> MIN_FLY_TOLERANC) {
                        val wspeed = convertToNodeSpace(Vec2(speed, 0))
                        performFly(_selectedView, degrees, wspeed.x)
                    }
                }
                _velocityTracker.clear()
            }
            return TOUCH_TRUE
        }
        return ret
    }

    override fun getDraggableObjectAtPoint(touchPoint: MultiTouchController.Companion.PointInfo): SMView? {
        val worldPoint = convertToWorldSpace(touchPoint.getPoint())

        val children = getChildren()
        val iter = children.iterator()
        while (iter.hasNext()) {
            val child = iter.next()
            if (child==_bgView) continue

            val nodePoint = child.convertToNodeSpace(worldPoint)

            val size = child.getContentSize()
            if (!(nodePoint.x<0 || nodePoint.y<0 || nodePoint.x>size.width-1 || nodePoint.y>size.height-1)) {
                if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)==null) {
                    return child
                }
            }
        }

        if (_selectedView!=null) {
            performSelected(_selectedView, false)
            _selectedView = null
        }

        return null
    }

    override fun getPositionAndScale(view: SMView?, objPosAndScaleOut: MultiTouchController.Companion.PositionAndScale) {
        if (view==null) return
        val pt = view.getPosition() ?: Vec2(Vec2.ZERO)
        objPosAndScaleOut.set(pt.x, pt.y, true, view.getScale(), false, 1f, 1f, true, toRadians(-view!!.getRotation()))
    }

    override fun setPositionAndScale(view: SMView?, newObjPosAndScale: MultiTouchController.Companion.PositionAndScale, touchPoint: MultiTouchController.Companion.PointInfo): Boolean {

        return if (view==null) false else  {
            view.setPosition(newObjPosAndScale.getXOff(), newObjPosAndScale.getYOff(), false)
            view.setScale(newObjPosAndScale.getScale(), false)
            view.setRotation((-toDegrees(newObjPosAndScale.getAngle().toDouble())).toFloat(), false)

            return true
        }
    }

    override fun selectObject(view: SMView?, touchPoint: MultiTouchController.Companion.PointInfo) {
        if (view==null) return

        bringChildToTop(view)

        if (_selectedView!=view) {
            if (_selectedView!=null) {
                performSelected(_selectedView, false)
            }

            _selectedView = view
            performSelected(view, true)

            _velocityTracker.clear()
            _lastTouchPoint.set(touchPoint)
        }
    }

    override fun doubleClickObject(
        view: SMView?,
        touchPoint: MultiTouchController.Companion.PointInfo
    ) {
        val point = touchPoint.getPoint()
        _listener?.onStickerDoubleClicked(view, convertToWorldSpace(point))
    }

    override fun touchModeChanged(
        touchMode: Int,
        touchPoint: MultiTouchController.Companion.PointInfo
    ) {
        if (touchMode==MultiTouchController.MODE_DRAG) {
            _velocityTracker.clear()
            _lastTouchPoint.set(touchPoint)
            _trackFlyEvent = false
        }
    }

    override fun toWorldPoint(canvasPoint: Vec2): Vec2 {
        return convertToWorldSpace(canvasPoint)
    }

    override fun toCanvasPoint(worldPoint: Vec2): Vec2 {
        return convertToNodeSpace(worldPoint)
    }

    fun performSelected(view: SMView?, selected: Boolean) {
        _listener?.onStickerSelected(view, selected)
    }

    fun performFly(view: SMView?, degrees: Float, speed: Float) {
        if (!_flyRemovable) return

        if (view==null) return

        if (_selectedView==view) {
            performSelected(view, false)
            _selectedView = null
        }

        val pt = view.getPosition()
        val dist = speed / 10f

        val radian = ( degrees * M_PI ).toFloat() / 180.0f

        val deltaX = dist * cos(radian)
        val deltaY = dist * sin(radian)
        val rotate = speed / 100.0f

        val direction = if (degrees>90 && degrees<270) -1f else 1f
        val moveTo = EaseOut.create(getDirector(), MoveBy.create(getDirector(), FLY_DURATION, Vec2(deltaX, deltaY)), 3.0f)
        val rotateTo = RotateBy.create(getDirector(), FLY_DURATION, direction * rotate)
        val fadeTo = Sequence.create(getDirector(), DelayTime.create(getDirector(), FLY_DURATION/2f), FadeOut.create(getDirector(), FLY_DURATION/2f), null)
        val remove = Spawn.create(getDirector(), moveTo!!, rotateTo, fadeTo!!, null)
        val seq = Sequence.create(getDirector(), remove, CallFuncN.create(getDirector(), object : PERFORM_SEL_N {
            override fun performSelector(view: SMView?) {
                if (view!=null) {
                    _listener?.onStickerRemoveEnd(view)
                    view.removeFromParent()
                }
            }
        }), null)
        seq?.setTag(AppConst.TAG.ACTION_STICKER_REMOVE)
        view.runAction(seq!!)

        _listener?.onStickerRemoveBegin(view)
    }

    fun removeChildWithGenieAction(child: SMView, sprite: Sprite?, removeAnchor: Vec2) {
        removeChildWithGenieAction(child, sprite, removeAnchor, 0.7f)
    }

    fun removeChildWithGenieAction(child: SMView, sprite: Sprite?, removeAnchor: Vec2, duration: Float) {
        removeChildWithGenieAction(child, sprite, removeAnchor, duration, 0.15f)
    }

    fun removeChildWithGenieAction(child: SMView, sprite: Sprite?, removeAnchor: Vec2, duration: Float, delay: Float) {
        if (sprite==null) {
            removeChild(child)
            return
        }

        if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null) return

        if (_selectedView==child) {
            performSelected(child, false)
            _selectedView = null
        }

        val genie = EaseBackIn.create(getDirector(), GenieAction.create(getDirector(), duration, sprite, removeAnchor))
        val seq = Sequence.create(getDirector(), DelayTime.create(getDirector(), delay), genie, CallFuncN.create(getDirector(), object : PERFORM_SEL_N {
            override fun performSelector(view: SMView?) {
                _listener?.onStickerRemoveEnd(view)
                view!!.removeFromParent()
            }
        }), null)
        seq!!.setTag(AppConst.TAG.ACTION_STICKER_REMOVE)
        child.runAction(seq)

        _listener?.onStickerRemoveBegin(child)
    }

    fun removeChildWithFadeOut(child: SMView, duration: Float, delay: Float) {
        if (child.getAlpha()<=0f) {
            removeChild(child)
            return
        }

        if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null) {
            return
        }

        if (_selectedView==child) {
            performSelected(child, false)
            _selectedView = null
        }

        val seq = Sequence.create(getDirector(), DelayTime.create(getDirector(), delay), FadeTo.create(getDirector(), duration, 0f), CallFuncN.create(getDirector(), object : PERFORM_SEL_N {
            override fun performSelector(view: SMView?) {
                _listener?.onStickerRemoveEnd(view)
            }
        }), null)
        seq?.setTag(AppConst.TAG.ACTION_STICKER_REMOVE)
        child.runAction(seq!!)

        _listener?.onStickerRemoveBegin(child)
    }

    fun removeChildWithFly(child: SMView, degrees: Float, speed: Float) {
        if (child.getAlpha()<=0f) {
            removeChild(child)
            return
        }

        if (child.getActionByTag(AppConst.TAG.ACTION_STICKER_REMOVE)!=null) {
            return
        }

        if (_selectedView==child) {
            performSelected(child, false)
            _selectedView = null
        }

        performFly(child, degrees, speed)
    }

    override fun containsPoint(point: Vec2): Boolean {
        return true
    }

    override fun containsPoint(x: Float, y: Float): Boolean {
        return true
    }

    interface StickerCanvasListener {
        fun onStickerSelected(view: SMView?, select: Boolean)
        fun onStickerRemoveBegin(view: SMView?)
        fun onStickerRemoveEnd(view: SMView?)
        fun onStickerDoubleClicked(view: SMView?, worldPoint: Vec2)
        fun onStickerTouch(view: SMView?, action: Int)
    }

    fun setStickerCanvasListener(l: StickerCanvasListener) {_listener = l}

    override fun dispatchTouchEvent(event: MotionEvent?, view: SMView, checkBounds: Boolean): Int {
        return super.dispatchTouchEvent(event, view, false)
    }

    override fun cancel() {
        if (_selectedView!=null) {
            setSelectedSticker(null)
        }
        super.cancel()
    }
}