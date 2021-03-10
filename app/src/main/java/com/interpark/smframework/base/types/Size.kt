package com.brokenpc.smframework.base.types

import kotlin.math.roundToInt

class Size : Cloneable {
    companion object {
        val ZERO: Size =
            Size(0.0f, 0.0f)
    }

    var width: Float = 0.0f
    var height: Float = 0.0f

    constructor(width: Int, height: Float) {
        this.width = width.toFloat()
        this.height = height
    }

    constructor(width: Float, height: Int) {
        this.width = width
        this.height = height.toFloat()
    }

    constructor(width:Int, height:Int) {
        this.width = width.toFloat()
        this.height = height.toFloat()
    }
    constructor(width: Float?, height: Float?) {
        this.width = width ?: 0f
        this.height = height ?: 0f
    }
    constructor() {
        Size(0.0f, 0.0f)
    }
    constructor(v: FloatArray) {
        Size(v[0], v[1])
    }
    constructor(s: Size) {
        Size(s.width, s.height)
    }

    fun set(width: Float, height: Float) {
        this.width = width
        this.height = height
    }
    fun set(s: Size): Size {
        set(s.width, s.height)
        return this
    }

    fun equal(s:Size):Boolean {
        return (width==s.width && height==s.height)
    }

    fun scale(v: Float): Size {
        return Size(
            this.width * v,
            this.height * v
        )
    }
    fun scaleLocal(v: Float) {
        this.width*v
        this.height*v
    }

    fun add(size: Size): Size {
        size.width += this.width
        size.height += this.height

        return size
    }
    fun addLocal(size: Size) {
        this.width += size.width
        this.height += size.height
    }

    fun minus(size: Size): Size {
        size.width = this.width-size.width
        size.height = this.height-size.height

        return size
    }
    fun minusLocal(size: Size) {
        this.width -= size.width
        this.height -= size.height
    }

    fun multiply(v: Float): Size {
        return this.scale(v)
    }
    fun multiplyLocal(v: Float) {
        this.scaleLocal(v)
    }

    fun toVec2(): Vec2 {
        return Vec2(this.width, this.height)
    }

    fun divide(r: Float): Size {
        return Size(
            this.width / r,
            this.height / r
        )
    }
    fun divideLocal(r: Float) {
        this.width /= r
        this.height /= r
    }

    fun roundEqual(size: Size): Boolean {
        return ((this.width.roundToInt() == size.width.roundToInt()) && (this.height.roundToInt() == size.height.roundToInt()))
    }



}