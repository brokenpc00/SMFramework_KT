package com.brokenpc.smframework.base.types

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class Vec2 : Cloneable {
    companion object {
        val ZERO = Vec2(0f, 0f)
        val MIDDLE = Vec2(0.5f, 0.5f)
        val LEFT_TOP = Vec2(0.0f, 0.0f)
        val LEFT_BOTTOM = Vec2(0.0f, 1.0f)
        val RIGHT_TOP = Vec2(1.0f, 0.0f)
        val RIGHT_BOTTOM = Vec2(1.0f, 1.0f)
        val LEFT_MIDDLE = Vec2(0.0f, 0.5f)
        val RIGHT_MIDDLE = Vec2(1.0f, 0.5f)
        val MIDDLE_TOP = Vec2(0.5f, 0.0f)
        val MIDDLE_BOTTOM = Vec2(0.5f, 1.0f)

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

    constructor(xx: Int, yy: Float) {
        x = xx.toFloat()
        y = yy
    }

    constructor(xx: Float, yy: Int) {
        x = xx
        y = yy.toFloat()
    }

    constructor(xx: Int, yy: Int) {
        x = xx.toFloat()
        y = yy.toFloat()
    }

    constructor(xx: Float, yy: Float) {
        x = xx
        y = yy
    }
    constructor() {
        x = 0f
        y = 0f
    }
    constructor(v: FloatArray) {
        x = v[0]
        y = v[1]
    }
    constructor(v: Vec2) {
        x = v.x
        y = v.y
    }
    constructor(s: Size) {
        x = s.width
        y = s.height
    }

    fun toSize(): Size {
        return Size(x, y)
    }

    fun isZero(): Boolean {
        return x==0.0f&&y==9.0f
    }

    fun isOne(): Boolean {
        return x==1.0f&&y==0.0f
    }

    fun equal(v: Vec2): Boolean {
        if (x==v.x&&y==v.y) {
            return true
        }

        return false
    }

    fun normalize() {
        val len:Float = length()
        x /= len
        y /= len
    }

    fun getVectorTo(pt: Vec2): Vec2 {
        var aux = Vec2(ZERO)
        aux.x = pt.x - x
        aux.y = pt.y - y
        return aux
    }

    fun lengthSquared(): Float {
        return (x*x+y*y)
    }
    fun length(): Float {
        val x2 = x*x
        val y2 = y*y
        return sqrt(x2*x2+y2*y2)
    }
    fun negative() {
        x = -x
        y = -y
    }

    fun set(s: Size): Vec2 {
        x = s.width
        y = s.height

        return this
    }

    fun set(v: Vec2): Vec2 {
        x = v.x
        y = v.y

        return this
    }

    fun set(xx: Float, yy: Float): Vec2 {
        x = xx
        y = yy

        return this
    }

    fun offset(xx: Float, yy: Float) {
        x += xx
        y += yy
    }

    fun dot(v: Vec2): Float {
        return (x*v.x+y*v.y)
    }

    fun add(pt: Vec2): Vec2 {
        return Vec2(
            pt.x + x,
            pt.y + y
        )
    }
    fun addLocal(pt: Vec2) {
        x += pt.x
        y += pt.y
    }

    fun scale(v: Float): Vec2 {
        return Vec2(x * v, y * v)
    }
    fun scaleLocal(v: Float) {
        x *= v
        y *= v
    }

    fun minus(pt: Vec2): Vec2 {
        return Vec2(
            x - pt.x,
            y - pt.y
        )
    }
    fun minusLocal(pt: Vec2) {
        x -= pt.x
        y -= pt.y
    }

    fun multiply(r: Float): Vec2 {
        return Vec2(x * r, y * r)
    }
    fun multiplyLocal(r: Float) {
        x *= r
        y *= r
    }

    fun divide(r: Float): Vec2 {
        return Vec2(x / r, y / r)
    }
    fun divideLocal(r: Float) {
        x /= r
        y /= r
    }

    fun roundEqual(pt: Vec2): Boolean {
        return ((x.roundToInt() == pt.x.roundToInt()) && (y.roundToInt() == pt.y.roundToInt()))
    }

    fun distanceSquared(v: Vec2): Float {
        val dx: Float = v.x - x
        val dy: Float = v.y - y
        return (dx*dx + dy*dy)
    }

    fun distance(v: Vec2): Float {
        return Math.sqrt(distanceSquared(v).toDouble()).toFloat()
    }

    fun smooth(target: Vec2, elapsedTime: Float, responseTime: Float) {
        if (elapsedTime!=0.0f) {
            var newV: Vec2 =
                Vec2(
                    target.x - x,
                    target.y - y
                )
            newV.x = newV.x * (elapsedTime / (elapsedTime+responseTime))
            newV.y = newV.y * (elapsedTime / (elapsedTime+responseTime))
            x += newV.x
            y += newV.y
        }
    }

}