package com.brokenpc.smframework.base.types

import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.SMView.Companion.M_PI
import kotlin.math.*

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

    constructor(pt: Float) {
        x = pt
        y = pt
    }

    constructor(pt : Int) {
        x = pt.toFloat()
        y = pt.toFloat()
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
        return sqrt(x*x+y*y)
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

    fun add(pt: Float): Vec2 {
        return Vec2(x+pt, y+pt)
    }

    fun add(pt: Int): Vec2 {
        return Vec2(x+pt, y+pt)
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

    fun minus(pt: Float): Vec2 {
        return Vec2(x-pt, y-pt)
    }

    fun minus(pt: Int): Vec2 {
        return Vec2(x-pt, y-pt)
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

    fun rotate(point: Vec2, angle: Float) {
        val sinAngle = sin(angle)
        val cosAngle = cos(angle)
        if (point.isZero()) {
            val tempX = x * cosAngle - y * sinAngle
            y = y * cosAngle + x * sinAngle
            x = tempX
        } else {
            val tempX = x - point.x
            val tempY = y - point.y

            x = tempX * cosAngle - tempY * sinAngle + point.x
            y = tempY * cosAngle + tempX * sinAngle + point.y
        }
    }

    fun rotate(src: Vec2, point: Vec2, angle: Float): Vec2 {
        val sinAngle = sin(angle)
        val cosAngle = cos(angle)
        if (point.isZero()) {
            val tempX = src.x * cosAngle - src.y * sinAngle
            src.y = src.y * cosAngle + src.x * sinAngle
            src.x = tempX
        } else {
            val tempX = src.x - point.x
            val tempY = src.y - point.y

            src.x = tempX * cosAngle - tempY * sinAngle + point.x
            src.y = tempY * cosAngle + tempX * sinAngle + point.y
        }
        return src
    }

    fun rotate(p: Vec2, theta: Float, base: Vec2): Vec2 {
        val ret = Vec2(ZERO)
        ret.x = ((p.x - base.x) * cos(theta * M_PI / 180f) - (p.y - base.y) * sin(theta*M_PI / 180f) + base.x).toFloat()
        ret.y = ((p.x - base.x) * sin(theta * M_PI / 180f) + (p.y - base.y) * sin(theta* M_PI / 180f) + base.y).toFloat()
        return ret
    }
}