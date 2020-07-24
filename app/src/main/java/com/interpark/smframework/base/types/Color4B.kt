package com.interpark.smframework.base.types

class Color4B : Cloneable {
    var r:Int = 0xff
    var g:Int = 0xff
    var b:Int = 0xff
    var a:Int = 0xff
    companion object {

    }

    constructor() {
        set(0xff, 0xff, 0xff, 0)
    }
    constructor(r:Int, g:Int, b:Int, a:Int) {
        set(r, g, b, a)
    }
    constructor(color4F: Color4F) {
        set(color4F)
    }
    constructor(color4B: Color4B) {
        set(color4B)
    }

    fun set(r:Int, g:Int, b:Int, a:Int) {
        this.r = r
        this.g = g
        this.b = b
        this.a = a
    }
    fun set(color4F: Color4F) {
        this.r = (color4F.r*0xff).toInt()
        this.g = (color4F.g*0xff).toInt()
        this.b = (color4F.b*0xff).toInt()
        this.a = (color4F.a*0xff).toInt()
    }
    fun set(color4B: Color4B) {
        this.r = color4B.r
        this.g = color4B.g
        this.b = color4B.b
        this.a = color4B.a
    }

    fun getColor4F():Color4F {
        return Color4F(this)
    }

    fun setAlpha(alpha:Float) {
        this.a = a
    }
}