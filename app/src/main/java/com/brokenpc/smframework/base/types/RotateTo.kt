package com.interpark.smframework.base.types

import com.brokenpc.smframework_kt.BuildConfig
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Vec3

class RotateTo(director: IDirector) : ActionInterval(director) {
    protected var _is3D = false
    protected val _dstAngle = Vec3(Vec3.ZERO)
    protected val _startAngle = Vec3(Vec3.ZERO)
    protected val _diffAngle = Vec3(Vec3.ZERO)

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, dstAngleX: Float, dstAngleY: Float): RotateTo? {
            val rot = RotateTo(director)
            if (rot.initWithDuration(duration, dstAngleX, dstAngleY)) {
                return rot
            }
            return null
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, dstAngle: Float): RotateTo? {
            val rot = RotateTo(director)
            if (rot.initWithDuration(duration, dstAngle, dstAngle)) {
                return rot
            }
            return null
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, dstAngle3D:Vec3): RotateTo? {
            val rot = RotateTo(director)
            if (rot.initWithDuration(duration, dstAngle3D)) {
                return rot
            }
            return null
        }
    }

    override fun Clone(): ActionInterval? {
        val a = RotateTo(getDirector())
        if (_is3D) {
            a.initWithDuration(_duration, _dstAngle)
        } else {
            a.initWithDuration(_duration, _dstAngle.x, _dstAngle.y)
        }

        return a
    }

    override fun reverse(): ActionInterval? {
        if (BuildConfig.DEBUG) {
            error("Assertion failed")
        } // no reverse
        return null
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)

        if (_is3D) {
            _startAngle.set(_target?.getRotation3D()?: Vec3(Vec3.ZERO))
        } else {
            _startAngle.x = _target?.getRotationSkewX()?:0f
            _startAngle.y = _target?.getRotationSkewY()?:0f
        }

        _startAngle.x = calculateAnglesStart(_startAngle.x)
        _diffAngle.x = calculateAnglesDiff(_startAngle.x, _dstAngle.x)

        _startAngle.y = calculateAnglesStart(_startAngle.y)
        _diffAngle.y = calculateAnglesDiff(_startAngle.y, _dstAngle.y)

        _startAngle.z = calculateAnglesStart(_startAngle.z)
        _diffAngle.z = calculateAnglesDiff(_startAngle.z, _dstAngle.z)
    }

    override fun update(dt: Float) {
        if (_target!=null) {
            if (_is3D) {
                _target?.setRotation3D(Vec3(_startAngle.x+_diffAngle.x*dt, _startAngle.y+_diffAngle.y*dt, _startAngle.z+_diffAngle.z*dt))
            } else {
                _target?.setRotationSkewX(_startAngle.x+_diffAngle.x*dt)
                _target?.setRotationSkewY(_startAngle.y+_diffAngle.y*dt)
            }
        }
    }

    protected fun initWithDuration(duration: Float, dstAngleX: Float, dstAngleY: Float): Boolean {
        if (super.initWithDuration(duration)) {
            _dstAngle.x = dstAngleX
            _dstAngle.y = dstAngleY
            return true
        }
        return false
    }

    protected fun initWithDuration(duration: Float, dstAngle3D: Vec3): Boolean {
        if (super.initWithDuration(duration)) {
            _dstAngle.set(dstAngle3D)
            _is3D = true
            return true
        }
        return false
    }

    fun calculateAnglesStart(s: Float): Float {
        var start = s
        if (start>0) {
            start %= 360.0f
        } else {
            start %= -360.0f
        }
        return start
    }

    fun calculateAnglesDiff(start: Float, dst: Float): Float {
        var diff = dst - start
        if (diff > 180.0f) {
            diff -= 360.0f
        }
        if (diff < -180.0f) {
            diff += 360.0f
        }
        return diff
    }
}