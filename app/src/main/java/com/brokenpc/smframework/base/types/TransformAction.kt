package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SMView.Companion.interpolation
import com.brokenpc.smframework.util.tweenfunc

open class TransformAction(director:IDirector) : DelayBaseAction(director) {
    private var _scaleAction = false
    private var _positionXAction = false
    private var _positionYAction = false
    private var _alphaAction = false
    private var _rotateAction = false

    private var _fromScale = 1.0f
    private var _toScale = 1.0f
    private var _fromAlpha = 1.0f
    private var _toAlpha = 1.0f
    private var _fromAngle = 0.0f
    private var _toAngle = 0.0f
    private var _fromPosition = Vec2(0f, 0f)
    private var _toPosition = Vec2(0f, 0f)

    private var _removeOnFinish = false
    private var _enableOnFinish = false
    private var _disableOnFinish = false
    private var _invisibleOnFinish = false

    private var _smooth = false

    private var _tweenType: tweenfunc.TweenType = tweenfunc.TweenType.Circ_EaseOut
    private var _easingParam = 0f

    private var _finishCallback: TransformAction.TransformFunc? = null
    private var _updateCallback: TransformAction.TransformUpdateCallback? = null
    private var _action: Action? = null
    private var _view: SMView? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector): TransformAction {
            val action:TransformAction = TransformAction(director)
            action.initWithDuration(0f)
            return action
        }
    }

    interface TransformFunc {
        fun onFinish(target:SMView?, tag:Int)
    }

    interface TransformUpdateCallback {
        fun onUpdate(target: SMView?, tag: Int, t:Float)
    }

    fun toScale(scale:Float): TransformAction {
        _scaleAction = true
        _toScale = scale
        return this
    }

    fun toPositionX(positionX: Float):TransformAction {
        _positionXAction = true
        _toPosition.set(positionX, 0f)
        return this
    }

    fun toPositoinY(positionY: Float): TransformAction {
        _positionYAction = true
        _toPosition.set(0f, positionY)
        return this
    }

    fun toPosition(position: Vec2): TransformAction {
        _positionXAction = true
        _positionYAction = true
        _toPosition.set(position)
        return this
    }

    fun toAlpha(alpha: Float): TransformAction {
        _alphaAction = true
        _toAlpha = alpha
        return this
    }

    fun removeOnFinish(): TransformAction {
        _removeOnFinish = true
        return this
    }

    fun enableOnFinish(): TransformAction {
        _enableOnFinish = true
        return this
    }

    fun enableSmooth(): TransformAction {
        _smooth = true
        return this
    }

    fun disableOnFinish(): TransformAction {
        _disableOnFinish = true
        return this
    }

    fun invisibleOnFinish(): TransformAction {
        _invisibleOnFinish = true
        return this
    }

    fun runActionOnFinish(action: Action): TransformAction {
        _action = action
        return this
    }

    fun runFuncOnFinish(callback: TransformFunc): TransformAction {
        _finishCallback = callback
        return this
    }

    fun setUpdateCallback(callback: TransformUpdateCallback): TransformAction {
        _updateCallback = callback
        return this
    }

    fun setTweenFunc(type: tweenfunc.TweenType): TransformAction {
        return setTweenFunc(type, 0f)
    }

    fun setTweenFunc(type: tweenfunc.TweenType, easingParam: Float): TransformAction {
        _tweenType = type
        _easingParam = easingParam
        return this
    }

    override fun onStart() {
        if (_scaleAction) {
            _fromScale = _target!!.getScale()
        }
        if (_rotateAction) {
            _fromAngle = _target!!.getRotation()
        }
        if (_positionXAction || _positionYAction) {
            _fromPosition = _target!!.getPosition()
        }
        if (_alphaAction) {
            _fromAlpha = _target!!.getAlpha()
        }
        _target!!.setVisible(true)
        if (_smooth) {
            _view = _target
        }
    }

    override fun onUpdate(dt: Float) {
        var t = dt
        if (_tweenType !== tweenfunc.TweenType.Linear) {
            val easingParam = FloatArray(1)
            easingParam[0] = _easingParam
            t = tweenfunc.tweenTo(t, _tweenType, easingParam)
        }
        if (_scaleAction) {
            val scale = interpolation(_fromScale, _toScale, t)
            if (_view != null) {
                _view!!.setScale(scale, false)
            } else {
                _target!!.setScale(scale)
            }
        }
        if (_rotateAction) {
            val angle = interpolation(_fromScale, _toScale, t)
            if (_view != null) {
                _view!!.setRotation(angle, false)
            } else {
                _target!!.setRotation(angle)
            }
        }
        if (_alphaAction) {
            var alpha = interpolation(_fromAlpha, _toAlpha, t)
            alpha = Math.max(0.0f, Math.min(1.0f, alpha))
            _target!!.setAlpha(alpha)
        }
        if (_positionXAction && _positionYAction) {
            val x = interpolation(_fromPosition.x, _toPosition.x, t)
            val y = interpolation(_fromPosition.y, _toPosition.y, t)
            if (_view != null) {
                _view!!.setPosition(x, y, false)
            } else {
                _target!!.setPosition(x, y)
            }
        } else if (_positionXAction) {
            val x = interpolation(_fromPosition.x, _toPosition.x, t)
            if (_view != null) {
                _view!!.setPositionX(x, false)
            } else {
                _target!!.setPositionX(x)
            }
        } else if (_positionYAction) {
            val y = interpolation(_fromPosition.y, _toPosition.y, t)
            if (_view != null) {
                _view!!.setPositionY(y, false)
            } else {
                _target!!.setPositionY(y)
            }
        }
        if (_updateCallback != null) {
            _updateCallback!!.onUpdate(_target, getTag(), t)
        }
    }

    override fun onEnd() {
        if (_enableOnFinish) {
            val view = _target
            view?.setEnabled(true)
        }
        if (_disableOnFinish) {
            val view = _target
            view?.setEnabled(false)
        }
        if (_invisibleOnFinish) {
            _target!!.setVisible(false)
        }
        if (_removeOnFinish) {
            if (_target!!.getParent() != null) {
                _target!!.removeFromParent()
            }
        }
        if (_finishCallback != null) {
            _finishCallback!!.onFinish(getTarget(), getTag())
        }
        if (_action != null) {
            _target!!.runAction(_action!!)
            _action = null
        }
    }
}