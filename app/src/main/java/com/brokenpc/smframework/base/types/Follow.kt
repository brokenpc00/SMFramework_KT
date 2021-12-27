package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Action
import com.brokenpc.smframework.base.types.Rect
import com.brokenpc.smframework.base.types.Size
import com.brokenpc.smframework.base.types.Vec2

open class Follow(director: IDirector): Action(director) {
    protected var _followView:SMView? = null
    protected var _boundarySet = false
    protected var _boundaryFullyCovered = false
    protected var _halfScreenSize = Vec2(Vec2.ZERO)
    protected var _fullScreenSize = Vec2(Vec2.ZERO)
    protected var _leftBoundary = 0f
    protected var _rightBoundary = 0f
    protected var _topBoundary = 0f
    protected var _bottomBoundary = 0f
    protected var _offsetX = 0f
    protected var _offsetY = 0f
    protected var _worldRect = Rect(Rect.ZERO)

    companion object {
        @JvmStatic
        fun createWithOffset(director: IDirector, followedView: SMView?, xOffset: Float, yOffseet: Float, rect: Rect): Follow? {
            val action = Follow(director)
            if (action.initWithTargetAndOffset(followedView, xOffset, yOffseet, rect)) {
                return action
            }
            return null
        }
    }

    fun initWithTargetAndOffset(followedView: SMView?, xOffset: Float, yOffseet: Float, rect: Rect): Boolean {
        _followView = followedView
        _worldRect = rect
        _boundarySet = !rect.equal(Rect.ZERO)
        _boundaryFullyCovered = false

        _fullScreenSize.set(Size(getDirector().getWidth(), getDirector().getHeight()))
        _halfScreenSize = Vec2(_fullScreenSize.x / 2f, _fullScreenSize.y / 2f)
        _offsetX = xOffset
        _offsetY = yOffseet
        _halfScreenSize.x += _offsetX
        _halfScreenSize.y += _offsetY

        if (_boundarySet) {
            _leftBoundary = -(rect.origin.x + rect.size.width - _fullScreenSize.x)
            _rightBoundary = -rect.origin.x
            _topBoundary = -rect.origin.y
            _bottomBoundary = -(rect.origin.y + rect.size.height - _fullScreenSize.y)

            if(_rightBoundary < _leftBoundary) {
                _leftBoundary = ((_leftBoundary+_rightBoundary)/2f).also { _rightBoundary = it }
            }

            if(_topBoundary < _bottomBoundary) {
                _bottomBoundary = ((_topBoundary+_bottomBoundary)/2f).also { _topBoundary = it }
            }

            if (_topBoundary==_bottomBoundary && _leftBoundary==_rightBoundary) {
                _boundaryFullyCovered = true
            }
        }

        return true
    }

    override fun Clone(): Action? {
        return Follow.createWithOffset(getDirector(), _followView, _offsetX, _offsetY, _worldRect)
    }

    override fun reverse(): Action? {
        return Clone()
    }

    override fun step(dt: Float) {
        if (_boundarySet) {
            if (_boundaryFullyCovered) return

            val tempPos = Vec2(_halfScreenSize.x-(_followView?.getX()?:0f), _halfScreenSize.y-(_followView?.getY()?:0f))
            _target?.setPosition(Vec2.clampf(tempPos.x, _leftBoundary, _rightBoundary), Vec2.clampf(tempPos.y, _bottomBoundary, _topBoundary))
        } else {
            _target?.setPosition(_halfScreenSize.x-(_followView?.getX()?:0f), _halfScreenSize.y-(_followView?.getY()?:0f))
        }
    }

    override fun isDone(): Boolean {
        return !(_followView?.isRunning()?:false)
    }

    override fun stop() {
        _target = null
        super.stop()
    }
}