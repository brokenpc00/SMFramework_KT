package com.interpark.smframework.base.types

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.GridSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Vec2

class GenieAction(director: IDirector) : ActionInterval(director) {
    private var _removeAnchor = Vec2.ZERO
    private var _sprite:Sprite? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, duration: Float, sprite: Sprite?, removeAnchor: Vec2): GenieAction {
            val action = GenieAction(director)
            action.initWithDuration(duration)
            action._sprite = GridSprite.create(director, sprite)
            action._removeAnchor.set(removeAnchor)

            return action
        }
    }

    override fun startWithTarget(target: SMView?) {
        super.startWithTarget(target)
        (_sprite as GridSprite).setGenieAnchor(_removeAnchor)
    }

    override fun update(dt: Float) {
        var t = dt
        if (t<0f) t *= 0.1f

        (_sprite as GridSprite).setGenieProgress((SMView.M_PI_2*t).toFloat())
    }
}