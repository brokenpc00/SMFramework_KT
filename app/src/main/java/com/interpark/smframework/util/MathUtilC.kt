package com.interpark.smframework.util

import com.interpark.smframework.base.types.Vec3
import com.interpark.smframework.base.types.Vec4

class MathUtilC {
    companion object {
        @JvmStatic
        fun addMatrix(m:FloatArray, scalar:Float, dst:FloatArray) {
            dst[0] = m[0] + scalar
            dst[1] = m[1] + scalar
            dst[2] = m[2] + scalar
            dst[3] = m[3] + scalar
            dst[4] = m[4] + scalar
            dst[5] = m[5] + scalar
            dst[6] = m[6] + scalar
            dst[7] = m[7] + scalar
            dst[8] = m[8] + scalar
            dst[9] = m[9] + scalar
            dst[10] = m[10] + scalar
            dst[11] = m[11] + scalar
            dst[12] = m[12] + scalar
            dst[13] = m[13] + scalar
            dst[14] = m[14] + scalar
            dst[15] = m[15] + scalar
        }

        @JvmStatic
        fun addMatrix(m1: FloatArray, m2:FloatArray, dst: FloatArray) {
            dst[0] = m1[0] + m2[0]
            dst[1] = m1[1] + m2[1]
            dst[2] = m1[2] + m2[2]
            dst[3] = m1[3] + m2[3]
            dst[4] = m1[4] + m2[4]
            dst[5] = m1[5] + m2[5]
            dst[6] = m1[6] + m2[6]
            dst[7] = m1[7] + m2[7]
            dst[8] = m1[8] + m2[8]
            dst[9] = m1[9] + m2[9]
            dst[10] = m1[10] + m2[10]
            dst[11] = m1[11] + m2[11]
            dst[12] = m1[12] + m2[12]
            dst[13] = m1[13] + m2[13]
            dst[14] = m1[14] + m2[14]
            dst[15] = m1[15] + m2[15]
        }

        @JvmStatic
        fun subtractMatrix(m1: FloatArray, m2: FloatArray, dst: FloatArray) {
            dst[0] = m1[0] - m2[0]
            dst[1] = m1[1] - m2[1]
            dst[2] = m1[2] - m2[2]
            dst[3] = m1[3] - m2[3]
            dst[4] = m1[4] - m2[4]
            dst[5] = m1[5] - m2[5]
            dst[6] = m1[6] - m2[6]
            dst[7] = m1[7] - m2[7]
            dst[8] = m1[8] - m2[8]
            dst[9] = m1[9] - m2[9]
            dst[10] = m1[10] - m2[10]
            dst[11] = m1[11] - m2[11]
            dst[12] = m1[12] - m2[12]
            dst[13] = m1[13] - m2[13]
            dst[14] = m1[14] - m2[14]
            dst[15] = m1[15] - m2[15]
        }

        @JvmStatic
        fun multiplyMatrix(m: FloatArray, scalar: Float, dst: FloatArray) {
            dst[0] = m[0] * scalar
            dst[1] = m[1] * scalar
            dst[2] = m[2] * scalar
            dst[3] = m[3] * scalar
            dst[4] = m[4] * scalar
            dst[5] = m[5] * scalar
            dst[6] = m[6] * scalar
            dst[7] = m[7] * scalar
            dst[8] = m[8] * scalar
            dst[9] = m[9] * scalar
            dst[10] = m[10] * scalar
            dst[11] = m[11] * scalar
            dst[12] = m[12] * scalar
            dst[13] = m[13] * scalar
            dst[14] = m[14] * scalar
            dst[15] = m[15] * scalar
        }

        @JvmStatic
        fun multiplyMatrix(m1: FloatArray, m2: FloatArray, dst: FloatArray) {
            dst[0] = m1[0] * m2[0] + m1[4] * m2[1] + m1[8] * m2[2] + m1[12] * m2[3]
            dst[1] = m1[1] * m2[0] + m1[5] * m2[1] + m1[9] * m2[2] + m1[13] * m2[3]
            dst[2] = m1[2] * m2[0] + m1[6] * m2[1] + m1[10] * m2[2] + m1[14] * m2[3]
            dst[3] = m1[3] * m2[0] + m1[7] * m2[1] + m1[11] * m2[2] + m1[15] * m2[3]
            dst[4] = m1[0] * m2[4] + m1[4] * m2[5] + m1[8] * m2[6] + m1[12] * m2[7]
            dst[5] = m1[1] * m2[4] + m1[5] * m2[5] + m1[9] * m2[6] + m1[13] * m2[7]
            dst[6] = m1[2] * m2[4] + m1[6] * m2[5] + m1[10] * m2[6] + m1[14] * m2[7]
            dst[7] = m1[3] * m2[4] + m1[7] * m2[5] + m1[11] * m2[6] + m1[15] * m2[7]
            dst[8] = m1[0] * m2[8] + m1[4] * m2[9] + m1[8] * m2[10] + m1[12] * m2[11]
            dst[9] = m1[1] * m2[8] + m1[5] * m2[9] + m1[9] * m2[10] + m1[13] * m2[11]
            dst[10] = m1[2] * m2[8] + m1[6] * m2[9] + m1[10] * m2[10] + m1[14] * m2[11]
            dst[11] = m1[3] * m2[8] + m1[7] * m2[9] + m1[11] * m2[10] + m1[15] * m2[11]
            dst[12] = m1[0] * m2[12] + m1[4] * m2[13] + m1[8] * m2[14] + m1[12] * m2[15]
            dst[13] = m1[1] * m2[12] + m1[5] * m2[13] + m1[9] * m2[14] + m1[13] * m2[15]
            dst[14] = m1[2] * m2[12] + m1[6] * m2[13] + m1[10] * m2[14] + m1[14] * m2[15]
            dst[15] = m1[3] * m2[12] + m1[7] * m2[13] + m1[11] * m2[14] + m1[15] * m2[15]
        }

        @JvmStatic
        fun negateMatrix(m: FloatArray, dst: FloatArray) {
            dst[0] = -m[0]
            dst[1] = -m[1]
            dst[2] = -m[2]
            dst[3] = -m[3]
            dst[4] = -m[4]
            dst[5] = -m[5]
            dst[6] = -m[6]
            dst[7] = -m[7]
            dst[8] = -m[8]
            dst[9] = -m[9]
            dst[10] = -m[10]
            dst[11] = -m[11]
            dst[12] = -m[12]
            dst[13] = -m[13]
            dst[14] = -m[14]
            dst[15] = -m[15]
        }

        @JvmStatic
        fun transposeMatrix(m: FloatArray, dst: FloatArray) {
            dst[0] = m[0]
            dst[1] = m[4]
            dst[2] = m[8]
            dst[3] = m[12]
            dst[4] = m[1]
            dst[5] = m[5]
            dst[6] = m[9]
            dst[7] = m[13]
            dst[8] = m[2]
            dst[9] = m[6]
            dst[10] = m[10]
            dst[11] = m[14]
            dst[12] = m[3]
            dst[13] = m[7]
            dst[14] = m[11]
            dst[15] = m[15]
        }

        @JvmStatic
        fun transformVec4(m: FloatArray, v: Vec4, dst: Vec4) {
            val x: Float = v.x * m[0] + v.y * m[4] + v.z * m[8] + v.w * m[12]
            val y: Float = v.x * m[1] + v.y * m[5] + v.z * m[9] + v.w * m[13]
            val z: Float = v.x * m[2] + v.y * m[6] + v.z * m[10] + v.w * m[14]
            val w: Float = v.x * m[3] + v.y * m[7] + v.z * m[11] + v.w * m[15]
            dst.x = x
            dst.y = y
            dst.z = z
            dst.w = w
        }

        @JvmStatic
        fun transformVec4(m: FloatArray, x: Float, y: Float, z: Float, w: Float, dst: Vec3) {
            dst.x = x * m[0] + y * m[4] + z * m[8] + w * m[12]
            dst.y = x * m[1] + y * m[5] + z * m[9] + w * m[13]
            dst.z = x * m[2] + y * m[6] + z * m[10] + w * m[14]
        }

        @JvmStatic
        fun transformVec4(m: FloatArray, x: Float, y: Float, z: Float, w: Float, dst: FloatArray) {
            dst[0] = x * m[0] + y * m[4] + z * m[8] + w * m[12]
            dst[1] = x * m[1] + y * m[5] + z * m[9] + w * m[13]
            dst[2] = x * m[2] + y * m[6] + z * m[10] + w * m[14]
        }

        @JvmStatic
        fun transformVec4(m: FloatArray, v: FloatArray, dst: FloatArray) {
            val x = v[0] * m[0] + v[1] * m[4] + v[2] * m[8] + v[3] * m[12]
            val y = v[0] * m[1] + v[1] * m[5] + v[2] * m[9] + v[3] * m[13]
            val z = v[0] * m[2] + v[1] * m[6] + v[2] * m[10] + v[3] * m[14]
            val w = v[0] * m[3] + v[1] * m[7] + v[2] * m[11] + v[3] * m[15]
            dst[0] = x
            dst[1] = y
            dst[2] = z
            dst[3] = w
        }

        @JvmStatic
        fun crossVec3(v1: FloatArray, v2: FloatArray, dst: FloatArray) {
            val x = v1[1] * v2[2] - v1[2] * v2[1]
            val y = v1[2] * v2[0] - v1[0] * v2[2]
            val z = v1[0] * v2[1] - v1[1] * v2[0]
            dst[0] = x
            dst[1] = y
            dst[2] = z
        }
    }
}