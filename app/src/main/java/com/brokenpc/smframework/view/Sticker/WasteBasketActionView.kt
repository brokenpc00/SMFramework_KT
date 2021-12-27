package com.brokenpc.smframework.view.Sticker

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.*
import com.brokenpc.smframework.view.SMImageView
import com.brokenpc.smframework.view.SMSolidCircleView
import com.brokenpc.smframework.base.types.*

class WasteBasketActionView(director: IDirector): SMView(director) {
    private var _removeSelfOnExit = false

    companion object {
        const val MOVE_DURATION = 0.2f
        const val OPEN_DURATION = 0.1f
        const val EXIT_DURATION = 0.15f

        const val TRASH_SIZE= 120.0f
        const val TRASH_SHADOW_SIZE = 135.0f
        val TRASH_TOP_ANCHOR = Vec2(12f/75.0f, (75-20)/75.0f)
        val TRASH_TOP_POS = Vec2(TRASH_SIZE/2f - (37.5f-12f), TRASH_SIZE/2f + (37.5f-20f))
        val TRASH_BODY_POS = Vec2(TRASH_SIZE/2f, TRASH_SIZE/2f)

        @JvmStatic
        fun show(director: IDirector, parent: SMView, from: Vec2, to: Vec2): WasteBasketActionView {
            val view = WasteBasketActionView(director)
            view.initWithParam(parent, from, to)
            parent.addChild(view)
            return view
        }

        @JvmStatic
        fun showForList(director: IDirector, parent: SMView, from: Vec2, to: Vec2): WasteBasketActionView {
            val view = WasteBasketActionView(director)
            view.initWithParam2(parent, from, to)
            parent.addChild(view)
            return view
        }

        @JvmStatic
        fun showForUtil(director: IDirector, parent: SMView, from: Vec2, to: Vec2): WasteBasketActionView {
            val view = WasteBasketActionView(director)
            view.initWithParam3(parent, from, to)
            parent.addChild(view)
            return view
        }
    }

    override fun onExit() {
        super.onExit()

        if (getParent()!=null && !_removeSelfOnExit) {
            _removeSelfOnExit = true
            removeFromParent()
        }
    }

    protected fun initWithParam(parent: SMView, from: Vec2, to: Vec2): Boolean {
        setContentSize(TRASH_SIZE, TRASH_SIZE)
        setAnchorPoint(Vec2.MIDDLE)

        val shadow = SMSolidCircleView.create(getDirector())
        shadow.setContentSize(Size(TRASH_SHADOW_SIZE, TRASH_SHADOW_SIZE))
        shadow.setAntiAliasWidth(30f)
        shadow.setPosition(-7.5f, -15f)
        shadow.setColor(Color4F(0f, 0f, 0f, 0.15f))
        addChild(shadow)

        val bg = SMSolidCircleView.create(getDirector())
        bg.setContentSize(TRASH_SIZE, TRASH_SIZE)
        bg.setColor(MakeColor4F(0xff5825, 0.8f))
        addChild(bg)

        val icon1 = SMImageView.create(getDirector(), "images/delete_top.png")
        val icon2 = SMImageView.create(getDirector(), "images/delete_body.png")
        icon1.setAnchorPoint(TRASH_TOP_ANCHOR)
        icon1.setPosition(TRASH_TOP_POS)
        icon2.setPosition(TRASH_BODY_POS)
        addChild(icon1)
        addChild(icon2)

        setPosition(from)

        val move = EaseSineInOut.create(getDirector(), MoveTo.create(getDirector(), MOVE_DURATION, to))
        val scale = ScaleTo.create(getDirector(), MOVE_DURATION, 1.5f)
        val step1 = Spawn.create(getDirector(), move, scale, null)

        val open = EaseIn.create(getDirector(), RotateTo.create(getDirector(), OPEN_DURATION, -30f), 1.0f)
        val close = RotateTo.create(getDirector(), 0.05f, 0f)
        val step2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), MOVE_DURATION+0.1f), open, DelayTime.create(getDirector(), 0.7f), close, null)
        icon1.runAction(step2!!)

        val bounce = EaseInOut.create(getDirector(), ScaleSine.create(getDirector(), 0.4f, 1.5f), 2.0f)
        val exit = Spawn.create(getDirector(), EaseIn.create(getDirector(), ScaleTo.create(getDirector(), EXIT_DURATION, 0.7f), 3.0f), FadeTo.create(getDirector(), EXIT_DURATION, 0f), null)
        val seq = Sequence.create(getDirector(),
                                                step1,
                                                DelayTime.create(getDirector(), 1.0f),
                                                bounce,
                                                exit,
                                                CallFuncN.create(getDirector(), object : PERFORM_SEL_N {
            override fun performSelector(view: SMView?) {
                _removeSelfOnExit = true
                view?.removeFromParent()
            }
        }), null)
        runAction(seq!!)

        return true
    }

    protected fun initWithParam2(parent: SMView, from: Vec2, to: Vec2): Boolean {
        setContentSize(TRASH_SIZE, TRASH_SIZE)
        setAnchorPoint(Vec2.MIDDLE)

        val shadow = SMSolidCircleView.create(getDirector())
        shadow.setContentSize(Size(TRASH_SHADOW_SIZE, TRASH_SHADOW_SIZE))
        shadow.setAntiAliasWidth(30f)
        shadow.setPosition(-7.5f, -15f)
        shadow.setColor(Color4F(0f, 0f, 0f, 0.15f))
        addChild(shadow)

        val bg = SMSolidCircleView.create(getDirector())
        bg.setContentSize(TRASH_SIZE, TRASH_SIZE)
        bg.setColor(MakeColor4F(0xff5825, 0.8f))
        addChild(bg)

        val icon1 = SMImageView.create(getDirector(), "images/delete_top.png")
        val icon2 = SMImageView.create(getDirector(), "images/delete_body.png")
        icon1.setAnchorPoint(12f/75f, (75f-20f)/75f)
        icon1.setPosition(55f-(32.5f-18f), TRASH_SIZE-(65f+10.5f))
        icon2.setAnchorPoint(Vec2.MIDDLE)
        icon2.setPosition(55f, 65f)
        addChild(icon1)
        addChild(icon2)

        setPosition(from)
        setScale(-1f)

        val move = EaseSineInOut.create(getDirector(), MoveTo.create(getDirector(), 0.1f, to))
        val scale = ScaleTo.create(getDirector(), 0.1f, -1.2f, 1.2f)
        val step1 = Spawn.create(getDirector(), move, scale, null)

        val open = EaseIn.create(getDirector(), RotateTo.create(getDirector(), 0.05f, -30f), 1.0f)
        val close = RotateTo.create(getDirector(), 0.05f, 0f)
        val step2 = Sequence.create(getDirector(), DelayTime.create(getDirector(), 0.1f), open, DelayTime.create(getDirector(), 0.5f), close, null)
        icon1.runAction(step2!!)

        val exit = Spawn.create(getDirector(), EaseIn.create(getDirector(), ScaleTo.create(getDirector(), EXIT_DURATION, -0.7f, 0.7f), 3.0f), FadeTo.create(getDirector(), EXIT_DURATION, 0f), null)
        val seq = Sequence.create(getDirector(), step1, DelayTime.create(getDirector(), 0.35f), exit, CallFuncN.create(getDirector(), object : PERFORM_SEL_N {
            override fun performSelector(view: SMView?) {
                _removeSelfOnExit = true
                view?.removeFromParent()
            }
        }), null)

        runAction(seq!!)

        return true
    }

    protected fun initWithParam3(parent: SMView, from: Vec2, to: Vec2): Boolean {
        setContentSize(TRASH_SIZE, TRASH_SIZE)
        setAnchorPoint(Vec2.MIDDLE)

        val shadow = SMSolidCircleView.create(getDirector())
        shadow.setContentSize(Size(TRASH_SHADOW_SIZE, TRASH_SHADOW_SIZE))
        shadow.setAntiAliasWidth(30f)
        shadow.setPosition(-7.5f, -15f)
        shadow.setColor(Color4F(0f, 0f, 0f, 0.15f))
        addChild(shadow)

        val bg = SMSolidCircleView.create(getDirector())
        bg.setContentSize(TRASH_SIZE, TRASH_SIZE)
        bg.setColor(MakeColor4F(0xff5825, 0.8f))
        addChild(bg)

        val icon1 = SMImageView.create(getDirector(), "images/delete_top.png")
        val icon2 = SMImageView.create(getDirector(), "images/delete_body.png")
        icon1.setAnchorPoint(12f/75f, (75f-20f)/75f)
        icon1.setPosition(55f-(32.5f-18f), TRASH_SIZE-(65f+10.5f))
        icon2.setAnchorPoint(Vec2.MIDDLE)
        icon2.setPosition(55f, 65f)
        addChild(icon1)
        addChild(icon2)

        setPosition(from)

        val open = EaseIn.create(getDirector(), RotateTo.create(getDirector(), OPEN_DURATION, -30f), 1.0f)
        val close = RotateTo.create(getDirector(), 0.05f, 0f)
        val step2 = Sequence.create(getDirector(), open, DelayTime.create(getDirector(), 0.4f), close, null)
        icon1.runAction(step2!!)

        val scale = ScaleTo.create(getDirector(), 0.1f, 1.2f)
        val exit = Spawn.create(getDirector(), EaseIn.create(getDirector(), ScaleTo.create(getDirector(), EXIT_DURATION, 0.7f), 3.0f),
                                                FadeTo.create(getDirector(), EXIT_DURATION, 1f),
                                                null)
        val seq = Sequence.create(getDirector(),
                                    scale,
                                    DelayTime.create(getDirector(), 0.6f),
                                    exit,
                                    CallFuncN.create(getDirector(), object : PERFORM_SEL_N {
            override fun performSelector(view: SMView?) {
                _removeSelfOnExit = true
                view?.removeFromParent()
            }
        }), null)

        runAction(seq!!)

        return true
    }
}