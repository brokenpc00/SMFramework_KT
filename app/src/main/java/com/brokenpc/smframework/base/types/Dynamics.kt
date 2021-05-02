package com.brokenpc.smframework.base.types

import kotlin.math.abs

class Dynamics {
    constructor() {
        reset()
    }

    private val MAX_TIMESTEP = 1.0f / 60.0f

    private var _position = 0.0f

    private var _velocity = 0.0f

    private var _maxPosition = Float.MAX_VALUE

    private var _minPosition = Float.MIN_VALUE

    private var _lastTime = 0.0f

    private var _friction = 0.0f

    private var _stiffness = 0.0f

    private var _damping = 0.0f

    fun reset() {
        _position = 0f
        _velocity = 0f
        _maxPosition = Float.MAX_VALUE
        _minPosition = Float.MIN_VALUE
        _lastTime = 0f
        _friction = 0f
        _stiffness = 0f
        _damping = 0f
    }

    fun setState(
        position: Float,
        velocity: Float,
        nowTime: Float
    ) {
        _velocity = velocity
        _position = position
        _lastTime = nowTime
    }

    fun getPosition(): Float {
        return _position
    }

    fun isAtRest(
        velocityTolerance: Float,
        positionTolerance: Float
    ): Boolean {
        return isAtRest(velocityTolerance, positionTolerance, 1.0f)
    }

    fun isAtRest(
        velocityTolerance: Float,
        positionTolerance: Float,
        range: Float
    ): Boolean {
        val standingStill = abs(_velocity) < velocityTolerance
//        val withinLimits = if (range == 1f) {
//            _position - positionTolerance < _maxPosition && _position + positionTolerance > _minPosition
//        } else {
//            _position * range - positionTolerance < _maxPosition * range && _position * range + positionTolerance > _minPosition * range
//        }
        val withinLimits = ((_position*range - positionTolerance < _maxPosition*range)&&(_position*range + positionTolerance > _minPosition*range))
        return standingStill && withinLimits
    }

    fun setMaxPosition(maxPosition: Float) {
        _maxPosition = maxPosition
    }

    fun setMinPosition(minPosition: Float) {
        _minPosition = minPosition
    }

    fun update(now: Float) {
        var dt = now - _lastTime
        if (dt > MAX_TIMESTEP) {
            dt = MAX_TIMESTEP
        }

        // Calculate current acceleration
        val acceleration = calculateAcceleration()

        // Calculate next position based on current velocity and acceleration
        _position += _velocity * dt + .5f * acceleration * dt * dt

        // Update velocity
        _velocity += acceleration * dt
        _lastTime = now
    }

    fun getDistanceToLimit(): Float {
        var distanceToLimit = 0f
        if (_position > _maxPosition) {
            distanceToLimit = _maxPosition - _position
        } else if (_position < _minPosition) {
            distanceToLimit = _minPosition - _position
        }
        return distanceToLimit
    }

    fun setFriction(friction: Float) {
        _friction = friction
    }

    fun setSpring(stiffness: Float, dampingRatio: Float) {
        _stiffness = stiffness
        _damping = dampingRatio * 2 * Math.sqrt(stiffness.toDouble()).toFloat()
    }

    fun calculateAcceleration(): Float {
        val acceleration: Float
        val distanceFromLimit = getDistanceToLimit()
        acceleration = if (distanceFromLimit != 0.0f) {
            distanceFromLimit * _stiffness - _damping * _velocity
        } else {
            -_friction * _velocity
        }
        return acceleration
    }
}