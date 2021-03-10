package com.interpark.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Action
import com.brokenpc.smframework.base.types.ActionInterval
import com.brokenpc.smframework.base.types.Color4F
import com.brokenpc.smframework.base.types.Vec2
import com.brokenpc.smframework.view.SMCircleView
import com.brokenpc.smframework.view.SMShapeView
import kotlin.math.cos
import kotlin.math.sin

class RingWave(director: IDirector) : SMView(director) {

    protected var _forever: Boolean = false
    protected lateinit var _circle: SMCircleView


    companion object {

    }

    fun initWithParam(size: Float, duration: Float, delay: Float, color: Color4F?, forever: Boolean): Boolean {
        setAnchorPoint(Vec2.MIDDLE)

        _forever = forever
        _circle = SMCircleView.create(getDirector())

        addChild(_circle)

        _circle.setAnchorPoint(Vec2.MIDDLE)
        _circle.setPosition(Vec2.ZERO)

        if (color!=null) {
            _circle.setLineColor(color)
        }
        _circle.setAlpha(0f)

//        val wave = EaseOut

        val action: Action


    }

    fun WaveCircleActionCreate(director: IDirector, duration: Float, shape: SMShapeView, size: Float): WaveCircleAction {
        val action = WaveCircleAction(director)
        action.initWithDuration(duration)
        action.setForever(_forever)
        action.setShape(shape)
        action.setSize(size)
        return action
    }

    class WaveCircleAction(director: IDirector) : ActionInterval(director) {
        private var _forever: Boolean = false
        private var _shape:SMShapeView? = null
        private var _size: Float = 0f
        override fun update(t: Float) {
            val r1 = (_size * sin(t* M_PI/2f)).toFloat()
            val r2 = (_size * (1f- cos(t * M_PI / 2f))).toFloat()

            val d = r2-r1
            val a = sin(t * M_PI).toFloat()

            _shape?.setAlpha(0.7f * a)
            _shape?.setContentSize(r1, r2)
            _shape?.setLineWidth(d / 4f)

            if (!_forever) {
                if (t >= 1) {
                    _target?.removeFromParentAndCleanup(true)
                }
            }
        }

        fun setForever(forever: Boolean) {_forever=forever}
        fun setShape(shape: SMShapeView) {_shape = shape}
        fun setSize(size: Float) {_size = size}
    }
}