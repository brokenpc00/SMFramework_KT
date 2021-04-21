package com.brokenpc.smframework.base.transition

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.types.Mat4

class SwipeDismiss(director:IDirector) : SwipeBack(director) {
    companion object {
        @JvmStatic
        fun create(director: IDirector, scene:SMScene) : SwipeDismiss? {
            val t:SwipeDismiss = SwipeDismiss(director)

            return if (t.initWithDuration(0f, scene)) t else null
        }
    }

    override fun draw(m: Mat4, flags: Int) {
        val process:Float = (_outScene!!.getY()-getDirector().getWinSize().height/2f) / getDirector().getWinSize().height

        updateProgress(process)

        if (_menuDrawContainer!=null) {
            // exist drawable another menu!!!
        }

        baseTransitionDraw(m, flags)
    }

    override fun onEnter() {
        _outScene?.setPosition(getDirector().getWinSize().width/2f, getDirector().getWinSize().height/2f)
        _inScene?.setPosition(getDirector().getWinSize().width/2f, getDirector().getWinSize().height/2f)
        super.onEnter()
    }

    override fun isNewSceneEnter(): Boolean {
        return false
    }
}