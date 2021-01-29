package com.brokenpc.smframework.base.transition

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.types.FiniteTimeAction
import com.brokenpc.smframework.base.types.TransformAction
import com.brokenpc.smframework.base.types.tweenfunc

class SlideInToLeft(director:IDirector) : BaseSceneTransition(director) {

    companion object {
        @JvmStatic
        fun create(director: IDirector, t:Float, inScene:SMScene):SlideInToLeft? {
            val scene:SlideInToLeft = SlideInToLeft(director)
            if (scene.initWithDuration(t, inScene)) {
                return scene
            }

            return null
        }
    }

    override fun getInAction(): FiniteTimeAction? {
        _inScene?.setPosition(getDirector().getWinSize().width+getDirector().getWinSize().width/2, getDirector().getWinSize().height/2)

        val action: TransformAction = TransformAction.create(getDirector())
        action.toPositionX(getDirector().getWidth()/2f).setTweenFunc(tweenfunc.TweenType.Cubic_EaseOut).setTimeValue(_duration, 0f)

        return action
    }

    override fun getOutAction(): FiniteTimeAction {
        val action:TransformAction = TransformAction.create(getDirector())
        action.toPositionX(-getDirector().getWinSize().width*0.3f+getDirector().getWidth()/2f).setTimeValue(_duration, 0f)
        return action
    }

    override fun isNewSceneEnter(): Boolean {
        return true
    }

    override fun sceneOrder() {
        _isInSceneOnTop = true
    }

}