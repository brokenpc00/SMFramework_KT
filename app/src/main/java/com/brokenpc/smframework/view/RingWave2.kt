package com.interpark.smframework.view

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.tweenfunc
import com.brokenpc.smframework.view.SMCircleView
import com.interpark.smframework.base.types.FadeIn
import com.interpark.smframework.base.types.RepeatForever

class RingWave2(director: IDirector): SMView(director) {
    var _circle:SMCircleView? = null

    companion object {
        @JvmStatic
        fun create(director: IDirector, minRadius: Float, maxRadius: Float): RingWave2 {
            return create(director, minRadius, maxRadius, 0f)
        }

        @JvmStatic
        fun create(director: IDirector, minRadius: Float, maxRadius: Float, startDelay: Float): RingWave2 {
            val wave = RingWave2(director)
            wave.initWithParam(minRadius, maxRadius, startDelay)
            return wave
        }
    }

    fun hide() {
        val action = TransformAction.create(getDirector())
        action.toAlpha(0f).removeOnFinish()
        action.setTimeValue(0.5f, 0f)
        runAction(action)
    }

    fun initWithParam(minRadius: Float, maxRadius: Float, startDelay: Float): Boolean {
        setAnchorPoint(Vec2.MIDDLE)
        setCascadeColorEnabled(true)

        _circle = SMCircleView.create(getDirector())
        _circle!!.setAnchorPoint(Vec2.MIDDLE)
        addChild(_circle!!)

        val reqAction = RepeatForever.create(getDirector(), Sequence.create(getDirector(), WaveActionCreate(getDirector(), 0.6f, minRadius, maxRadius), DelayTime.create(getDirector(), 0.1f), null))
        if (startDelay>0) {
            val seqAction = Sequence.create(getDirector(), DelayTime.create(getDirector(), startDelay), CallFunc.create(getDirector(), object : PERFORM_SEL{
                override fun performSelector() {
                    _circle!!.runAction(reqAction!!)
                }
            }), null)
            _circle!!.runAction(seqAction!!)
        } else {
            _circle!!.runAction(reqAction!!)
        }
        runAction(FadeIn.create(getDirector(), 0.2f))
        return true
    }

    fun WaveActionCreate(director: IDirector, duration: Float, minRadius: Float, maxRadius: Float): WaveAciton {
        val wave = WaveAciton(director)
        wave.initWithDuration(duration)
        wave._minRadius = minRadius
        wave._maxRadius = maxRadius
        return wave
    }

    class WaveAciton(director: IDirector): ActionInterval(director) {
        var _minRadius = 0f
        var _maxRadius = 0f

        override fun update(dt: Float) {
            val ring = _target as SMCircleView

            val d = _maxRadius - _minRadius
            val outR = _minRadius + d * tweenfunc.cubicEaseOut(dt)
            val inR = _minRadius + d * tweenfunc.cubicEaseIn(dt)

            ring.setContentSize(Size(outR*2f, outR*2f))
            ring.setLineWidth(outR-inR)

            ring.setAlpha(1f - tweenfunc.sineEaseIn(dt))
        }
    }
}