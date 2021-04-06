package com.brokenpc.smframework.base.types

import com.brokenpc.app.BuildConfig
import com.brokenpc.smframework.util.MathUtilC
import kotlin.math.atan2
import kotlin.math.sqrt

class Vec3 : Cloneable {
    companion object {
        val MATH_FLOAT_SMALL = 1.0e-37f
        val MATH_TOLERANCE = 2e-37f
        val MATH_PIOVER2 = 1.57079632679489661923f
        val MATH_EPSILON = 0.000001f
        val ZERO = Vec3(0f, 0f, 0f)

        @JvmStatic
        fun fromColor(color:Int): Vec3 {
            var components:FloatArray = FloatArray(3)
            var componentIndex:Int = 0
            for (i in 2 downTo 0) {
                val component:Int = color.shr(i*8).and(0x0000ff)
                components[componentIndex++] = (component.toFloat()/255.0f)
            }

            return Vec3(components)
        }

        @JvmStatic
        fun dot(v1: Vec3, v2: Vec3): Float {
            return (v1.x*v2.x + v1.y*v2.y + v1.z*v2.z)
        }

        @JvmStatic
        fun add(v1: Vec3, v2: Vec3, dst: Vec3) {
            dst.x = v1.x + v2.x
            dst.y = v1.y + v2.y
            dst.z = v1.z + v2.z
        }

        @JvmStatic
        fun clamp(v: Vec3, min: Vec3, max: Vec3, dst: Vec3) {
            dst.x = v.x
            if (dst.x < min.x) dst.x = min.x
            if (dst.x > max.x) dst.x = max.x

            dst.y = v.y
            if (dst.y < min.y) dst.y = min.y
            if (dst.y > max.y) dst.y = max.y

            dst.z = v.z
            if (dst.z < min.z) dst.z = min.z
            if (dst.z > max.z) dst.z = max.z
        }

        @JvmStatic
        fun cross(v1: Vec3, v2: Vec3, dst: Vec3) {
            val v1x:FloatArray = FloatArray(1)
            val v2x:FloatArray = FloatArray(1)
            val dstx:FloatArray = FloatArray(1)
            v1x[0] = v1.x
            v2x[0] = v2.x
            dstx[0] = dst.x
            MathUtilC.crossVec3(v1x, v2x, dstx)
            v1.x = v1x[0]
            v2.x = v2x[0]
            dst.x = dstx[0]
        }

        @JvmStatic
        fun minus(v1: Vec3, v2: Vec3, dst: Vec3) {
            dst.x = v1.x-v2.x
            dst.y = v1.y-v2.y
            dst.z = v1.z-v2.z
        }
    }

    var x:Float = 0f
    var y:Float = 0f
    var z:Float = 0f

    constructor() {
        set(0f, 0f, 0f)
    }
    constructor(v: Vec3) {
        set(v)
    }
    constructor(v1: Vec3, v2: Vec3) {
        set(v1, v2)
    }
    constructor(va:FloatArray) {
        set(va)
    }
    constructor(x:Float, y:Float, z:Float) {
        set(x, y, z)
    }

    fun set(v: Vec3) {
        this.x = v.x
        this.y = v.y
        this.z = v.z
    }
    fun set(v1: Vec3, v2: Vec3) {
        this.x = v2.x - v1.x
        this.y = v2.y - v1.y
        this.z = v2.z - v1.z
    }
    fun set(va:FloatArray) {
        if (BuildConfig.DEBUG && va.size < 3) {
            error("Assertion failed")
        }
        this.x = va[0]
        this.y = va[1]
        this.z = va[2]
    }
    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun angle(v1: Vec3, v2: Vec3): Float {
        val dx:Float = v1.y * v2.z - v1.z * v2.y
        val dy:Float = v1.z * v2.x - v1.x * v2.z
        val dz:Float = v1.x * v2.y - v1.y * v2.x

        return atan2(sqrt(dx*dx + dy*dy + dz*dz) + MATH_FLOAT_SMALL.toDouble(), dot(
            v1,
            v2
        ).toDouble()).toFloat()
    }

    fun length():Float {return sqrt(x*x+y*y+z*z)}
    fun lengthSquared():Float {return x*x+y*y+z*z}
    fun magnitude():Float {return sqrt(x*x+y*y+z*z)}
    fun getUintVector(): Vec3 {
        val m:Float = magnitude()
        return Vec3(x / m, y / m, z / m)
    }
    fun dot(r:Vec3):Float  { return (this.x*r.x + this.y*r.y + this.z*r.z) }
    fun normalize() {
        var n:Float = lengthSquared()
        if (n==1f) {
            return
        }

        n = sqrt(n)

        if (n< MATH_TOLERANCE) return

        n = 1f/n
        x *= n
        y *= n
        z *= n
    }

    fun getNormalize(): Vec3 {
        var v: Vec3 = Vec3(this)
        v.normalize()
        return v
    }

    fun clamp(min: Vec3, max: Vec3) {
        if (x<min.x) x = min.x
        if (x>max.x) x = max.x
        if (y<min.y) y = min.y
        if (y>max.y) y = max.y
        if (z<min.z) z = min.z
        if (z>max.z) z = max.z
    }

    fun add(v: Vec3): Vec3 {
        var ret: Vec3 = Vec3()
        ret.x = this.x+v.x
        ret.y = this.y+v.y
        ret.z = this.z+v.z

        return ret
    }

    fun addLocal(r:Vec3) {
        this.x += r.x
        this.y += r.y
        this.z += r.z
    }

    fun cross(v: Vec3) {cross(this, v, this)}

    fun distance(v: Vec3):Float {
        return sqrt(distanceSquared(v))
    }
    fun distanceSquared(v: Vec3): Float {
        val dx:Float = v.x - x
        val dy:Float = v.y - y
        val dz:Float = v.z - z

        return dx*dx+dy*dy+dz*dz
    }

    fun scale(r:Float): Vec3 {
        return Vec3(this.x*r, this.y*r, this.z*r)
    }

    fun lerp(target:Vec3, alpha:Float):Vec3 {
        return this.multiply(1f-alpha).add(target.multiply(alpha))
    }

    fun scaleLocal(r:Float) {
        this.x *= r
        this.y *= r
        this.z *= r
    }

    fun minus(r: Vec3):Vec3 {
        return Vec3(this.x-r.x, this.y-r.y, this.z-r.z)
    }
    fun minusLocal(r:Vec3) {
        this.x -= r.x
        this.y -= r.y
        this.z -= r.z
    }

    fun smooth(target: Vec3, elapsedTime:Float, responseTime:Float) {
        if (elapsedTime>0) {
            this.addLocal(target.minus(this).multiply(elapsedTime/(elapsedTime-responseTime)))
        }
    }

    fun multiply(r:Float): Vec3 {
        return Vec3(this.x*r, this.y*r, this.z*r)
    }
    fun multiplyLocal(r:Float) {
        this.x *= r
        this.y *= r
        this.z *= r
    }

    fun divide(r: Float):Vec3 {
        return Vec3(this.x/r, this.y/r, this.z/r)
    }
    fun divideLocal(r: Float) {
        this.x /= r
        this.y /= r
        this.z /= r
    }

    fun equal(v: Vec3):Boolean {
        return this.x==v.x&&this.y==v.y&&this.z==v.z
    }

    fun toFloat(): FloatArray {return floatArrayOf(this.x, this.y, this.z)}
}