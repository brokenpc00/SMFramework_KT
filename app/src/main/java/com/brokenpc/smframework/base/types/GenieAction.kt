package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.GridSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.shader.ShaderManager

class GenieAction(director: IDirector) : ActionInterval(director) {
    private var _removeAnchor = Vec2(Vec2.ZERO)
    private var _sprite:GridSprite? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, sprite: Sprite?, removeAnchor: Vec2): GenieAction {
            val action = GenieAction(director)
            action.initWithDuration(duration)
            action._sprite = GridSprite.create(director, sprite)
            action._sprite!!.setProgramType(ShaderManager.ProgramType.GeineEffect2)
            action._removeAnchor.set(removeAnchor)

            return action
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        _sprite!!.setGenieAnchor(_removeAnchor)
    }

    override fun update(dt: Float) {
        var t = dt
        if (t<0f) t *= 0.1f
        _sprite!!.setGenieProgress((SMView.M_PI_2*t).toFloat())
    }
}