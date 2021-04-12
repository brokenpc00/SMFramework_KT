package com.brokenpc.smframework.base.transition

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.types.EaseCubicActionOut
import com.brokenpc.smframework.base.types.FiniteTimeAction
import com.brokenpc.smframework.base.types.MoveTo
import com.brokenpc.smframework.base.types.Vec2

class SlideInToTop(director:IDirector) : BaseSceneTransition(director) {

    companion object {
        @JvmStatic
        fun create(director: IDirector, t:Float, inScene:SMScene):SlideInToTop? {
            val scene:SlideInToTop = SlideInToTop(director)
            if (scene.initWithDuration(t, inScene)) {
                return scene
            }

            return null
        }
    }

    override fun getInAction(): FiniteTimeAction? {
        _inScene!!.setPosition(getDirector().getWinSize().width / 2, getDirector().getWinSize().height + getDirector().getWinSize().height / 2)

        return EaseCubicActionOut.create(getDirector(), MoveTo.create(getDirector(), _duration, Vec2(getDirector().getWinSize().width/2f, getDirector().getWinSize().height/2f))!!)
    }

    override fun isNewSceneEnter(): Boolean {
        return true
    }

    override fun sceneOrder() {
        _isInSceneOnTop = true
    }
}