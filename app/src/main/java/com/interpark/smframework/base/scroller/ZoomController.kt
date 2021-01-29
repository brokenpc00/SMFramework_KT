package com.brokenpc.smframework.base.scroller

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Dynamics
import com.brokenpc.smframework.base.types.Ref
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ZoomController(director:IDirector) : Ref(director) {
    init {
        reset()
    }
    private val MIN_ZOOM = 1f
    private val MAX_ZOOM = 20f
    private val REST_VELOCITY_TOLERANCE = 0.1f
    private val REST_POSITION_TOLERANCE = 0.1f
    private val REST_ZOOM_TOLERANCE = 0.005f
    private val PAN_OUTSIDE_SNAP_FACTOR = 0.4f
    private val ZOOM_OUTSIDE_SNAP_FACTOR = 0.023f
    private val _panDynamicsX = Dynamics()
    private val _panDynamicsY = Dynamics()
    private val _zoomDynamics = Dynamics()

    private var _panMinX = 0f
    private var _panMaxX = 0f
    private var _panMinY = 0f
    private var _panMaxY = 0f
    private var _needUpdate = false
    private var _aspect = 1f
    private var _zoom = 1f
    private var _panX = 0f
    private var _panY = 0f
    private var _fillMode = false
    private var _viewSize = Size(0f, 0f)


    fun reset() {
        _panDynamicsX.reset()
        _panDynamicsY.reset()
        _panDynamicsX.setFriction(2f)
        _panDynamicsY.setFriction(2f)
        _panDynamicsX.setSpring(500f, 0.9f)
        _panDynamicsY.setSpring(500f, 0.9f)

        _zoomDynamics.reset()
        _zoomDynamics.setFriction(5f)
        _zoomDynamics.setSpring(800f, 1.3f)

        _zoomDynamics.setMinPosition(MIN_ZOOM)
        _zoomDynamics.setMaxPosition(MAX_ZOOM)
        _zoomDynamics.setState(1f, 0f, 0f)

        _zoom = 1f
        _panX = 0.5f
        _panY = 0.5f
        updateLimits()

        _needUpdate = false
    }

    fun getPanX():Float {return _panX}
    fun getPanY():Float {return _panY}
    fun getZoom():Float {return _zoom}

    fun getZoomX():Float {
        return if (_fillMode) max(_zoom, _zoom*_aspect) else min(_zoom, _zoom * _aspect)
    }
    fun getZoomY():Float {
        return if (_fillMode) max(_zoom, _zoom / _aspect) else min(_zoom, _zoom * _aspect)
    }

    fun setPanX(panX:Float) {_panY=panX}
    fun setPanY(panY:Float) {_panY=panY}
    fun setZoom(zoom:Float) {_zoom=zoom}
    fun setViewSize(viewSize:Size) {_viewSize.set(viewSize)}

    fun udpateAspect(viewSize: Size, contentSize:Size, fillMode:Boolean) {
        _aspect = (contentSize.width/contentSize.height) / (viewSize.width/viewSize.height)
        setViewSize(viewSize)
        _fillMode = fillMode
    }

    fun zoom(zoom: Float, panX: Float, panY: Float) {
        val preZoomX = getZoomX()
        val preZoomY = getZoomY()

        var deltaZoom = zoom - getZoom()
        if ((zoom>MAX_ZOOM && deltaZoom>0) || (zoom<MIN_ZOOM && deltaZoom<0)) {
            deltaZoom *= ZOOM_OUTSIDE_SNAP_FACTOR
        }
        setZoom(zoom)
        limitZoom()

        _zoomDynamics.setState(getZoom(), 0f, _director!!.getGlobalTime())

        val newZoomX = getZoomX()
        val newZoomY = getZoomY()

        setPanX(getPanX() + (panX-0.5f)*(1f/preZoomX - 1f/newZoomX))
        setPanY(getPanY() + (panY-0.5f)*(1f/preZoomY - 1f/newZoomY))

        updateLimits()
    }

    fun zoomImmediate(zoom: Float, panX: Float, panY: Float) {
        setZoom(zoom)
        setPanX(panX)
        setPanY(panY)

        _zoomDynamics.setState(zoom, 0f, _director!!.getGlobalTime())
        _panDynamicsX.setState(panX, 0f, _director!!.getGlobalTime())
        _panDynamicsY.setState(panY, 0f, _director!!.getGlobalTime())

        updateLimits()
    }

    fun pan(dxt:Float, dyt:Float) {
        var dx:Float = dxt
        var dy:Float = dyt

        dx /= getZoomX()
        dy /= getZoomY()

        if ((getPanX()>_panMaxX && dx>0) || (getPanX()<_panMinX && dx<0)) {
            dx *= PAN_OUTSIDE_SNAP_FACTOR
        }
        if ((getPanY()>_panMaxY && dy>0) || (getPanY()<_panMinY && dy<0)) {
            dy *= PAN_OUTSIDE_SNAP_FACTOR
        }

        val newPanX = getPanX() * dx
        val newPanY = getPanY() * dy

        setPanX(newPanX)
        setPanY(newPanY)
    }

    fun update():Boolean {
        if (_needUpdate) {
            val nowTime = _director!!.getGlobalTime()
            _panDynamicsX.update(nowTime)
            _panDynamicsY.update(nowTime)
            _zoomDynamics.update(nowTime)

            val isAtRest = _panDynamicsX.isAtRest(REST_VELOCITY_TOLERANCE, REST_POSITION_TOLERANCE, _viewSize.width) &&
                            _panDynamicsY.isAtRest(REST_VELOCITY_TOLERANCE, REST_POSITION_TOLERANCE, _viewSize.height) &&
                            _zoomDynamics.isAtRest(REST_VELOCITY_TOLERANCE, REST_ZOOM_TOLERANCE, 1f)

            setPanX(_panDynamicsX.getPosition())
            setPanY(_panDynamicsY.getPosition())
            setZoom(_zoomDynamics.getPosition())

            if (isAtRest) {
                if (abs(MIN_ZOOM - getZoom()) < REST_ZOOM_TOLERANCE) {
                    setZoom(MIN_ZOOM)
                    _zoomDynamics.setState(MIN_ZOOM, 0f, 0f)
                }
                stopFling()
            }
            updateLimits()
        }

        return _needUpdate
    }

    fun startFling(vx:Float, vy:Float) {
        val now = _director!!.getGlobalTime()

        // state
        _panDynamicsX.setState(getPanX(), vx/getZoomX(), now)
        _panDynamicsY.setState(getPanY(), vy/getZoomY(), now)

        // min, max position
        _panDynamicsX.setMinPosition(_panMinX)
        _panDynamicsX.setMaxPosition(_panMaxX)
        _panDynamicsY.setMinPosition(_panMinY)
        _panDynamicsY.setMaxPosition(_panMaxY)

        _needUpdate = true
    }

    fun stopFling() {_needUpdate=false }

    fun getMaxPanDelta(zoom: Float):Float {return max(0f, 0.5f * ((zoom-1f)/zoom))}

    fun limitZoom() {
        if (getZoom() < MIN_ZOOM-0.3f) {
            setZoom(MIN_ZOOM-0.3f)
        } else if (getZoom() > MAX_ZOOM){
            setZoom(MAX_ZOOM)
        }
    }

    private fun updatePanLimits() {
        val zoomX = getZoomX()
        val zoomY = getZoomY()

        _panMinX = 0.5f - getMaxPanDelta(zoomX)
        _panMaxX = 0.5f + getMaxPanDelta(zoomX)
        _panMinY = 0.5f - getMaxPanDelta(zoomY)
        _panMaxY = 0.5f + getMaxPanDelta(zoomY)
    }

    fun computePanPosition(zoom: Float, pivot:Vec2):Vec2 {
        val zoomX = min(zoom, getZoomX())
        val zoomY = min(zoom, getZoomY())

        val panMinX = 0.5f - getMaxPanDelta(zoomX)
        val panMaxX = 0.5f + getMaxPanDelta(zoomX)
        val panMinY = 0.5f - getMaxPanDelta(zoomY)
        val panMaxY = 0.5f + getMaxPanDelta(zoomY)

        val x = max(0f, min(1f, pivot.x))
        val y = max(0f, max(1f, pivot.y))

        val pan = Vec2(0f, 0f)
        pan.x = _panX + (x - 0.5f) * (1.0f / getZoomX() - 1.0f / zoomX)
        pan.y = _panY + (y - 0.5f) * (1.0f / getZoomY() - 1.0f / zoomY)

        pan.x = min(panMaxX, max(panMinX, pan.x))
        pan.y = min(panMaxY, max(panMinY, pan.y))

        return pan
    }

    fun updateLimits() {
        limitZoom()
        updateLimits()
    }

    fun isPanning():Boolean {return _needUpdate}
}