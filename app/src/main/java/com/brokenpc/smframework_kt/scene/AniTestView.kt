package com.brokenpc.smframework_kt.scene

import android.view.MotionEvent
import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.sprite.BitmapSprite
import com.brokenpc.smframework.base.sprite.Sprite
import com.brokenpc.smframework.base.texture.Texture
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.view.Popup
import com.brokenpc.smframework.view.SMImageView
import com.brokenpc.smframework_kt.R
import com.brokenpc.smframework.base.types.FadeTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos

class AniTestView(director: IDirector, listener: AniTestListener?) : Popup(director) {

    private var _logoScale = 1f

    private var _introLogo: LogoSprite? = null
    private var _introText: Sprite? = null
    private var _introLogoImg: SMImageView? = null
    private var _introTextImg: SMImageView? = null

    private var _startTime = 0L
    private var _framCount = 0

    private var _scissor1: Sprite? = null
    private var _scissor2: Sprite? = null

    companion object {
        const val ANI_DURATION_TIME = 2.7f
        const val GRAVITY_VALUE = 9.8f
//        const val STATE_TIME_WAIT = 500L
//        const val STATE_TIME_CUT = 1500L
//        const val SCISSOR_TIME = 180f
//        const val PARTICLE_LIFE = 500L
        const val STATE_TIME_WAIT = 100L
        const val STATE_TIME_CUT = 2400L
        const val SCISSOR_TIME = 300f
        const val PARTICLE_LIFE = 500L

        @JvmStatic
        fun show(director: IDirector, scene: SMScene): AniTestView {
            val view = AniTestView(director, null)
            view.init()
            director.openPopupView(view, 0f, true)
            return view
        }

        @JvmStatic
        fun create(director: IDirector, x: Float, y: Float, width: Float, height: Float, listener: AniTestListener?): AniTestView {
            val view = AniTestView(director, listener)
            view.setContentSize(width, height)
            view.setPosition(x, y)
            view.setAnchorPoint(Vec2.ZERO)
            view.init()
            return view
        }

        @JvmStatic
        fun randf(f: Float): Float {return (Math.random()*f).toFloat()}
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

    private var _listener: AniTestListener? = listener

    private var _testSprite:Sprite? = null

    override fun init(): Boolean {
        if (!super.init()) {
            return false
        }

        val s = getContentSize()

//        val contentView = SMView.create(getDirector(), 0f, 0f, s.width, s.height)
//        addChild(contentView)
//        contentView.setBackgroundColor(Color4F(1f, 1f, 0f, 0.3f))
        val white = BitmapSprite.createFromAsset(getDirector(), "images/intro_logo02.png", false, null)
        val black = BitmapSprite.createFromAsset(getDirector(), "images/intro_logo01.png", false, null)
        _introLogo = LogoSprite(getDirector(), white!!.getTexture()!!, black!!.getTexture()!!)

        val tex = getDirector().getTextureManager().createTextureFromResource(R.raw.intro_logo01)
//
        _testSprite = Sprite(getDirector(), tex, tex.getWidth()/2f, tex.getHeight()/2f)

//        _introLogoImg = SMImageView.create(getDirector(), _introLogo!!)


        _introText = BitmapSprite.createFromAsset(getDirector(), "images/intro_logo_text.png", false, null)
//
//        _introLogoImg = SMImageView.create(getDirector(), _introLogo!!)
//        _introTextImg = SMImageView.create(getDirector(), _introText!!)
//        contentView.addChild(_introLogoImg)
////
//        val x = getContentSize().width/2f
//        val y = getContentSize().height * 0.35f
////        _introLogoImg!!.setContentSize(_introLogo!!.getWidth(), _introLogo!!.getHeight())
//        _introLogoImg!!.setContentSize(_introLogo!!.getContentSize())
//        _introLogoImg!!.setPosition(x-_introLogoImg!!.getContentSize().width/2f, y- _introLogoImg!!.getContentSize().height /2f)
////
//        return true

//
////        setBackgroundColor(MakeColor4F(0xffD339, 1f))
//
//        _startTime = getDirector().getTickCount()
//
////        val s = getDirector().getWinSize()
////
//////        setPosition(Vec2.ZERO)
//////        setContentSize(s)
////
////        val moveTo = MoveTo.create(getDirector(), 0.4f, Vec2(0f, s.height+100f))
////        val sequence = Sequence.create(getDirector(), moveTo, CallFunc.create(getDirector(), object : PERFORM_SEL{
////            override fun performSelector() {
////                if (_listener!=null) {
////                    _listener!!.onAniEnd()
////                }
////            }
////        }), null)
////
//////        runAction(sequence!!)
//
        return true
    }

    override fun onExit() {
        super.onExit()

        _introLogo?.removeTexture()
        _introLogo = null
        _introText?.removeTexture()
        _introText = null

        _scissor1?.removeTexture()
        _scissor2?.removeTexture()
        }

    // update flag가 갱신 되어야만 호출됨.
    override fun onUpdateOnVisit() {
//        var x = getContentSize().width/2f
//        var y = getContentSize().height/2f
////        var width = _introLogo?.getWidth() ?: 0f
////        var height = _introLogo?.getHeight() ?: 0f
//////        _introLogo?.transform(0f, 0.8f)
//////        _introLogo?.setDrawFront()
//////        _introLogo?.draw(x-width/2f, y-height/2f)
////        _introLogo?.transform(2f, 0.8f)
////        _introLogo?.setDrawBack()
////        getDirector().setColor(1f, 1f, 1f,1f)
//////        getDirector().setColor(_displayedColor.r, _displayedColor.g, _displayedColor.b, _displayedColor.a)
////        _introLogo?.draw(x-_introLogo!!.getWidth()/2f, y-_introLogo!!.getHeight()/2f)
//
//        getDirector().setColor(0f, 0f, 0f, 1f)
////        _testSprite?.draw(x, y)
//        _introText!!.draw(x-_introText!!.getWidth()/2f, y+100f)
    }

    override fun draw(m: Mat4, flags: Int) {
        super.draw(m, flags)
        var x = getContentSize().width/2f
        var y = getContentSize().height/2f
        var width = _introLogo!!.getWidth()
        var height = _introLogo!!.getHeight()
//        getDirector().setColor(0f, 0f, 0f, 1f)
//        _introText!!.draw(x-_introText!!.getWidth()/2f, y+100f)
        getDirector().setColor(1f, 1f, 1f,1f)
        _testSprite?.draw(x, y-400f)
        getDirector().setColor(0f, 0f, 0f,1f)
        _introLogo!!.transform(2f, 0.8f)
        _introLogo!!.setDrawBack()
        _introLogo!!.draw(x-width/2f, y-height/2f)
    }

//    override fun visit(parentTransform: Mat4, parentFlags: Int) {
//        var x = getContentSize().width/2f
//        var y = 0f
//        var width = _introLogo?.getWidth() ?: 0f
//        var height = _introLogo?.getHeight() ?: 0f
//        val a = getColor().a
//
//        y = getContentSize().height * 0.35f
//
////        getDirector().setColor(a, a, a, a)
//        _introText!!.draw(x-_introText!!.getWidth()/2f, y+100f)
//
//        var time = (getDirector().getTickCount() - _startTime)
//
//        if (time < STATE_TIME_WAIT) {
//            _introLogo!!.transform(0f, 0.8f)
//            _introLogo!!.setDrawFront()
////            getDirector().setColor(a, a, a, a)
//            _introLogo!!.draw(x-width/2f, y-height/2f)
//            return
//        }
//
//        time -= STATE_TIME_WAIT
//
//        if (time< STATE_TIME_CUT) {
//            val f = (time % STATE_TIME_CUT).toFloat()/ STATE_TIME_CUT
//            _introLogo!!.transform((1.5f*(1- cos(f*PI/2))).toFloat(), 0.7f*0.325f*f)
//
//            _introLogo!!.setDrawBack()
////            getDirector().setColor(a, a, a, a)
//            _introLogo!!.draw(x-width/2f, y-height/2)
//
//            var cl = a
//            if (f>0.7f) {
//                cl = a * (1-f) / 0.3f
//            }
//            _introLogo!!.setDrawFront()
////            getDirector().setColor(cl, cl, cl, cl)
//            _introLogo!!.draw(x-width/2f, y-height-2f)
//
//            val fromX = x + width/2f
//            val toX = x - width/2f
//            val xx = 5f + interpolation(fromX, toX, 1.4f*(1.0-cos(f*Math.PI/2)).toFloat());
//            val yy = y - height/2f
//            if (xx > toX-10f) {
////                getDirector().setColor(a, a, a, a)
//                drawScissor(xx, y-height/2f)
//
//                if (_framCount%5 == 0 || (Math.random()*20).toInt()==0) {
//                    addParticle(xx, yy)
//                }
//            }
//            _framCount++
//            drawParticle()
//            return
//        }
//
//        _introLogo?.transform(2f, 0.8f)
//        _introLogo?.setDrawBack()
////        getDirector().setColor(a, a, a, a)
//        _introLogo?.draw(x-_introLogo!!.getWidth()/2f, y-_introLogo!!.getHeight()/2f)
//
//        drawParticle()
//    }

    fun drawScissor(x: Float, y: Float) {
        val time = getDirector().getTickCount()

        val scissorFactor = (time% SCISSOR_TIME) / SCISSOR_TIME
        val scissorAngle = abs(35f* cos(scissorFactor*PI)).toFloat()+5f
        val tex1 = getDirector().getTextureManager().createTextureFromResource(R.raw.intro_scissor)
        val tex2 = getDirector().getTextureManager().createTextureFromResource(R.raw.intro_scissor)
        if (_scissor1==null) {
            _scissor1 = Sprite(getDirector(), tex1, tex1.getWidth()/2f-7f, tex1.getHeight()/2f)
        }
        if (_scissor2==null) {
            _scissor2 = Sprite(getDirector(), tex2, tex2.getWidth()/2f-7f, tex2.getHeight()/2f)
        }
        _scissor1?.drawScaleXYRotate(x, y-tex1.getHeight()-32, 3f, -3f, -scissorAngle)
        _scissor2?.drawScaleXYRotate(x, y, 3f, 3f, scissorAngle)
    }

    fun addParticle(x: Float, y: Float) {
        val node = Particle()
        node.x = x + randf(10f)
        node.y = y + randf(5f)
        node.ay = 100 + randf(50f)
        node.vx = 50 + randf(50f)
        node.vy = -300 - randf(100f)
        node.start = getDirector().getTickCount()

        _particle.add(node)
    }

    fun drawParticle() {
        val nowTime = getDirector().getTickCount()

        var ftime = 0f
        var acc = 0f
        var b = 0f
        var dx = 0f
        var dy = 0f
        var time = 0L
        var f = 0f
        var cl = 0f

        val numParticle = _particle.size
        for (i in numParticle-1 downTo 0) {
            val node = _particle[i]

            time = nowTime - node.start
            if (time > PARTICLE_LIFE) {
                _particle.removeAt(i)
                continue
            }

            ftime = time/1000f

            f = (time/ PARTICLE_LIFE).toFloat()

            cl = getColor().a
            if (f > 0.8f) {
                cl *= (1f-f) / 0.2f
            }

            acc = GRAVITY_VALUE * node.ay * ftime * ftime / 2f
            b = node.vy * ftime
            dx = node.x + ftime * node.vx
            dy = node.y + acc + b

            getDirector().setColor(0f, 0f, 0f, 0.6f)
            getDirector().drawCircle(dx, dy, 3.5f)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Int {
        return TOUCH_TRUE
    }

    override fun onBackPressed(): Boolean {
        return true
    }
}