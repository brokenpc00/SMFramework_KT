package com.brokenpc.smframework.base.transition

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.view.SMSolidRectView

class SlideOutToBottom(director:IDirector) : BaseSceneTransition(director) {

    companion object {
        @JvmStatic
        fun create(director: IDirector, t: Float, inScene: SMScene): SlideOutToBottom? {
            val scene:SlideOutToBottom = SlideOutToBottom(director)
            if (scene.initWithDuration(t, inScene)) {
                return scene
            }

            return null
        }
    }

    override fun draw(m: Mat4, flags: Int) {
        if (isDimLayerEnable() && _lastProgress > 0 && _dimLayer == null) {
            _dimLayer = SMSolidRectView(getDirector())
            _dimLayer!!.setContentSize(Size(getDirector().getWidth(), getDirector().getHeight()))
            _dimLayer!!.setAnchorPoint(Vec2(0.5f, 0.5f))
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
            if (_lastProgress > 0.0f && _lastProgress < 1.0f && _dimLayer != null) {
            _outScene!!.visit(m, flags)
                val alpha = 0.4f * _lastProgress
                _dimLayer!!.setColor(0f, 0f, 0f, alpha)
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

    override fun getOutAction(): FiniteTimeAction? {
        return EaseSineOut.create(
            getDirector(),
            MoveTo.create(
                getDirector(),
                _duration,
                Vec2(
                    getDirector().getWinSize().width / 2,
                    getDirector().getWinSize().height + getDirector().getWinSize().height / 2
                )
            )!!
        )
    }

    override fun isNewSceneEnter(): Boolean {
        return false
    }

    override fun sceneOrder() {
        _isInSceneOnTop = false
    }
}