package com.interpark.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.view.SMCircleView
import com.brokenpc.smframework.view.SMShapeView
import com.interpark.smframework.base.types.EaseOut
import com.interpark.smframework.base.types.RepeatForever
import kotlin.math.cos
import kotlin.math.sin

class RingWave(director: IDirector) : SMView(director) {

    protected var _forever: Boolean = false
    protected lateinit var _circle: SMCircleView


    companion object {
        @JvmStatic
        fun show(director: IDirector, parent: SMView?, x: Float, y: Float, size: Float, duration: Float, delay: Float): RingWave {
            return show(director, parent, x, y, size, duration, delay, null)
        }
        @JvmStatic
        fun show(director: IDirector, parent: SMView?, x: Float, y: Float, size: Float, duration: Float, delay: Float, color: Color4F?): RingWave {
            return show(director, parent, x, y, size, duration, delay, color, false)
        }
        @JvmStatic
        fun show(director: IDirector, parent: SMView?, x: Float, y: Float, size: Float, duration: Float, delay: Float, color: Color4F?, forever: Boolean): RingWave {
            val wave = RingWave(director)
            if (wave.initWithParam(size, duration, delay, color, forever)) {
                if (parent!=null) {
                    parent.addChild(wave)
                    wave.setPosition(x, y)
                }
            }

            return wave
        }
    }

    fun setWaveColor(color: Color4F?) {
        if (color!=null) {
            _circle.setLineColor(color)
        }
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

        var action: Action

        val wave = EaseOut.create(getDirector(), WaveCircleActionCreate(getDirector(), duration, _circle, size), 0.2f)!!

        if (_forever) {
            var reqAction:RepeatForever
            if (delay>0f) {
                reqAction = RepeatForever.create(getDirector(), Sequence.create(getDirector(), DelayTime.create(getDirector(), delay), wave, DelayTime.create(getDirector(), 0.1f), null))!!
            } else {
                reqAction = RepeatForever.create(getDirector(), Sequence.create(getDirector(), wave, DelayTime.create(getDirector(), 0.1f), null))!!
            }
            runAction(reqAction)
        } else {
            if (delay>0f) {
                action = Sequence.create(getDirector(), DelayTime.create(getDirector(), delay), wave, null)!!
            } else {
                action = wave
            }
            runAction(action)
        }

        return true
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