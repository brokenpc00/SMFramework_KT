package com.interpark.smframework.base.transition

import com.interpark.smframework.IDirector
import com.interpark.smframework.base.SMScene
import com.interpark.smframework.base.types.*
import com.interpark.smframework.base.types.tweenfunc.TweenType
import com.interpark.smframework.view.SMSolidRectView

class SlideOutToRight(director:IDirector) : BaseSceneTransition(director) {

    companion object {
        @JvmStatic
        fun create(director: IDirector, t: Float, inScene: SMScene): SlideOutToRight? {
            val scene: SlideOutToRight = SlideOutToRight(director)
            if (scene.initWithDuration(t,inScene)) {
                return scene
            }

            return null
        }
    }

    override fun getInAction(): FiniteTimeAction? {
        _inScene!!.setPosition(
            -getDirector().getWinSize().width * 0.3f + getDirector().getWinSize().width / 2,
            getDirector().getWinSize().height / 2
        )
        val action = TransformAction.create(getDirector())
        action.toPositionX(getDirector().getWinSize().width / 2).setTimeValue(_duration, 0f)
        return action
    }

    override fun getOutAction(): FiniteTimeAction? {
        val action = TransformAction.create(getDirector())
        action.toPositionX(getDirector().getWinSize().width + getDirector().getWinSize().width / 2)
            .setTweenFunc(TweenType.Sine_EaseOut).setTimeValue(_duration, 0f)
        return action
    }

    override fun draw(m: Mat4, flags: Int) {
        if (isDimLayerEnable() && _lastProgress > 0 && _dimLayer == null) {
            _dimLayer = SMSolidRectView(getDirector())
            _dimLayer!!.setContentSize(Size(getDirector().getWidth(), getDirector().getHeight()))
            _dimLayer!!.setAnchorPoint(Vec2.MIDDLE)
            _dimLayer!!.setPosition(
                Vec2(
                    getDirector().getWinSize().width / 2,
                    getDirector().getWinSize().height / 2
                )
            )
            _dimLayer!!.setColor(Color4F.TRANSPARENT)
        }
        if (_isInSceneOnTop) {
            // new scene entered!!
            _outScene!!.visit(m, flags)
            if (_lastProgress > 0.0f && _lastProgress < 1.0f && _dimLayer != null) {
                val alpha = 0.4f * _lastProgress
                _dimLayer!!.setColor(Color4F(0f, 0f, 0f, alpha))
                _dimLayer!!.visit(m, flags)
            }
            _inScene!!.visit(m, flags)
        } else {
            // top scene exist
            val minusScale = 0.6f * _lastProgress
            _inScene!!.setScale(1.6f - minusScale)
            _inScene!!.visit(m, flags)
            if (_lastProgress > 0.0f && _lastProgress < 1.0f && _dimLayer != null) {
                _dimLayer!!.setColor(Color4F(0f, 0f, 0f, 0.4f * (1.0f - _lastProgress)))
                _dimLayer!!.visit(m, flags)
            }
            _outScene!!.visit(m, flags)
        }
    }

    override fun isNewSceneEnter(): Boolean {
        return false
    }

    override fun sceneOrder() {
        _isInSceneOnTop = false
    }
}