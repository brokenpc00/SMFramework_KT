package com.brokenpc.smframework.base.scroller

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Dynamics
import com.brokenpc.smframework.base.types.Ref

class ScrollController(director:IDirector) : Ref(director) {
    private val REST_VELOCITY_TOLERANCE = 0.5f

    private val REST_POSITION_TOLERANCE = 0.5f

    private val PAN_OUTSIDE_SNAP_FACTOR = 0.2f

    var _panDynamicsY: Dynamics = Dynamics()

    private var _panMinY = 0.0f

    private var _panMaxY = 0.0f

    private var _needUpdate = false

    private var _panY = 0.0f

    private var _viewSize = 0.0f

    private var _scrollSize = 0.0f

    private var _hangSize = 0.0f

    private val SCROLLING_STIFFNESS = 150.0f

    private val SCROLLING_DAMPING = 1.0f

    fun reset() {
        _panDynamicsY.reset()
        _panDynamicsY.setFriction(3.0f)

        // bouncing... 조절...
        _panDynamicsY.setSpring(SCROLLING_STIFFNESS, SCROLLING_DAMPING)
        _panY = 0.0f
        updateLimits()
        _needUpdate = false
    }

    fun getPanY(): Float {
        return _panY
    }

    fun setPanY(panY: Float) {
        setPanY(panY, false)
    }

    fun setPanY(panY: Float, force: Boolean) {
        _panY = panY
        if (force) {
            _panDynamicsY.setState(_panY, 0f, _director!!.getGlobalTime())
        }
    }

    fun setViewSize(viewSize: Float) {
        _viewSize = viewSize
        updateLimits()
    }

    fun setScrollSize(scrollSize: Float) {
        _scrollSize = scrollSize
        updateLimits()
    }

    fun pan(dy: Float) {
        var dy = dy
        if (getPanY() > _panMaxY && dy > 0 || getPanY() < _panMinY && dy < 0) {
            dy *= PAN_OUTSIDE_SNAP_FACTOR
        }
        val newPanY = getPanY() + dy
        setPanY(newPanY)
    }

    fun update(): Boolean {
        if (_needUpdate) {
            val nowTime = _director!!.getGlobalTime()
            _panDynamicsY.update(nowTime)
            val isAtRest: Boolean =
                _panDynamicsY.isAtRest(REST_VELOCITY_TOLERANCE, REST_POSITION_TOLERANCE)
            setPanY(_panDynamicsY.getPosition())
            if (isAtRest) {
                stopFling()
            }
        }
        return _needUpdate
    }

    fun startFling(vy: Float) {
        var vy = vy
        val now = _director!!.getGlobalTime()
        if (vy < 0 && _panY < _panMinY || vy > 0 && _panY > _panMaxY) {
            vy /= 5f
        }
        _panDynamicsY.setState(getPanY(), vy, now)
        _panDynamicsY.setMinPosition(_panMinY - _hangSize)
        _panDynamicsY.setMaxPosition(_panMaxY)
        _needUpdate = true
    }

    fun stopFling() {
        _needUpdate = false
    }

    private fun updatePanLimits() {
        _panMinY = 0f
        _panMaxY = Math.max(0.0f, _scrollSize - _viewSize)
    }

    fun updateLimits() {
        updatePanLimits()
    }

    fun isPanning(): Boolean {
        return _needUpdate
    }

    fun stopIfExceedLimit() {
        _panDynamicsY.setMinPosition(_panMinY - _hangSize)
        _panDynamicsY.setMaxPosition(_panMaxY)
        if (getPanY() > _panMaxY || getPanY() < _panMinY) {
            startFling(0f)
        }
    }

    fun setHangSize(size: Float) {
        _hangSize = size
        _panDynamicsY.setMinPosition(_panMinY - _hangSize)
    }
}