package com.interpark.smframework.view

import android.view.MotionEvent
import android.view.VelocityTracker
import com.brokenpc.app.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.UIContainerView
import com.brokenpc.smframework.base.scroller.ZoomController
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.AppConst
import com.interpark.smframework.base.types.EaseSineInOut
import kotlin.Exception
import kotlin.math.abs
import kotlin.math.sqrt

class SMZoomView(director: IDirector): UIContainerView(director) {
    private var _mode = Mode.UNDEFINED
    private var _contentView: SMView? = null
    private lateinit var _controller: ZoomController
    private var _velocityTracker: VelocityTracker? = null
    private var _panX = 0f
    private var _panY = 0f
    private var _zoom = 1f
    private var _baseZoom = 1f
    private var _prevZoom = 1f
    private var _prevDistance = 0f
    private var _prevTouchX = 0f
    private var _prevTouchY = 0f
    private var _initTouchX = 0f
    private var _initTouchY = 0f
    private var _panEnable = true
    private var _zoomEnable = true
    private var _interpolate = true
    private var _accuX = 0f
    private var _accuY = 0f
    private var _fillType = FillType.INSIDE
    private var _innerSize = Size(Size.ZERO)

    companion object {
        const val FLAG_ZOOM_UPDATE = 1L

        @JvmStatic
        fun create(director: IDirector, x: Float, y: Float, width: Float, height: Float, anchorX: Float, anchorY: Float): SMZoomView {
            val view = SMZoomView(director)
            view.setPosition(x, y)
            view.setContentSize(width, height)
            view.setAnchorPoint(anchorX, anchorY)
            view.init()
            return view
        }
    }

    override fun init(): Boolean {
        super.init()

        _uiContainer.setAnchorPoint(Vec2.ZERO)
        _uiContainer.setPosition(Vec2.ZERO)
        _controller = ZoomController(getDirector())
        setDoubleClickable(true)
        return true
    }

    enum class FillType {
        INSIDE,
        FILL
    }

    enum class Mode {
        UNDEFINED,
        PAN,
        ZOOM
    }

    class ZoomTo(director: IDirector, zoom: Float, pan: Vec2, duration: Float): ActionInterval(director) {
        private val _fromPan = Vec2(Vec2.ZERO)
        private var _fromZoom = 1f
        private val _toPan = pan
        private var _toZoom = zoom

        init {
            initWithDuration(duration)
        }

        override fun startWithTarget(target: SMView?) {
            super.startWithTarget(target)

            if (_target is SMZoomView) {
                val zoomView = _target as SMZoomView
                val controller = zoomView.getController()
                _fromPan.set(controller.getPan())
                _fromZoom = controller.getZoom()
            }
        }

        override fun update(t: Float) {
            if (_target is SMZoomView) {
                val zoomView = _target as SMZoomView
                val controller = zoomView.getController()
                val zoom = interpolation(_fromZoom, _toZoom, t)
                val panX = interpolation(_fromPan.x, _toPan.x, t)
                val panY = interpolation(_fromPan.y, _toPan.y, t)

                controller.zoomImmediate(zoom, panX, panY)
                zoomView.registerUpdate(FLAG_ZOOM_UPDATE)
            }
        }
    }

    fun setFillType(type: FillType) {_fillType = type}
    fun setPanEnable(enable: Boolean) {_panEnable = enable}
    fun setZoomEnable(enable: Boolean) {_zoomEnable = enable}
    fun isIdle(): Boolean {return _mode==Mode.UNDEFINED}
    fun isPanning(): Boolean {return _controller.isPanning()}
    fun getContentView(): SMView? {return _contentView}

    override fun setPadding(padding: Float) {
        if (BuildConfig.DEBUG && _contentView!=null) {
            error("Assertion failed")
        }

        super.setPadding(padding)
    }

    override fun setPadding(left: Float, top: Float, right: Float, bottom: Float) {
        if (BuildConfig.DEBUG && _contentView!=null) {
            error("Assertion Failed")
        }

        super.setPadding(left, top, right, bottom)
    }

    override fun setContentSize(size: Size) {
        _innerSize.set(size.width-_paddingLeft-_paddingRight, size.height-_paddingTop-_paddingBottom)
        super.setContentSize(size)

        registerUpdate(FLAG_ZOOM_UPDATE)
    }

    fun refreshContentView(reset: Boolean) {
        if (_contentView!=null) {
            _uiContainer.setContentSize(_contentView!!.getContentSize())

            if (reset) {
                _controller.reset()

                _uiContainer.setAnchorPoint(Vec2.MIDDLE)
                _uiContainer.setPosition(_paddingLeft+_innerSize.width/2f, _paddingBottom+_innerSize.height/2f)
                _uiContainer.setScale(_controller.getZoom()*_baseZoom)
            }

            _controller.updateAspect(_innerSize, _contentView!!.getContentSize(), _fillType==FillType.FILL)
            _controller.updateLimits()
            _baseZoom = computeBaseZoom(_innerSize, _contentView!!.getContentSize())
        }
    }

    fun setContentView(contentView: SMView?) {
        if (_contentView==contentView) return

        if (_contentView!=null) {
            _uiContainer.removeChild(_contentView)
            _controller.reset()
        }

        if (contentView!=null) {
            val size = contentView.getContentSize()

            _uiContainer.addChild(contentView)
            _uiContainer.setContentSize(size)

            _controller.reset()
            _controller.updateAspect(_innerSize, size, _fillType==FillType.FILL)
            _controller.updateLimits()
            _baseZoom = computeBaseZoom(_innerSize, size)

            _uiContainer.setAnchorPoint(Vec2.MIDDLE)
            _uiContainer.setPosition(getContentSize().width/2f, getContentSize().height/2f)

            val scale = _controller.getZoom() * _baseZoom
            _uiContainer.setScale(scale)
            contentView.setIgnoreTouchBounds(true)

            _interpolate = true
        }

        _contentView = contentView
    }

    fun getController():ZoomController {return _controller}

    fun computeBaseZoom(viewSize: Size, contentSize: Size): Float {
        val zoomX = viewSize.width/contentSize.width
        val zoomY = viewSize.height/contentSize.height

        if (_fillType==FillType.INSIDE) {
            return zoomX.coerceAtMost(zoomY)
        } else {
            return zoomX.coerceAtLeast(zoomY)
        }
    }

    fun getContentZoomScale(): Float {return _zoom}

    fun getContentBaseScale(): Float {return _baseZoom}

    fun getContentPosition(): Vec2 {return _uiContainer.getPosition()}

    fun getZoom(): Float {return _zoom}

    fun getPanX(): Float {return _panX}

    fun getPanY(): Float {return _panY}

    override fun containsPoint(point: Vec2): Boolean {
        if (_ignoreTouchBounds) {
            return true
        }
        return super.containsPoint(point)
    }

    override fun containsPoint(x: Float, y: Float): Boolean {
        if (_ignoreTouchBounds) {
            return true
        }
        return super.containsPoint(x, y)
    }

    override fun onUpdateOnVisit() {
        if (!_controller.update()) {
            unregisterUpdate(FLAG_ZOOM_UPDATE)
        }

        _panX = _controller.getPanX()
        _panY = _controller.getPanY()
        _zoom = _controller.getZoom()

        val scale = _baseZoom*_zoom

        val x = scale * (-(_panX-0.5f)) * _uiContainer.getContentSize().width
        val y = scale * (-(_panY-0.5f)) * _uiContainer.getContentSize().height

        _uiContainer.setPosition(_paddingLeft+_innerSize.width/2f + x, _paddingBottom+_innerSize.height/2f+y, _interpolate)
        _uiContainer.setScale(scale, _interpolate)

        _interpolate = false
    }

    override fun dispatchTouchEvent(event: MotionEvent?, view: SMView, checkBounds: Boolean): Int {
        return super.dispatchTouchEvent(event, view, false)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Int {
        if (_mode==Mode.UNDEFINED) {
            val ret = super.dispatchTouchEvent(event)
            if (ret== TOUCH_INTERCEPT) {
                return ret
            }
        }

        val gv = Vec2(event.x, event.y)
        val mm = Vec2(_paddingLeft, _paddingBottom)

        val point = Vec2(gv.x-mm.x, gv.y-mm.y)

        var x = point.x
        var y = point.y

        if (_velocityTracker==null) {
            _velocityTracker = VelocityTracker.obtain()
        }

        _velocityTracker!!.addMovement(event)

        val action = event.action

        when (action.and(MotionEvent.ACTION_MASK)) {
            MotionEvent.ACTION_DOWN -> {
                if (_panEnable) {
                    _controller.stopFling()
                }

                _initTouchX = x.also { _prevTouchX = it }
                _initTouchY = y.also { _prevTouchY = it }

                _accuX = 0f.also { _accuY }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (_zoomEnable) {
                    val point2 = Vec2(event.getX(1), event.getY(1))
                    point2.set(point2.x-_paddingLeft, point2.y-_paddingBottom)

                    val distance = spacing(event)

                    if (distance>0f) {
                        _prevDistance = distance
                        if (_prevDistance>10.0f) {
                            _mode = Mode.ZOOM

                            val midPoint = Vec2((point.x+point2.x)/2f, (point.y+point2.y)/2f)
                            _initTouchX = midPoint.x.also { _prevTouchX = it }
                            _initTouchY = midPoint.y.also { _prevTouchY = it }

                            _prevZoom = _controller.getZoom()
                        }
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (_mode==Mode.ZOOM) {
                    // zoom in/out
                    val point2 = Vec2(event.getX(1), event.getY(1))
                    point2.set(point2.x - _paddingLeft, point2.y-_paddingBottom)

                    val midPoint = Vec2((point.x+point2.x)/2f, (point.y+point2.y)/2f)

                    x = midPoint.x
                    y = midPoint.y

                    val dx = (x-_prevTouchX) / _innerSize.width
                    val dy = (y-_prevTouchY) / _innerSize.height

                    _controller.pan(-dx, -dy)

                    val distance = spacing(event)
                    val scale = (distance/_prevDistance) * _prevZoom

                    _controller.zoom(scale, x / _innerSize.width, y / _innerSize.height)
                    registerUpdate(FLAG_ZOOM_UPDATE)

                } else if (_mode==Mode.PAN) {
                    // moving

                    val dx = (x - _prevTouchX) / _innerSize.width
                    val dy = (y - _prevTouchY) / _innerSize.height

                    _controller.pan(-dx, -dy)
                    registerUpdate(FLAG_ZOOM_UPDATE)

                } else {
                    if (_panEnable) {
                        val scrollX = _initTouchX - x
                        val scrollY = _initTouchY - y
                        val distance = sqrt(scrollX*scrollX+scrollY*scrollY)

                        if (distance>AppConst.Config.SCALED_TOUCH_SLOPE) {
                            _mode = Mode.PAN
                            registerUpdate(FLAG_ZOOM_UPDATE)
                        }
                    }
                }
                _accuX += x - _prevTouchX
                _accuY += y - _prevTouchY

                _prevTouchX = x
                _prevTouchY = y

            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (_mode==Mode.ZOOM) {
                    val index = event.actionIndex
                    if (index==0 || index==1) {
                        // just two finger allowed
                        _mode = Mode.PAN
                        val pt = if (index==1) {
                            Vec2(event.getX(0) - _paddingLeft, event.getY(0) - _paddingBottom)
                        } else {
                            Vec2(event.getX(1) - _paddingLeft, event.getY(1) - _paddingBottom)
                        }

                        _initTouchX = pt.x.also { _prevTouchX = it }
                        _initTouchY = pt.y.also { _prevTouchY = it}
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (_mode==Mode.PAN) {
                    _velocityTracker!!.computeCurrentVelocity(AppConst.Config.MAX_VELOCITY.toInt(), AppConst.Config.MAX_VELOCITY)
                    val vx = _velocityTracker!!.getXVelocity(0)
                    val vy = _velocityTracker!!.getYVelocity(0)
                    _controller.startFling(-vx/_innerSize.width, -vy/_innerSize.height)
                } else {
                    if (_panEnable) {
                        _controller.updateLimits()
                        _controller.startFling(0f, 0f)
                    }
                }

                _velocityTracker!!.recycle()
                _velocityTracker = null

                _mode = Mode.UNDEFINED
                registerUpdate(FLAG_ZOOM_UPDATE)
            }
        }

        return TOUCH_TRUE
    }

    fun updateZoom() {registerUpdate(FLAG_ZOOM_UPDATE)}

    override fun performDoubleClick(worldPoint: Vec2?) {
        if (!_zoomEnable || _contentView==null || worldPoint==null) return

        val action = getActionByTag(AppConst.TAG.ACTION_ZOOM)
        if (action!=null) {
            stopAction(action)
        }

        val zoom = _controller.getZoom()
        val point = Vec2(worldPoint.x - _paddingLeft, worldPoint.y - _paddingBottom)
        val pivot = Vec2(point.x / _innerSize.width, point.y / _innerSize.height)

        var newZoom = 1f
        if (abs(zoom-1f)<=0.5f) {
            newZoom = 2f
        } else if (abs(zoom-2f)<=0.5f) {
            newZoom = 4f
        }

        val newPan = Vec2(_controller.computePanPosition(newZoom, pivot))

        // animation
        val zoomTo = EaseSineOut.create(getDirector(), ZoomTo(getDirector(), newZoom, newPan, AppConst.Config.ZOOM_NORMAL_TIME))
        zoomTo?.setTag(AppConst.TAG.ACTION_ZOOM)
        runAction(zoomTo!!)
    }

    fun setFocusRect(focusRect: Rect, duration: Float) {
        if (focusRect.size.width<=0f || focusRect.size.height<=0f) return

        if (_contentView==null) return

        val action = getActionByTag(AppConst.TAG.ACTION_ZOOM)
        if (action!=null) {
            stopAction(action)
        }

        val size = _contentView!!.getContentSize()
        val aspectView = _contentSize.width / _contentSize.height
        val aspectCont = size.width / size.height

        var width: Float
        var height: Float

        if (aspectCont>aspectView) {
            width = size.width
            height = size.width / aspectView
        } else {
            height = size.height
            width = size.height * aspectView
        }

        val newZoom = (width/focusRect.size.width).coerceAtMost(height/focusRect.size.height)
        val newPanxX = focusRect.getMinX() / size.width
        val newPanY = focusRect.getMidY() / size.height

        // zoom in animation
        val zoomTo = EaseSineInOut.create(getDirector(), ZoomTo(getDirector(), newZoom, Vec2(newPanxX, newPanY), duration))
        zoomTo?.setTag(AppConst.TAG.ACTION_ZOOM)

        runAction(zoomTo!!)
    }

    fun setZoomWithAnimation(panX: Float, panY: Float, zoom: Float, duration: Float) {
        val action = getActionByTag(AppConst.TAG.ACTION_ZOOM)
        if (action!=null) {
            stopAction(action)
        }

        val zoomTo = EaseSineInOut.create(getDirector(), ZoomTo(getDirector(), zoom, Vec2(panX, panY), duration))
        zoomTo!!.setTag(AppConst.TAG.ACTION_ZOOM)
        runAction(zoomTo)
    }

    fun spacing(event: MotionEvent): Float {
        try {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0 ) - event.getY(1)
            return sqrt(x*x+y*y)
        } catch (e: Exception) {

        }

        return -1f
    }

    fun midPoint(point: Vec2, event: MotionEvent): Vec2 {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        return point.set(x/2f, y/2f)
    }

    fun getReverseFocusRect(focusRect: Rect): Rect {
        // ToDo. realization
        return Rect(Rect.ZERO)
    }
}