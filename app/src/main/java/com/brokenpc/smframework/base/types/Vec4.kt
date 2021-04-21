package com.brokenpc.smframework.base.types

import com.brokenpc.smframework_kt.BuildConfig
import kotlin.math.atan2
import kotlin.math.sqrt

class Vec4 : Cloneable {
    companion object {
        @JvmField val MATH_FLOAT_SMALL:Float = 1.0e-37f
        @JvmField val MATH_TOLERANCE:Float = 2e-37f
        @JvmField val MATH_PIOVER2:Float = 1.57079632679489661923f
        @JvmField val MATH_EPSILON:Float = 0.000001f

        val ZERO = Vec4(0f, 0f, 0f, 0f)
        val ONE = Vec4(1f, 1f, 1f, 1f)
        val UNIT_X = Vec4(1f, 0f, 0f, 0f)
        val UNIT_Y = Vec4(0f, 1f, 0f, 0f)
        val UNIT_Z = Vec4(0f, 0f, 1f, 0f)
        val UNIT_W = Vec4(0f, 0f, 0f, 1f)


        @JvmStatic
        fun fromColor(color:Int): Vec4 {
            var components:FloatArray = FloatArray(4)
            var componentIndex:Int = 0
            for (i in 3 downTo 0) {
                val component:Int = color.shr(i*8).and(0x000000ff)
                components[componentIndex++] = component.toFloat()/255.0f
            }

            return Vec4(components)
        }

        @JvmStatic
        fun angle(v1: Vec4, v2: Vec4):Float {
            val dx:Float = v1.w * v2.x - v1.x * v2.w - v1.y * v2.z + v1.z * v2.y
            val dy:Float = v1.w * v2.y - v1.y * v2.w - v1.z * v2.x + v1.x * v2.z
            val dz:Float = v1.w * v2.z - v1.z * v2.w - v1.x * v2.y + v1.y * v2.x

            return atan2(sqrt(dx*dx+dy*dy+dz*dz) + MATH_FLOAT_SMALL,
                dot(v1, v2)
            )
        }

        @JvmStatic
        fun dot(v1: Vec4, v2: Vec4):Float {
            return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z + v1.w * v2.w
        }

        @JvmStatic
        fun add(v1: Vec4, v2: Vec4, dst: Vec4) {
            dst.x = v1.x + v2.x
            dst.y = v1.y + v2.y
            dst.z = v1.z + v2.z
            dst.w = v1.w + v2.w
        }

        @JvmStatic
        fun clamp(v: Vec4, min: Vec4, max: Vec4, dst: Vec4) {
            if (BuildConfig.DEBUG && (min.x > max.x || min.y > max.y || min.z > max.z || min.w > max.w)) {
                error("Assertion failed")
            }

            dst.x=v.x
            dst.y=v.y
            dst.z=v.z
            dst.w=v.w

            if (dst.x<min.x) dst.x=min.x
            if (dst.x>max.x) dst.x=max.x

            if (dst.y<min.y) dst.y=min.y
            if (dst.y>max.y) dst.y=max.y

            if (dst.z<min.z) dst.z=min.z
            if (dst.z>max.z) dst.z=max.z

            if (dst.w<min.z) dst.w=min.w
            if (dst.w>max.w) dst.w=max.w
        }

        @JvmStatic
        fun minus(v1: Vec4, v2: Vec4, dst: Vec4) {
            dst.x = v1.x-v2.x
            dst.y = v1.y-v2.y
            dst.z = v1.z-v2.z
            dst.w = v1.w-v2.w
        }
    }

    var x:Float = 0f
    var y:Float = 0f
    var z:Float = 0f
    var w:Float = 0f

    constructor() {
        set(0f, 0f, 0f, 0f)
    }
    constructor(v: Vec4) {
        set(v)
    }
    constructor(v1: Vec4, v2: Vec4) {
        set(v1, v2)
    }
    constructor(va: FloatArray) {
        set(va)
    }
    constructor(x: Float, y: Float, z: Float, w: Float) {
        set(x, y, z, w)
    }


    fun set(x:Float, y:Float, z:Float, w:Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }
    fun set(v: Vec4) {
        this.x = v.x
        this.y = v.y
        this.z = v.z
        this.w = v.w
    }
    fun set(v1: Vec4, v2: Vec4) {
        this.x = v2.x - v1.x
        this.y = v2.y - v1.y
        this.z = v2.z - v1.z
        this.w = v2.w - v1.w
    }
    fun set(va:FloatArray) {
        if (BuildConfig.DEBUG && va.size < 4) {
            error("Assertion failed")
        }
        this.x = va[0]
        this.y = va[1]
        this.z = va[2]
        this.w = va[3]
    }

    fun isZero():Boolean {return x==0f&&y==0f&&z==0f&&w==0f}
    fun isOne():Boolean {return x==1f&&y==1f&&z==1f&&w==1f}

    fun clamp(min: Vec4, max: Vec4) {
        if (BuildConfig.DEBUG && (min.x > max.x || min.y > max.y || min.z > max.z || min.w > max.w)) {
            error("Assertion failed")
        }

        if (x<min.x) x=min.x
        if (x>max.x) x=max.x

        if (y<min.y) y=min.y
        if (y>max.y) y=max.y

        if (z<min.z) z=min.z
        if (z>max.z) z=max.z

        if (w<min.w) w=min.w
        if (w>max.w) w=max.w
    }

    fun distance(v: Vec4):Float {
        return sqrt(distanceSquared(v))
    }

    fun distanceSquared(v: Vec4):Float {
        val dx:Float = v.x - x
        val dy:Float = v.y - y
        val dz:Float = v.z - z
        val dw:Float = v.w - w

        return dx*dx + dy*dy + dz*dz + dw*dw
    }

    fun dot(v: Vec4):Float {
        return x*v.x+y*v.y+z*v.z+w*v.w
    }

    fun lengthSquared():Float {return x*x+y*y+z*z+w*w}
    fun length():Float {return sqrt(lengthSquared())}

    fun negate() {
        x = -x
        y = -y
        z = -z
        w = -w
    }

    fun normalize() {
        var n:Float = lengthSquared()

        if (n==1.0f) return

        n = sqrt(n)

        if (n< MATH_TOLERANCE) return

        n = 1f/n
        x *= n
        y *= n
        z *= n
        w *= n
    }

    fun scale(s:Float) {
        x *= s
        y *= s
        z *= s
        w *= s
    }

    fun add(v: Vec4): Vec4 {
        val ret: Vec4 =
            Vec4(this)
        ret.x += v.x
        ret.y += v.y
        ret.z += v.z
        ret.w += v.w

        return ret
    }

    fun minus(v: Vec4): Vec4 {
        val ret: Vec4 =
            Vec4(this)
        ret.x -= v.x
        ret.y -= v.y
        ret.z -= v.z
        ret.w -= v.w

        return ret
    }

    fun addLocal(v: Vec4): Vec4 {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        this.w += v.w

        return this
    }

    fun minusLocal(v: Vec4): Vec4 {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        this.w -= v.w

        return this
    }

    fun multiply(a: Float): Vec4 {
        val ret: Vec4 =
            Vec4(this)
        ret.x *= a
        ret.y *= a
        ret.z *= a
        ret.w *= a

        return ret
    }

    fun divide(r: Float): Vec4 {
        val ret: Vec4 =
            Vec4(this)
        ret.x /= r
        ret.y /= r
        ret.z /= r
        ret.w /= r

        return ret
    }

    fun multiplyLocal(a: Float): Vec4 {
        this.x *= a
        this.y *= a
        this.z *= a
        this.w *= a

        return this
    }

    fun divideLocal(r: Float): Vec4 {
        this.x /= r
        this.y /= r
        this.z /= r
        this.w /= r

        return this
    }

    fun greaterthan(v: Vec4):Boolean {
        if (this.x==v.x) {
            if (this.y==v.y) {
                if (this.z==v.z) {
                    if (this.w>v.w) return this.w>v.w
                }
                return this.z>v.z
            }
            return this.y>v.y
        }
        return this.x>v.x
    }

    fun lessthan(v: Vec4):Boolean {
        if (this.x==v.x) {
            if (this.y==v.y) {
                if (this.z==v.z) {
                    if (this.w<v.w) return this.w<v.w
                }
                return this.z<v.z
            }
            return this.y<v.y
        }
        return this.x<v.x
    }

    fun equals(v: Vec4): Boolean {
        return this.x==v.x&&this.y==v.y&&this.z==v.z&&this.w==v.w
    }
}