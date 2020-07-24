package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMView

open class MoveBy(director:IDirector) : ActionInterval(director) {
    protected var _is3D:Boolean = false
    protected var _positionDelta:Vec3 = Vec3(0f, 0f, 0f)
    protected var _startPosition:Vec3 = Vec3(0f, 0f, 0f)
    protected var _previousPosition:Vec3 = Vec3(0f, 0f, 0f)


    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, deltaPosition: Vec2): MoveBy? {
            return create(director, duration, Vec3(deltaPosition.x, deltaPosition.y, 0f))
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, deltaPosition: Vec3): MoveBy? {
            val action:MoveBy = MoveBy(director)
            if (action.initWithDuration(duration, deltaPosition)) {
                return action
            }
            return null
        }
    }

    override fun Clone(): MoveBy? {
        return create(getDirector(), _duration, _positionDelta)
    }

    override fun reverse(): MoveBy? {
        return create(getDirector(), _duration, Vec3(-_positionDelta.x, -_positionDelta.y, -_positionDelta.z))
    }

    protected open fun initWithDuration(duration: Float, deltaPosition: Vec2): Boolean {
        return initWithDuration(duration, Vec3(deltaPosition.x, deltaPosition.y, 0f))
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        val v3 = target!!.getPosition3D()
        _startPosition.set(v3)
        _previousPosition.set(v3)
    }

    override fun update(time: Float) {
        if (_target != null) {
            val currentPos = Vec3(_target!!.getPosition3D())
            val diff = Vec3(currentPos.minus(_previousPosition))
            _startPosition.addLocal(diff)
            val newPos = Vec3(_startPosition.add(_positionDelta.multiply(time)))
            _target!!.setPosition3D(newPos)
            _previousPosition.set(newPos)
        }
    }

    protected open fun initWithDuration(duration: Float, deltaPosition3D: Vec3?): Boolean {
        var ret = false
        if (super.initWithDuration(duration)) {
            _positionDelta.set(deltaPosition3D!!)
            _is3D = true
            ret = true
        }
        return ret
    }

}