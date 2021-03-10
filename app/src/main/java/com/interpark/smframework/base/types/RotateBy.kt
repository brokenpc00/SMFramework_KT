package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Vec3

class RotateBy(director: IDirector): ActionInterval(director) {
    protected var _is3D = false
    protected val _deltaAngle = Vec3.ZERO
    protected val _startAngle = Vec3.ZERO

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, deltaAngle: Float): RotateBy {
            val rot = RotateBy(director)
            rot.initWithDuration(duration, deltaAngle)
            return rot
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, deltaAngleZ_X: Float, deltaAngleZ_Y: Float): RotateBy {
            val rot = RotateBy(director)
            rot.initWithDuration(duration, deltaAngleZ_X, deltaAngleZ_Y)
            return rot
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, deltaAngle3D: Vec3): RotateBy {
            val rot = RotateBy(director)
            rot.initWithDuration(duration, deltaAngle3D)
            return rot
        }
    }

    protected fun initWithDuration(duration: Float, deltaAngle: Float): Boolean {
        super.initWithDuration(duration)
        _deltaAngle.x = deltaAngle.also { _deltaAngle.y = it }
        return true
    }

    protected fun initWithDuration(duration: Float, deltaAngleZ_X: Float, deltaAngleZ_Y: Float): Boolean {
        super.initWithDuration(duration)
        _deltaAngle.x = deltaAngleZ_X
        _deltaAngle.y = deltaAngleZ_Y
        return true
    }

    protected fun initWithDuration(duration: Float, deltaAngle3D: Vec3): Boolean {
        super.initWithDuration(duration)
        _deltaAngle.set(deltaAngle3D)
        _is3D = true
        return true
    }

    override fun Clone(): ActionInterval? {
        val a = RotateBy(getDirector())
        if (_is3D) {
            a.initWithDuration(_duration, _deltaAngle)
        } else {
            a.initWithDuration(_duration, _deltaAngle.x, _deltaAngle.y)
        }

        return a
    }

    override fun reverse(): ActionInterval? {
        return if (_is3D) {
            val v = Vec3(-_deltaAngle.x, -_deltaAngle.y, -_deltaAngle.z)
            RotateBy.create(getDirector(), _duration, v)
        } else {
            RotateBy.create(getDirector(), _duration, -_deltaAngle.x, -_deltaAngle.y)
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        if (_is3D) {
            _startAngle.set(target?.getPosition3D()?: Vec3.ZERO)
        } else {
            _startAngle.x = target?.getRotationSkewX()?:0f
            _startAngle.y = target?.getRotationSkewY()?:0f
        }
    }

    override fun update(dt: Float) {
        if (_target!=null) {
            if (_is3D) {
                val v = Vec3(_startAngle.x + _deltaAngle.x*dt, _startAngle.y+_deltaAngle.y*dt, _startAngle.z+_deltaAngle.z*dt)
                _target?.setRotation3D(v)
            } else {
                if (_startAngle.x==_startAngle.y && _deltaAngle.x==_deltaAngle.y) {
                    _target?.setRotation(_startAngle.x+_deltaAngle.x*dt)
                } else {
                    _target?.setRotationSkewX(_startAngle.x+_deltaAngle.x*dt)
                    _target?.setRotationSkewY(_startAngle.y+_deltaAngle.y*dt)
                }
            }
        }
    }
}