package com.interpark.smframework.base.types

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMView

open class MoveTo(director:IDirector) : MoveBy(director) {

    protected var _endPosition:Vec3 = Vec3(0f, 0f, 0f)

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, position: Vec2):MoveTo? {
            return create(director, duration, Vec3(position.x, position.y, 0f))
        }

        @JvmStatic
        fun create(director: IDirector, duration: Float, position: Vec3):MoveTo? {
            val action:MoveTo = MoveTo(director)
            if (action.initWithDuration(duration, position)) {
                return action
            }

            return null
        }
    }

    override fun Clone(): MoveTo? {
        return create(getDirector(), _duration, _endPosition)
    }

    override fun reverse(): MoveTo? {
        // no reverse
        return null
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        _positionDelta.set(_endPosition.minus(target!!.getPosition3D()))
    }

    override fun initWithDuration(duration: Float, position: Vec2): Boolean {
        return initWithDuration(duration, Vec3(position.x, position.y, 0f))
    }

    override fun initWithDuration(duration: Float, position3D: Vec3?): Boolean {
        var ret = false
        if (super.initWithDuration(duration)) {
            _endPosition.set(position3D!!)
            ret = true
        }
        return ret
    }

}