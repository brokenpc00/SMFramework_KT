package com.brokenpc.smframework.base.types

import com.brokenpc.app.BuildConfig
import com.brokenpc.smframework.base.SMView

class Color4F {
    var r:Float = 1f
    var g:Float = 1f
    var b:Float = 1f
    var a:Float = 1f

    constructor() {
        r = 1f
        g = 1f
        b = 1f
        a = 1f
    }
    constructor(color:FloatArray) {
        if (BuildConfig.DEBUG && color.size < 4) {
            error("Assertion failed")
    }
        r = color[0]
        g = color[1]
        b = color[2]
        a = color[3]
    }
    constructor(rr:Float, gg:Float, bb:Float, aa:Float) {
        r = rr
        g = gg
        b = bb
        a = aa
    }
    constructor(c4f:Color4F) {
        r = c4f.r
        g = c4f.g
        b = c4f.b
        a = c4f.a
    }
    constructor(c4b:Color4B) {
        r = c4b.r.toFloat()/255.0f
        g = c4b.g.toFloat()/255.0f
        b = c4b.b.toFloat()/255.0f
        a = c4b.a.toFloat()/255.0f
    }

    fun set(color:FloatArray) {
        if (BuildConfig.DEBUG && color.size < 4) {
            error("Assertion failed")
        }
        r = color[0]
        g = color[1]
        b = color[2]
        a = color[3]
    }
    fun set(rr:Float, gg:Float, bb:Float, aa:Float) {
        r = rr
        g = gg
        b = bb
        a = aa
    }
    fun set(c4f:Color4F) {
        r = c4f.r
        g = c4f.g
        b = c4f.b
        a = c4f.a
    }
    fun set(c4b:Color4B) {
        r = c4b.r.toFloat()/255.0f
        g = c4b.g.toFloat()/255.0f
        b = c4b.b.toFloat()/255.0f
        a = c4b.a.toFloat()/255.0f
    }

    fun setAlpha(alpha:Float) {
        a = alpha
    }

    fun getColor4B():Color4B {
        return Color4B(this)
    }

    fun minus(color: Color4F):Color4F {
        return Color4F(r-color.r, g-color.g, b-color.b, a-color.a)
    }

    fun add(color: Color4F):Color4F {
        return Color4F(r+color.r, g+color.g, b+color.b, a+color.a)
    }

    fun multiply(f:Float): Color4F {
        var ratio:Float = f
        if (ratio>1) ratio =1f

        return Color4F(r*ratio, g*ratio, b*ratio, a*ratio)
    }

    fun divide(f:Float):Color4F {
        var ratio:Float = f
        if (ratio>1) ratio =1f

        return Color4F(r/ratio, g/ratio, b/ratio, a/ratio)
    }

    fun equal(color:Color4F):Boolean {
        return r==color.r&&g==color.g&&b==color.b&&a==color.a
    }


    companion object {
        val WHITE = Color4F(1f, 1f, 1f, 1f)
        val TRANSPARENT = Color4F(0f, 0f, 0f, 0f)
        val BLACK = Color4F(0f, 0f, 0f, 1f)

        val XDBDCDF = SMView.MakeColor4F(0xdbdcdf, 1.0f)
        val XADAFB3 = SMView.MakeColor4F(0xadafb3, 1.0f)
        val XEEEFF1 = SMView.MakeColor4F(0xeeeff1, 1.0f)
        val X00A1E4 = SMView.MakeColor4F(0x00a1e4, 1.0f)

        val TEXT_BLACK = SMView.MakeColor4F(0x222222, 1f)
        val ALARM_BADGE_RED = SMView.MakeColor4F(0xff3a2f, 1f)
        val ALARM_BADGE_RED_DIM = SMView.MakeColor4F(0xd53128, 1f)
        val MINT = SMView.MakeColor4F(0x64dbd5, 1f)

        val TOAST_RED = SMView.MakeColor4F(0xFF3A2F, 0.95f)
        val TOAST_GRAY = SMView.MakeColor4F(0xADAFB3, 0.95f)
        val TOAST_GREEN = SMView.MakeColor4F(0x64DBD5, 0.95f)
        val TOAST_BLUE = SMView.MakeColor4F(0x4399FA, 0.95f)

        val NEGATIVE_BUTTON_NORMAL = SMView.MakeColor4F(0xADAFB3, 1.0f)
        val NEGATIVE_BUTTON_PRESSED = SMView.MakeColor4F(0x9A9CA1, 1.0f)
        val POSITIVE_BUTTON_NORMAL = SMView.MakeColor4F(0x494949, 1.0f)
        val POSITIVE_BUTTON_PRESSED = SMView.MakeColor4F(0x373737, 1.0f)
    }
}