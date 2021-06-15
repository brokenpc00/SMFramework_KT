package com.brokenpc.smframework_kt.scene

import androidx.appcompat.widget.ViewUtils
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.util.AppConst
import com.brokenpc.smframework.view.Popup
import com.interpark.smframework.base.types.FadeTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

class AniTestView(director: IDirector, listener: AniTestListener?) : Popup(director) {

    private var _storeLogo:Sprite? = null
    private var _logoScale = 1f

    private var _introLogo: LogoSprite? = null
    private var _introText: Sprite? = null

    private var _startTime = 0L
    private var _framCount = 0

    companion object {
        const val ANI_DURATION_TIME = 2.7f
        const val GRAVITY_VALUE = 9.8f
        const val STATE_TIME_WAIT = 500L
        const val STATE_TIME_CUT = 1500L
        const val SCISSOR_TIME = 180f

        @JvmStatic
        fun show(director: IDirector, scene: SMScene): AniTestView {
            val view = AniTestView(director)
            director.openPopupView(view, 0f, true)
            return view
        }
    }

    interface AniTestListener {
        fun onAniEnd()
    }

    inner class Particle {
        var x = 0f
        var y = 0f
        var ay = 0f
        var vx = 0f
        var vy = 0f
        var start = 0L
    }

    private val _particle: ArrayList<Particle> = ArrayList()

    private var _listener: AniTestListener? = null

    init {
        _listener = listener

        val white = BitmapSprite.createFromAsset(getDirector(), "images/intro_logo02.png", false, null)
        val black = BitmapSprite.createFromAsset(getDirector(), "images/intro_logo01.png", false, null)
        _introLogo = LogoSprite(getDirector(), white!!.getTexture()!!, black!!.getTexture()!!)
        _introText = BitmapSprite.createFromAsset(getDirector(), "images/intro_logo_text.png", false, null)

        setBackgroundColor(MakeColor4F(0xffD339, 1f))
        val fadeTo = FadeTo.create(getDirector(), 0.1f, 1f)
        runAction(fadeTo)
    }

    override fun init(): Boolean {
        if (!super.init()) {
            return false
        }

        val s = getDirector().getWinSize()

        setPosition(Vec2.ZERO)
        setContentSize(s)
        _startTime = getDirector().getTickCount()

        val moveTo = MoveTo.create(getDirector(), 0.4f, Vec2(0f, s.height+100f))
        val sequence = Sequence.create(getDirector(), moveTo, CallFunc.create(getDirector(), object : PERFORM_SEL{
            override fun performSelector() {
                if (_listener!=null) {
                    _listener!!.onAniEnd()
                }
            }
        }), null)

        runAction(sequence!!)

        return true
    }

    override fun onExit() {
        super.onExit()

        _introLogo?.removeTexture()
        _introLogo = null
        _introText?.removeTexture()
        _introText = null
        _storeLogo?.removeTexture()
        _storeLogo = null
    }

    override fun visit(parentTransform: Mat4, parentFlags: Int) {
        var x = getContentSize().width/2f
        var y = 0f
        var width = _introLogo?.getWidth() ?: 0f
        var height = _introLogo?.getHeight() ?: 0f
        val a = getColor().a

        // store 로고
        if (_storeLogo!=null) {
            getDirector().setColor(a, a, a, a)
            y = getContentSize().height - 120f
            _storeLogo!!.drawScale(x-_storeLogo!!.getWidth()/2f*_logoScale, y-_storeLogo!!.getHeight()/2f, _logoScale)
        }

        y = getContentSize().height * 0.35f

        getDirector().setColor(a, a, a, a)
        _introText!!.draw(x-_introText!!.getWidth()/2f, y+100f)

        var time = (getDirector().getTickCount() - _startTime)

        if (time < STATE_TIME_WAIT) {
            _introLogo!!.transform(0f, 0.8f)
            _introLogo!!.setDrawFront()
            getDirector().setColor(a, a, a, a)
            _introLogo!!.draw(x-width/2f, y-height/2f)
            return
        }

        time -= STATE_TIME_WAIT

        if (time< STATE_TIME_CUT) {
            val f = (time % STATE_TIME_CUT).toFloat()/ STATE_TIME_CUT
            _introLogo!!.transform((1.5f*(1- cos(f*PI/2))).toFloat(), 0.7f*0.325f*f)

            _introLogo!!.setDrawBack()
            getDirector().setColor(a, a, a, a)
            _introLogo!!.draw(x-width/2f, y-height/2)

            var cl = a
            if (f>0.7f) {
                cl = a * (1-f) / 0.3f
            }
            _introLogo!!.setDrawFront()
            getDirector().setColor(cl, cl, cl, cl)
            _introLogo!!.draw(x-width/2f, y-height-2f)

            val fromX = x + width/2f
            val toX = x - width/2f
            val xx = 5f + interpolation(fromX, toX, 1.4f*(1 - cos(PI/2).toFloat()))
            val yy = y - height/2f
            if (xx > toX-10f) {
                getDirector().setColor(a, a, a, a)

            }
        }

    }

    fun drawScissor(x: Float, y: Float) {
        val time = getDirector().getTickCount()

        val scissorFactor = (time% SCISSOR_TIME) / SCISSOR_TIME
        val scissorAngle = abs(35f* cos(scissorFactor*PI)).toFloat()+5f
        getDirector().getSpriteSet()!!?.get(SR.intro_scissor)
    }
}