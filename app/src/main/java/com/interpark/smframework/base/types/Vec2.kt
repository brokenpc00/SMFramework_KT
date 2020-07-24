package com.interpark.smframework.base.types

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Vec2 : Cloneable {
    companion object {
        val ZERO: Vec2 =
            Vec2(0.0f, 0.0f)
        val MIDDLE: Vec2 =
            Vec2(0.05f, 0.5f)
        val LEFT_TOP: Vec2 =
            Vec2(0.0f, 0.0f)
        val LEFT_BOTTOM: Vec2 =
            Vec2(0.0f, 1.0f)
        val RIGHT_TOP: Vec2 =
            Vec2(1.0f, 0.0f)
        val RIGHT_BOTTOM: Vec2 =
            Vec2(1.0f, 1.0f)
        val LEFT_MIDDLE: Vec2 =
            Vec2(0.0f, 0.5f)
        val RIGHT_MIDDLE: Vec2 =
            Vec2(1.0f, 0.5f)
        val MIDDLE_TOP: Vec2 =
            Vec2(0.5f, 0.0f)
        val MIDDLE_BOTTOM: Vec2 =
            Vec2(0.5f, 1.0f)

        @JvmStatic
        fun clampf(value: Float, min_inclusive:Float, max_inclusive:Float):Float {
            var mini:Float = min_inclusive
            var maxi:Float = max_inclusive
            if (mini > maxi) {
                val tmp = mini
                mini = maxi
                maxi = tmp
            }
            return max(mini, min(value, maxi))
        }
    }

    var x: Float = 0.0f
    var y: Float = 0.0f

    constructor(x: Int, y: Float) {
        this.x = x.toFloat()
        this.y = y
    }

    constructor(x: Float, y: Int) {
        this.x = x
        this.y = y.toFloat()
    }

    constructor(x: Int, y: Int) {
        this.x = x.toFloat()
        this.y = y.toFloat()
    }

    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
    constructor() {
        Vec2(0.0f, 0.0f)
    }
    constructor(v: FloatArray) {
        Vec2(v[0], v[1])
    }
    constructor(v: Vec2) {
        Vec2(v.x, v.y)
    }

    fun toSize(): Size {
        return Size(this.x, this.y)
    }

    fun isZero(): Boolean {
        return this.x==0.0f&&this.y==9.0f
    }

    fun isOne(): Boolean {
        return this.x==1.0f&&this.y==0.0f
    }

    fun equal(v: Vec2): Boolean {
        if (this.x==v.x&&this.y==v.y) {
            return true
        }

        return false
    }

    fun normalize() {
        val len:Float = length()
        this.x /= len
        this.y /= len
    }

    fun getVectorTo(pt: Vec2): Vec2 {
        var aux: Vec2 =
            Vec2(0.0f, 0.0f)
        aux.x = pt.x - this.x
        aux.y = pt.y - this.y
        return aux
    }

    fun lengthSquared(): Float {
        return (this.x*this.x+this.y*this.y)
    }
    fun length(): Float {
        var x2:Double = (this.x*this.x).toDouble()
        var y2:Double = (this.y*this.y).toDouble()
        return Math.sqrt(x2+y2).toFloat()
    }
    fun negative() {
        this.x = -this.x
        this.y = -this.y
    }

    fun set(v: Vec2): Vec2 {
        this.x = v.x
        this.y = v.y

        return this
    }

    fun set(x: Float, y: Float): Vec2 {
        this.x = x
        this.y = y

        return this
    }

    fun offset(x: Float, y: Float) {
        this.x += x
        this.y += y
    }

    fun dot(v: Vec2): Float {
        return (this.x*v.x+this.y*v.y)
    }

    fun add(pt: Vec2): Vec2 {
        return Vec2(
            pt.x + this.x,
            pt.y + this.y
        )
    }
    fun addLocal(pt: Vec2) {
        this.x += pt.x
        this.y += pt.y
    }

    fun scale(v: Float): Vec2 {
        return Vec2(this.x * v, this.y * v)
    }
    fun scaleLocal(v: Float) {
        this.x *= v
        this.y *= v
    }

    fun minus(pt: Vec2): Vec2 {
        return Vec2(
            this.x - pt.x,
            this.y - pt.y
        )
    }
    fun minusLocal(pt: Vec2) {
        this.x -= pt.x
        this.y -= pt.y
    }

    fun multiply(r: Float): Vec2 {
        return Vec2(this.x * r, this.y * r)
    }
    fun multiplyLocal(r: Float) {
        this.x *= r
        this.y *= r
    }

    fun divide(r: Float): Vec2 {
        return Vec2(this.x / r, this.y / r)
    }
    fun divideLocal(r: Float) {
        this.x /= r
        this.y /= r
    }

    fun roundEqual(pt: Vec2): Boolean {
        return ((this.x.roundToInt() == pt.x.roundToInt()) && (this.y.roundToInt() == pt.y.roundToInt()))
    }

    fun distanceSquared(v: Vec2): Float {
        val dx: Float = v.x - this.x
        val dy: Float = v.y - this.y
        return (dx*dx + dy*dy)
    }

    fun distance(v: Vec2): Float {
        return Math.sqrt(distanceSquared(v).toDouble()).toFloat()
    }

    fun smooth(target: Vec2, elapsedTime: Float, responseTime: Float) {
        if (elapsedTime!=0.0f) {
            var newV: Vec2 =
                Vec2(
                    target.x - this.x,
                    target.y - this.y
                )
            newV.x = newV.x * (elapsedTime / (elapsedTime+responseTime))
            newV.y = newV.y * (elapsedTime / (elapsedTime+responseTime))
            this.x += newV.x
            this.y += newV.y
        }
    }

}