package com.brokenpc.smframework.base.types

import com.brokenpc.app.BuildConfig
import com.brokenpc.smframework.base.SMView

class Color4F : Cloneable {
    var r:Float = 1f
    var g:Float = 1f
    var b:Float = 1f
    var a:Float = 0f

    companion object {
        val WHITE:Color4F = Color4F(1f, 1f, 1f, 1f)
        val TRANSPARENT:Color4F = Color4F(0f, 0f, 0f, 0f)
        val BLACK:Color4F = Color4F(0f, 0f, 0f, 1f)

        val XDBDCDF:Color4F = SMView.MakeColor4F(0xdbdcdf, 1.0f)
        val XADAFB3:Color4F = SMView.MakeColor4F(0xadafb3, 1.0f)
        val XEEEFF1:Color4F = SMView.MakeColor4F(0xeeeff1, 1.0f)
        val X00A1E4:Color4F = SMView.MakeColor4F(0x00a1e4, 1.0f)
    }

    constructor() {
        set(1f, 1f, 1f, 0f)
    }
    constructor(color:FloatArray) {
        set(color)
    }
    constructor(r:Float, g:Float, b:Float, a:Float) {
        set(r, g, b, a)
    }
    constructor(c4f:Color4F) {
        set(c4f)
    }
    constructor(c4b:Color4B) {
        set(c4b)
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
    fun set(r:Float, g:Float, b:Float, a:Float) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }
    fun set(c4f:Color4F) {
        this.r = c4f.r
        this.g = c4f.g
        this.b = c4f.b
        this.a = c4f.a
    }
    fun set(c4b:Color4B) {
        this.r = c4b.r.toFloat()/255.0f
        this.g = c4b.g.toFloat()/255.0f
        this.b = c4b.b.toFloat()/255.0f
        this.a = c4b.a.toFloat()/255.0f
    }

    fun setAlpha(alpha:Float) {
        this.a = alpha
    }

    fun getColor4B():Color4B {
        return Color4B(this)
    }

    fun minus(color: Color4F):Color4F {
        val c:Color4F = Color4F(this)

        c.r -= color.r
        c.g -= color.g
        c.b -= color.b
        c.a -= color.a

        if (c.r<0) c.r = 0f
        if (c.g<0) c.g = 0f
        if (c.b<0) c.b = 0f
        if (c.a<0) c.a = 0f

        return c
    }

    fun add(color: Color4F):Color4F {
        val c:Color4F = Color4F(this)

        c.r += color.r
        c.g += color.g
        c.b += color.b
        c.a += color.a

        if (c.r>1) c.r = 1f
        if (c.g>1) c.g = 1f
        if (c.b>1) c.b = 1f
        if (c.a>1) c.a = 1f

        return c
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



}