package com.interpark.smframework.base.types

import com.interpark.app.BuildConfig
import com.interpark.smframework.util.MathUtilC
import com.interpark.smframework.util.OpenGlUtils
import java.util.*
import kotlin.math.*

class Mat4 {
    var m:FloatArray = FloatArray(16)
    companion object {
        private val m: FloatArray = FloatArray(16)
        val ZERO:Mat4 = Mat4(0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f,
                            0f, 0f, 0f, 0f)
        val IDENTITY:Mat4 = Mat4(1f, 0f, 0f, 0f,
                                0f, 1f, 0f, 0f,
                                0f, 0f, 1f, 0f,
                                0f, 0f, 0f, 1f)

        @JvmStatic
        fun MATH_DEG_TO_RAD(x:Float): Float {
            return x*0.0174532925f
        }

        @JvmStatic
        fun MATH_RAD_TO_DEG(x: Float): Float {
            return x * 57.29577951f
        }

        val MATH_FLOAT_SMALL:Float = 1.0e-37f
        val MATH_TOLERANCE:Float = 2e-37f
        val MATH_PIOVER2:Float = 1.57079632679489661923f
        val MATH_EPSILON:Float = 0.000001f

        @JvmStatic
        fun createLookAt(eyePosition:Vec3, targetPosition:Vec3, up:Vec3, dst:Mat4) {
            createLookAt(eyePosition.x, eyePosition.y, eyePosition.z, targetPosition.x, targetPosition.y, targetPosition.z, up.x, up.y, up.z, dst)
        }

        @JvmStatic
        fun createLookAt(eyePositionX:Float, eyePositionY:Float, eyePositionZ:Float,
                         targetPositionX:Float, targetPositionY: Float, targetPositionZ: Float,
                         upX:Float, upY:Float, upZ:Float, dst:Mat4) {
            var eye:Vec3 = Vec3(eyePositionX, eyePositionY, eyePositionZ)
            var target:Vec3 = Vec3(targetPositionX, targetPositionY, targetPositionZ)
            var up:Vec3 = Vec3(upX, upY, upZ)
            up.normalize()

            val zaxis = Vec3(Vec3.ZERO)
            Vec3.minus(eye, target, zaxis)
            zaxis.normalize()

            val xaxis = Vec3(Vec3.ZERO)
            Vec3.minus(eye, target, xaxis)
            xaxis.normalize()

            val yaxis = Vec3(Vec3.ZERO)
            Vec3.minus(eye, target, yaxis)
            yaxis.normalize()

            dst.m[0] = xaxis.x
            dst.m[1] = yaxis.x
            dst.m[2] = zaxis.x
            dst.m[3] = 0f

            dst.m[4] = xaxis.y
            dst.m[5] = yaxis.y
            dst.m[6] = zaxis.y
            dst.m[7] = 0f

            dst.m[8] = xaxis.z
            dst.m[9] = yaxis.z
            dst.m[10] = zaxis.z
            dst.m[11] = 0f

            dst.m[12] = -Vec3.dot(xaxis, eye)
            dst.m[13] = -Vec3.dot(yaxis, eye)
            dst.m[14] = -Vec3.dot(zaxis, eye)
            dst.m[15] = 1f
        }

        @JvmStatic
        fun createPerspective(fieldOfView: Float, aspectRatio: Float, zNearPlane: Float, zFarPlane:Float, dst:Mat4) {
            if (BuildConfig.DEBUG && zFarPlane == zNearPlane) {
                error("Assertion failed")
            }

            val f_n: Float = 1.0f / (zFarPlane - zNearPlane)
            val theta: Float = MATH_DEG_TO_RAD(fieldOfView) * 0.5f
            if (Math.abs(theta % MATH_PIOVER2) < MATH_EPSILON) return

            val divisor: Float = tan(theta)
            if (BuildConfig.DEBUG && divisor == 0f) {
                error("Assertion failed")
            }
            val factor: Float = 1.0f / divisor
            dst.set(ZERO)

            if (BuildConfig.DEBUG && aspectRatio == 0f) {
                error("Assertion failed")
            }

            dst.m[0] = (1.0f/aspectRatio) * factor
            dst.m[5] = factor
            dst.m[10] = (-(zFarPlane + zNearPlane)) * f_n
            dst.m[11] = -1.0f
            dst.m[14] = -2.0f * zFarPlane * zNearPlane * f_n
        }

        @JvmStatic
        fun createOrthographic(width:Float, height:Float, zNearPlane: Float, zFarPlane: Float, dst: Mat4) {
            val halfWidth:Float = width/2.0f
            val halfHeight:Float = height/2.0f

        }

        @JvmStatic
        fun createOrthographicOffCenter(left:Float, right:Float, bottom:Float, top:Float, zNearPlane: Float, zFarPlane: Float, dst: Mat4) {
            if (BuildConfig.DEBUG && (right==left || top==bottom || zFarPlane==zNearPlane)) {
                error("Assertion Failed")
            }

            dst.set(ZERO)

            dst.m[0] = 2f / (right-left)
            dst.m[5] = 2f / (top-bottom)
            dst.m[10] = 2f / (zNearPlane - zFarPlane)

            dst.m[12] = (left+right) / (left-right)
            dst.m[13] = (top+bottom) /  (bottom-top)
            dst.m[14] = (zNearPlane+zFarPlane) / (zNearPlane-zFarPlane)
            dst.m[15] = 1f
        }

        @JvmStatic
        fun createBillboard(objectPosition:Vec3, cameraPosition:Vec3, cameraUpVector:Vec3, dst: Mat4) {
            createBillboardHelper(objectPosition, cameraPosition, cameraUpVector, null, dst)
        }

        @JvmStatic
        fun createBillboard(objectPosition: Vec3, cameraPosition: Vec3, cameraUpVector: Vec3, cameraForwardVector:Vec3, dst: Mat4) {
            createBillboardHelper(objectPosition, cameraPosition, cameraUpVector, cameraForwardVector, dst)
        }

        @JvmStatic
        fun createBillboardHelper(objectPosition: Vec3, cameraPosition: Vec3, cameraUpVector: Vec3, cameraForwardVector: Vec3?, dst:Mat4) {
            val delta:Vec3 = Vec3(objectPosition, cameraPosition)
            val isSufficientDelta:Boolean = delta.lengthSquared() > MATH_EPSILON

            dst.set(ZERO)
            dst.m[3] = objectPosition.x
            dst.m[7] = objectPosition.y
            dst.m[11] = objectPosition.z

            if (cameraForwardVector!=null || isSufficientDelta) {
                val target:Vec3 = Vec3(if (isSufficientDelta) cameraPosition else objectPosition.minus(cameraForwardVector!!))

                val lookAt:Mat4 = Mat4(ZERO)
                createLookAt(objectPosition, target, cameraUpVector, lookAt)
                dst.m[0] = lookAt.m[0]
                dst.m[1] = lookAt.m[4]
                dst.m[2] = lookAt.m[8]
                dst.m[4] = lookAt.m[1]
                dst.m[5] = lookAt.m[5]
                dst.m[6] = lookAt.m[9]
                dst.m[8] = lookAt.m[2]
                dst.m[9] = lookAt.m[6]
                dst.m[10] = lookAt.m[10]
            }
        }

        @JvmStatic
        fun createScale(scale:Vec3, dst: Mat4) {
            dst.set(IDENTITY)

            dst.m[0] = scale.x
            dst.m[5] = scale.y
            dst.m[10] = scale.z
        }

        @JvmStatic
        fun createScale(xScale:Float, yScale:Float, zScale:Float, dst: Mat4) {
            dst.set(IDENTITY)

            dst.m[0] = xScale
            dst.m[5] = yScale
            dst.m[10] = zScale
        }

        @JvmStatic
        fun createRotation(q:Quaternion, dst: Mat4) {
            val x2:Float = q.x+q.x
            val y2:Float = q.y+q.y
            val z2:Float = q.z+q.z

            val xx2:Float = q.x*x2
            val yy2:Float = q.y*y2
            val zz2:Float = q.z*z2

            val xy2:Float = q.x*y2

            val xz2:Float = q.x*z2
            val yz2:Float = q.y*z2

            val wx2:Float = q.w*x2
            val wy2:Float = q.w*y2
            val wz2:Float = q.w*y2

            dst.m[0] = 1.0f - yy2 - zz2
            dst.m[1] = xy2 + wz2
            dst.m[2] = xz2 - wy2
            dst.m[3] = 0.0f

            dst.m[4] = xy2 - wz2
            dst.m[5] = 1.0f - xx2 - zz2
            dst.m[6] = yz2 + wx2
            dst.m[7] = 0.0f

            dst.m[8] = xz2 + wy2
            dst.m[9] = yz2 - wx2
            dst.m[10] = 1.0f - xx2 - yy2
            dst.m[11] = 0.0f

            dst.m[12] = 0.0f
            dst.m[13] = 0.0f
            dst.m[14] = 0.0f
            dst.m[15] = 1.0f
        }

        @JvmStatic
        fun createRotation(axis: Vec3, angle:Float, dst: Mat4) {
            var x:Float = axis.x
            var y:Float = axis.y
            var z:Float = axis.z

            var n:Float = x*x + y*y + z*z
            if (n!=1f) {
                n = sqrt(n)

                if (n>0.000001f) {
                    n = 1f / n
                    x *= n
                    y *= n
                    z *= n
                }
            }

            val c:Float = cos(angle)
            val s:Float = sin(angle)

            val t:Float = 1f - c
            val tx:Float = t*x
            val ty:Float = t*y
            val tz:Float = t*z
            val txy:Float = tx*y
            val txz:Float = tx*z
            val tyz:Float = ty*z
            val sx:Float = s*x
            val sy:Float = s*y
            val sz:Float = s*z

            dst.m[0] = c + tx * x
            dst.m[1] = txy + sz
            dst.m[2] = txz - sy
            dst.m[3] = 0f

            dst.m[4] = txy - sz
            dst.m[5] = c + ty * y
            dst.m[6] = tyz + sx
            dst.m[7] = 0f

            dst.m[8] = txz + sy
            dst.m[9] = tyz - sx
            dst.m[10] = c + tz * z
            dst.m[11] = 0f

            dst.m[12] = 0f
            dst.m[13] = 0f
            dst.m[14] = 0f
            dst.m[15] = 1f
        }

        @JvmStatic
        fun createRotationX(angle: Float, dst: Mat4) {
            dst.set(IDENTITY)
            val c:Float = cos(angle)
            val s:Float = sin(angle)

            dst.m[5] = c
            dst.m[6] = s
            dst.m[9] = -s
            dst.m[10] = c
        }

        @JvmStatic
        fun createRotationY(angle: Float, dst: Mat4) {
            dst.set(IDENTITY)
            val c:Float = cos(angle)
            val s:Float = sin(angle)

            dst.m[0] = c
            dst.m[2] = -s
            dst.m[8] = s
            dst.m[10] = c
        }

        @JvmStatic
        fun createRotationZ(angle: Float, dst: Mat4) {
            dst.set(IDENTITY)
            val c:Float = cos(angle)
            val s:Float = sin(angle)

            dst.m[0] = c
            dst.m[1] = s
            dst.m[4] = -s
            dst.m[5] = c
        }

        @JvmStatic
        fun createTranslation(translation: Vec3, dst: Mat4) {
            dst.set(IDENTITY)

            dst.m[12] = translation.x
            dst.m[13] = translation.y
            dst.m[14] = translation.z
        }

        @JvmStatic
        fun createTranslation(xTranslation:Float, yTranslation:Float, zTranslation:Float, dst: Mat4) {
            dst.set(IDENTITY)

            dst.m[12] = xTranslation
            dst.m[13] = yTranslation
            dst.m[14] = zTranslation
        }

        @JvmStatic
        fun add(scalar: Float, dst: Mat4) {
            MathUtilC.addMatrix(this.m, scalar, dst.m)
        }

        @JvmStatic
        fun add(m1:Mat4, m2:Mat4, dst:Mat4) {
            MathUtilC.addMatrix(m1.m, m2.m, dst.m)
        }

        @JvmStatic
        fun subtract(m1:Mat4, m2:Mat4, dst: Mat4) {
            MathUtilC.subtractMatrix(m1.m, m2.m, dst.m)
        }
    }

    constructor() {
        set(IDENTITY)
    }
    constructor(m:FloatArray) {
        set(m)
    }
    constructor(m11:Float, m12:Float, m13:Float, m14:Float,
                m21:Float, m22:Float, m23:Float, m24:Float,
                m31:Float, m32:Float, m33:Float, m34:Float,
                m41:Float, m42:Float, m43:Float, m44:Float) {
        set(m11, m12, m13, m14,
            m21, m22, m23, m24,
            m31, m32, m33, m34,
            m41, m42, m43, m44)
    }
    constructor(m:Mat4) {
        set(m)
    }

    fun set(m:FloatArray) {
        if (BuildConfig.DEBUG && m.size < 16) {
            error("Assertion failed")
        }

        set(m[0], m[1], m[2], m[3],
            m[4], m[5], m[6], m[7],
            m[8], m[9], m[10], m[11],
            m[12], m[13], m[14], m[15])
    }
    fun set(m11:Float, m12:Float, m13:Float, m14:Float,
            m21:Float, m22:Float, m23:Float, m24:Float,
            m31:Float, m32:Float, m33:Float, m34:Float,
            m41:Float, m42:Float, m43:Float, m44:Float) {
        m[0] = m11
        m[1] = m12
        m[2] = m13
        m[3] = m14
        m[4] = m21
        m[5] = m22
        m[6] = m23
        m[7] = m24
        m[8] = m31
        m[9] = m32
        m[10] = m33
        m[11] = m34
        m[12] = m41
        m[13] = m42
        m[14] = m43
        m[15] = m44
    }
    fun set(mat4: Mat4) {
        m[0] = mat4.m[0]
        m[1] = mat4.m[1]
        m[2] = mat4.m[2]
        m[3] = mat4.m[3]
        m[4] = mat4.m[4]
        m[5] = mat4.m[5]
        m[6] = mat4.m[6]
        m[7] = mat4.m[7]
        m[8] = mat4.m[8]
        m[9] = mat4.m[9]
        m[10] = mat4.m[10]
        m[11] = mat4.m[11]
        m[12] = mat4.m[12]
        m[13] = mat4.m[13]
        m[14] = mat4.m[14]
        m[15] = mat4.m[15]
    }

    fun getRotation(r:Quaternion):Boolean {
        return decompose(null, r, null)
    }

    fun decompose(scale:Vec3?, rotation:Quaternion?, translation:Vec3?):Boolean {
        if (translation!=null) {
            translation.x = m[12]
            translation.y = m[13]
            translation.z = m[14]
        }

        if (scale==null && rotation==null) return true

        val xaxis:Vec3 = Vec3(m[0], m[1], m[2])
        val yaxis:Vec3 = Vec3(m[4], m[5], m[6])
        val zaxis:Vec3 = Vec3(m[8], m[9], m[10])
        val scaleX = xaxis.length()
        val scaleY = yaxis.length()
        var scaleZ = zaxis.length()

        val det = determinant()
        if (det<0) scaleZ = -scaleZ

        if (scale!=null) {
            scale.x = scaleX
            scale.y = scaleY
            scale.z = scaleZ
        }

        if (rotation==null) return true

        if (scaleX< MATH_TOLERANCE || scaleY< MATH_TOLERANCE || abs(scaleZ) < MATH_TOLERANCE) return false

        var rn:Float = 0f



        rn = 1.0f / scaleX
        xaxis.x *= rn
        xaxis.y *= rn
        xaxis.z *= rn

        rn = 1.0f / scaleY
        yaxis.x *= rn
        yaxis.y *= rn
        yaxis.z *= rn

        rn = 1.0f / scaleZ
        zaxis.x *= rn
        zaxis.y *= rn
        zaxis.z *= rn

        val trace:Float = xaxis.x + yaxis.y + zaxis.z + 1.0f

        if (trace> MATH_EPSILON) {
            val s:Float = 0.5f / sqrt(trace)
            rotation.w = 0.25f / s
            rotation.x = (yaxis.z - zaxis.y) * s
            rotation.y = (zaxis.x - xaxis.z) * s
            rotation.z = (xaxis.y - yaxis.x) * s
        } else {
            if (xaxis.x > yaxis.y && xaxis.x > zaxis.z) {
                val s:Float = 0.5f / sqrt(1.0f + xaxis.x - yaxis.y - zaxis.z)
                rotation.w = (yaxis.z - zaxis.y) * s
                rotation.x = 0.25f / s
                rotation.y = (yaxis.x + xaxis.y) * s
                rotation.z = (zaxis.x + xaxis.z) * s
            } else if (yaxis.y > zaxis.z) {
                val s:Float = 0.5f / sqrt(1.0f + yaxis.y - xaxis.x - zaxis.z)
                rotation.w = (zaxis.x - xaxis.z) * s
                rotation.x = (yaxis.x + xaxis.y) * s
                rotation.y = 0.25f / s
                rotation.z = (zaxis.y + yaxis.z) * s
            } else {
                val s:Float = 0.5f / sqrt(1.0f + zaxis.z - xaxis.x - yaxis.y)
                rotation.w = (xaxis.y - yaxis.x) * s
                rotation.x = (zaxis.x + xaxis.z) * s
                rotation.y = (zaxis.y + yaxis.z) * s
                rotation.z = 0.25f / s
            }
        }

        return true
    }

    fun determinant():Float {
        val a0:Float = m[0] * m[5] - m[1] * m[4]
        val a1:Float = m[0] * m[6] - m[2] * m[4]
        val a2:Float = m[0] * m[7] - m[3] * m[4]
        val a3:Float = m[1] * m[6] - m[2] * m[5]
        val a4:Float = m[1] * m[7] - m[3] * m[5]
        val a5:Float = m[2] * m[7] - m[3] * m[6]
        val b0:Float = m[8] * m[13] - m[9] * m[12]
        val b1:Float = m[8] * m[14] - m[10] * m[12]
        val b2:Float = m[8] * m[15] - m[11] * m[12]
        val b3:Float = m[9] * m[14] - m[10] * m[13]
        val b4:Float = m[9] * m[15] - m[11] * m[13]
        val b5:Float = m[10] * m[15] - m[11] * m[14]

        return a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0
    }

    fun getScale(scale: Vec3) {
        decompose(scale, null, null)
    }




    fun add(scalar:Float):Mat4 {
        val ret:Mat4 = Mat4(ZERO)
        add(scalar, ret)
        return ret
    }

    fun addLocal(scalar: Float):Mat4 {
        add(scalar, this)
        return this
    }

    fun add(mat: Mat4):Mat4 {
        val ret = Mat4(ZERO)
        add(this, mat, ret)
        return ret
    }

    fun addLocal(mat: Mat4) {
        add(this, mat, this)
    }

    fun getTranslation(translation: Vec3) {
        decompose(null, null, translation)
    }

    fun getUpVector(dst: Vec3) {
        dst.x = -m[4]
        dst.y = -m[5]
        dst.z = -m[6]
    }

    fun getDownVector(dst: Vec3) {
        dst.x = m[4]
        dst.y = m[5]
        dst.z = m[6]
    }

    fun getLeftVector(dst: Vec3) {
        dst.x = -m[0]
        dst.y = -m[1]
        dst.z = -m[2]
    }

    fun getRightVector(dst: Vec3) {
        dst.x = m[0]
        dst.y = m[1]
        dst.z = m[2]
    }

    fun getForwardVector(dst: Vec3) {
        dst.x = -m[8]
        dst.y = -m[9]
        dst.z = -m[10]
    }

    fun getBackVector(dst: Vec3) {
        dst.x = m[8]
        dst.y = m[9]
        dst.z = m[10]
    }

    fun inverse():Boolean {
        val a0:Float = m[0] * m[5] - m[1] * m[4]
        val a1:Float = m[0] * m[6] - m[2] * m[4]
        val a2:Float = m[0] * m[7] - m[3] * m[4]
        val a3:Float = m[1] * m[6] - m[2] * m[5]
        val a4:Float = m[1] * m[7] - m[3] * m[5]
        val a5:Float = m[2] * m[7] - m[3] * m[6]
        val b0:Float = m[8] * m[13] - m[9] * m[12]
        val b1:Float = m[8] * m[14] - m[10] * m[12]
        val b2:Float = m[8] * m[15] - m[11] * m[12]
        val b3:Float = m[9] * m[14] - m[10] * m[13]
        val b4:Float = m[9] * m[15] - m[11] * m[13]
        val b5:Float = m[10] * m[15] - m[11] * m[14]

        val det:Float = a0 * b5 - a1 * b4 + a2 * b3 + a3 * b2 - a4 * b1 + a5 * b0

        if (abs(det)<= MATH_TOLERANCE) return false

        val inverse:Mat4 = Mat4()
        inverse.m[0] = m[5] * b5 - m[6] * b4 + m[7] * b3
        inverse.m[1] = -m[1] * b5 + m[2] * b4 - m[3] * b3
        inverse.m[2] = m[13] * a5 - m[14] * a4 + m[15] * a3
        inverse.m[3] = -m[9] * a5 + m[10] * a4 - m[11] * a3

        inverse.m[4] = -m[4] * b5 + m[6] * b2 - m[7] * b1
        inverse.m[5] = m[0] * b5 - m[2] * b2 + m[3] * b1
        inverse.m[6] = -m[12] * a5 + m[14] * a2 - m[15] * a1
        inverse.m[7] = m[8] * a5 - m[10] * a2 + m[11] * a1

        inverse.m[8] = m[4] * b4 - m[5] * b2 + m[7] * b0
        inverse.m[9] = -m[0] * b4 + m[1] * b2 - m[3] * b0
        inverse.m[10] = m[12] * a4 - m[13] * a2 + m[15] * a0
        inverse.m[11] = -m[8] * a4 + m[9] * a2 - m[11] * a0

        inverse.m[12] = -m[4] * b3 + m[5] * b1 - m[6] * b0
        inverse.m[13] = m[0] * b3 - m[1] * b1 + m[2] * b0
        inverse.m[14] = -m[12] * a3 + m[13] * a1 - m[14] * a0
        inverse.m[15] = m[8] * a3 - m[9] * a1 + m[10] * a0

        multiply(inverse, 1f/det, this)

        return true
    }

    fun getInversed():Mat4 {
        val mat:Mat4 = Mat4(this)
        mat.inverse()
        return mat
    }

    fun isIdentity():Boolean {
        return Arrays.equals(this.m, IDENTITY.m)
    }

    fun multiply(mat: Mat4) {
        multiply(this, mat, this)
    }

    fun multiply(m1: Mat4, m2: Mat4, dst: Mat4) {
        MathUtilC.multiplyMatrix(m1.m, m2.m, dst.m)
    }

    fun multiply(scalar: Float) {
        multiply(scalar, this)
    }

    fun multiply(scalar: Float, dst: Mat4) {
        multiply(this, scalar, dst)
    }

    fun multiply(mat: Mat4, scalar: Float, dst: Mat4) {
        MathUtilC.multiplyMatrix(mat.m, scalar, dst.m)
    }

    fun multiplyRet(mat: Mat4):Mat4 {
        val result:Mat4 = Mat4(this)
        result.multiply(mat)
        return result
    }

    fun negate() { MathUtilC.negateMatrix(m, m) }

    fun getNegate():Mat4 {
        val mat:Mat4 = Mat4(this)
        mat.negate()
        return mat
    }

    fun rotate(q: Quaternion) { rotate(q, this)}

    fun rotate(q: Quaternion, dst: Mat4) {
        val r:Mat4 = Mat4()
        createRotation(q, r)
        multiply(this, r, dst)
    }

    fun rotate(axis:Vec3, angle: Float) { rotate(axis, angle, this) }

    fun rotate(axis: Vec3, angle: Float, dst:Mat4) {
        val r:Mat4 = Mat4()
        createRotation(axis, angle, r)
        multiply(this, r, dst)
    }

    fun rotateX(angle: Float) { rotateX(angle, this) }

    fun rotateX(angle: Float, dst: Mat4) {
        val r:Mat4 = Mat4()
        createRotationX(angle, r)
        multiply(this, r, dst)
    }

    fun rotateY(angle: Float) { rotateY(angle, this) }

    fun rotateY(angle: Float, dst: Mat4) {
        val r:Mat4 = Mat4()
        createRotationY(angle, r)
        multiply(this, r, dst)
    }

    fun rotateZ(angle: Float) { rotateZ(angle, this) }

    fun rotateZ(angle: Float, dst: Mat4) {
        val r:Mat4 = Mat4()
        createRotationZ(angle, r)
        multiply(this, r, dst)
    }

    fun scale(value: Float) { scale(value, this) }

    fun scale(value: Float, dst: Mat4) { scale(value, value, value, dst) }

    fun scale(xScale: Float, yScale: Float, zScale: Float) {
        scale(xScale, yScale, zScale, this)
    }

    fun scale(xScale: Float, yScale: Float, zScale: Float, dst: Mat4) {
        val s:Mat4 = Mat4()
        createScale(xScale, yScale, zScale, dst)
        multiply(this, s, dst)
    }

    fun setIdentity() {
        OpenGlUtils.copyMatrix(m, IDENTITY.m, 16)
    }

    fun setZero() {Arrays.fill(m, 0f)}

    fun subtract(mat: Mat4) { subtract(this, mat, this) }

    fun transformPoint(point:Vec3) { transformVector(point.x, point.y, point.z, 1f, point) }

    fun transformPoint(point:Vec3, dst: Vec3) { transformVector(point.x, point.y, point.z, 1f, dst) }

    fun transformVector(vector: Vec3) { transformVector(vector.x, vector.y, vector.z, 1f, vector) }

    fun transformVector(x:Float, y:Float, z:Float, w:Float, dst: Vec3) {
        MathUtilC.transformVec4(m, x, y, z, w, dst)
    }

    fun transformVector(vec4: Vec4) { transformVector(vec4, vec4) }

    fun transformVector(vec4: Vec4, dst: Vec4) {
        MathUtilC.transformVec4(m, vec4, dst)
    }

    fun translate(x: Float, y: Float, z: Float) { translate(x, y, z, this) }

    fun translate(x: Float, y: Float, z: Float, dst: Mat4) {
        val t:Mat4 = Mat4()
        createTranslation(x, y, z, t)
        multiply(this, t, dst)
    }

    fun translate(t:Vec3) {translate(t.x, t.y, t.z, this)}

    fun translate(t: Vec3, dst: Mat4) { translate(t.x, t.y, t.z, dst) }

    fun transpose() {MathUtilC.transposeMatrix(m, m)}

    fun getTransposed():Mat4 {
        val mat:Mat4 = Mat4(this)
        mat.transpose()
        return mat
    }
}