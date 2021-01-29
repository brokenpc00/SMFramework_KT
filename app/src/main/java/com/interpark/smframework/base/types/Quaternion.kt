package com.brokenpc.smframework.base.types

import com.brokenpc.app.BuildConfig
import kotlin.math.*

class Quaternion {
    var x:Float = 0f
    var y:Float = 0f
    var z:Float = 0f
    var w:Float = 0f

    constructor() {
        set(0f, 0f, 0f, 0f)
    }
    constructor(x: Float, y: Float, z: Float, w: Float) {
        set(x, y, z, w)
    }
    constructor(array: FloatArray) {
        set(array)
    }
    constructor(m: Mat4) {
        set(m)
    }
    constructor(axis: Vec3, angle: Float) {
        set(axis, angle)
    }
    constructor(q:Quaternion) {
        set(q)
    }

    fun set(x:Float, y:Float, z:Float, w:Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }
    fun set(array: FloatArray) {
        if (BuildConfig.DEBUG && array.size < 4) {
            error("Assertion failed")
        }
        this.x = array[0]
        this.y = array[1]
        this.z = array[2]
        this.w = array[3]
    }
    fun set(m: Mat4) {
        Quaternion.createFromRotationMatrix(m, this)
    }
    fun set(q:Quaternion) {
        this.x = q.x
        this.y = q.y
        this.z = q.z
        this.w = q.w
    }
    fun set(axis:Vec3, angle:Float) {
        Quaternion.createFromAxisAngle(axis, angle, this)
    }

    companion object {
        var identityValue:Quaternion? = null
        val ZERO:Quaternion = Quaternion()

        @JvmStatic
        fun identity():Quaternion {
            if (identityValue==null) {
                identityValue = Quaternion(0f, 0f, 0f, 1f)
            }
            return identityValue!!
        }

        var zeroValue:Quaternion? = null

        @JvmStatic
        fun zero():Quaternion {
            if (zeroValue==null) {
                zeroValue = Quaternion(0f, 0f, 0f, 0f)
            }
            return zeroValue!!
        }

        @JvmStatic
        fun createFromRotationMatrix(m:Mat4, dst:Quaternion) {
            m.getRotation(dst)
        }

        @JvmStatic
        fun createFromAxisAngle(axis:Vec3, angle:Float, dst: Quaternion) {
            val halfAngle:Float = angle*0.5f
            val sinHalfAngle:Float = sin(halfAngle)

            val normal:Vec3 = Vec3(axis)
            normal.normalize()
            dst.x = normal.x * sinHalfAngle
            dst.y = normal.y * sinHalfAngle
            dst.z = normal.z * sinHalfAngle
            dst.w = cos(halfAngle)
        }

        @JvmStatic
        fun multiply(q1:Quaternion, q2:Quaternion, dst: Quaternion) {
            val x:Float = q1.w * q2.x + q1.x * q2.w + q1.y * q2.z - q1.z * q2.y
            val y:Float = q1.w * q2.y - q1.x * q2.z + q1.y * q2.w + q1.z * q2.x
            val z:Float = q1.w * q2.z + q1.x * q2.y - q1.y * q2.x + q1.z * q2.w
            val w:Float = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z

            dst.x = x
            dst.y = y
            dst.z = z
            dst.w = w
        }

        @JvmStatic
        fun lerp(q1: Quaternion, q2: Quaternion, t:Float, dst: Quaternion) {
            if (t==0f) {
                dst.set(q1)
                return
            } else if (t==1f) {
                dst.set(q2)
                return
            }

            val t1:Float = 1f - t
            dst.x = t1 * q1.x + t * q2.x
            dst.y = t1 * q1.y + t * q2.y
            dst.z = t1 * q1.z + t * q2.z
            dst.w = t1 * q1.w + t * q2.w
        }

        @JvmStatic
        fun slerp(q1: Quaternion, q2:Quaternion, t: Float, dst: Quaternion) {
            val x:FloatArray = FloatArray(1)
            val y:FloatArray = FloatArray(1)
            val z:FloatArray = FloatArray(1)
            val w:FloatArray = FloatArray(1)

            x[0] = dst.x
            y[0] = dst.y
            z[0] = dst.z
            w[0] = dst.w

            slerp(q1.x, q1.y, q1.z, q1.w, q2.x, q2.y, q2.z, q2.w, t, x, y, z, w)

            dst.x = x[0]
            dst.y = y[0]
            dst.z = z[0]
            dst.w = w[0]
        }

        fun slerp(q1x:Float, q1y:Float, q1z:Float, q1w:Float, q2x:Float, q2y:Float, q2z:Float, q2w:Float, t:Float, dstx:FloatArray, dsty:FloatArray, dstz:FloatArray, dstw:FloatArray) {

            if (t==0f) {
                dstx[0] = q1x
                dsty[0] = q1y
                dstz[0] = q1z
                dstw[0] = q1w
                return
            } else if (t==1.0f) {
                dstx[0] = q2x
                dsty[0] = q2y
                dstz[0] = q2z
                dstw[0] = q2w
                return
            }

            if (q1x==q2x && q1y==q2y && q1z==q2z && q1w==q2w) {
                dstx[0] = q1x
                dsty[0] = q1y
                dstz[0] = q1z
                dstw[0] = q1w
                return
            }

            var halfY:Float = 0f
            var alpha:Float = 0f
            var beta:Float = 0f
            var u:Float = 0f
            var f1:Float = 0f
            var f2a:Float = 0f
            var f2b:Float = 0f
            var ratio1:Float = 0f
            var ratio2:Float = 0f
            var halfSecHalfTheta:Float = 0f
            var versHalfTheta:Float = 0f
            var sqNotU:Float = 0f
            var sqU:Float = 0f
            val cosTheta:Float = q1w*q2w + q1x*q2x + q1y*q2y + q1z+q2z

            alpha = if (cosTheta>=0) 1f else -1f
            halfY = 1f + alpha * cosTheta

            f2b = t-0.5f
            u = if (f2b>=0) f2b else -f2b
            f2a = u - f2b
            f2b += u
            u += u
            f1 = 1f - u

            halfSecHalfTheta = 1.09f - (0.476537f - 0.0903321f * halfY) * halfY
            halfSecHalfTheta *= 1.5f - halfY * halfSecHalfTheta * halfSecHalfTheta
            versHalfTheta = 1.0f - halfY * halfSecHalfTheta

            sqNotU = f1 * f1
            ratio2 = 0.0000440917108f * versHalfTheta
            ratio1 = -0.00158730159f + (sqNotU - 16.0f) * ratio2
            ratio1 = 0.0333333333f + ratio1 * (sqNotU - 9.0f) * versHalfTheta
            ratio1 = -0.333333333f + ratio1 * (sqNotU - 4.0f) * versHalfTheta
            ratio1 = 1.0f + ratio1 * (sqNotU - 1.0f) * versHalfTheta

            sqU = u * u
            ratio2 = -0.00158730159f + (sqU - 16.0f) * ratio2
            ratio2 = 0.0333333333f + ratio2 * (sqU - 9.0f) * versHalfTheta
            ratio2 = -0.333333333f + ratio2 * (sqU - 4.0f) * versHalfTheta
            ratio2 = 1.0f + ratio2 * (sqU - 1.0f) * versHalfTheta

            f1 *= ratio1 * halfSecHalfTheta
            f2a *= ratio2
            f2b *= ratio2
            alpha *= f1 + f2a
            beta = f1 + f2b

            val w:Float = alpha * q1w + beta * q2w
            val x:Float = alpha * q1x + beta * q2x
            val y:Float = alpha * q1y + beta * q2y
            val z:Float = alpha * q1z + beta * q2z

            f1 = 1.5f - 0.5f * (w * w + x * x + y * y + z * z)
            dstw[0] = w * f1
            dstx[0] = x * f1
            dsty[0] = y * f1
            dstz[0] = z * f1
        }

        @JvmStatic
        fun slerpForSquad(q1: Quaternion, q2: Quaternion, t:Float, dst: Quaternion) {
            val c:Float = q1.x * q2.x + q1.y * q2.y + q1.z * q2.z + q1.w * q2.w

            if (abs(c)>=1f) {
                dst.x = q1.x
                dst.y = q1.y
                dst.z = q1.z
                dst.w = q1.w
                return
            }

            val omega:Float = acos(c)
            val s:Float = sqrt(1f - c*c)
            if (abs(s)<=0.00001f) {
                dst.x = q1.x
                dst.y = q1.y
                dst.z = q1.z
                dst.w = q1.w
                return
            }

            val r1:Float = sin((1f-t) * omega) / s
            val r2:Float = sin(t*omega) / s

            dst.x = q1.x * r1 + q2.x * r2
            dst.y = q1.y * r1 + q2.y * r2
            dst.z = q1.z * r1 + q2.z * r2
            dst.w = q1.w * r1 + q2.w * r2
        }

        @JvmStatic
        fun squad(q1: Quaternion, q2: Quaternion, s1:Quaternion, s2:Quaternion, t: Float, dst: Quaternion) {
            val dstQ:Quaternion = Quaternion(0f, 0f, 0f, 1f)
            val dstS:Quaternion = Quaternion(0f, 0f, 0f, 1f)

            slerpForSquad(q1, q2, t, dstQ)
            slerpForSquad(s1, s2, t, dstS)
            slerpForSquad(dstQ, dstS, 2f*t*(1f-t), dst)
        }
    }

    fun conjugate() {
        x = -x
        y = -y
        z = -z
    }

    fun getConjugated():Quaternion {
        val q:Quaternion = Quaternion(this)
        q.conjugate()
        return q
    }

    fun inverse():Boolean {
        var n:Float = x * x + y * y + z * z + w * w
        if (n==1f) {
            x = -x
            y = -y
            z = -z
            return true
        }

        if (n<0.000001f) return false

        n = 1.0f / n
        x = -x * n
        y = -y * n
        z = -z * n
        w *= n

        return true
    }

    fun getInversed():Quaternion {
        val q:Quaternion = Quaternion(this)
        q.inverse()
        return q
    }

    fun normalize() {
        var n:Float = x*x + y*y + z*z + w*w

        if (n==1f) {
            return
        }

        n = sqrt(n)
        if (n < 0.000001f) return

        n = 1f / n
        x *= n
        y *= n
        z *= n
        w *= n
    }

    fun getNormalized():Quaternion {
        val q:Quaternion = Quaternion(this)
        q.normalize()
        return q
    }

    fun setIdentity() {
        x = 0f
        y = 0f
        z = 0f
        w = 1f
    }

    fun toAxisAngle(axis: Vec3):Float {
        val q:Quaternion = Quaternion(this)
        q.normalize()

        axis.x = q.x
        axis.y = q.y
        axis.z = q.z
        axis.normalize()

        return 2f * acos(q.w)
    }

    fun multiply(q: Quaternion):Quaternion {
        val result:Quaternion = Quaternion(this)
        multiply(this, q, result)
        return result
    }

    fun multiply(v:Vec3):Vec3 {
        val uv:Vec3 = Vec3()
        val uuv:Vec3 = Vec3()

        val qvec:Vec3 = Vec3(this.x, this.y, this.z)
        Vec3.cross(qvec, v, uv)
        Vec3.cross(qvec, uv, uuv)

        uv.multiplyLocal(2f*w)
        uuv.multiplyLocal(2f)

        return v.add(uv).add(uuv)
    }

    fun multiplyLocal(q: Quaternion):Quaternion {
        multiply(q)
        return this
    }
}