package com.brokenpc.smframework.base.types

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class AffineTransform {

    constructor() {
        set()
    }
    constructor(t: AffineTransform) {
        set(t)
    }

    fun set() {
        a = 0f
        b = 0f
        c = 0f
        d = 0f
        tx = 0f
        ty = 0f
    }
    fun set(t: AffineTransform) {
        this.a = t.a
        this.b = t.b
        this.c = t.c
        this.d = t.d
        this.tx = t.tx
        this.ty = t.ty
    }


    companion object {
        @JvmStatic
        fun CGAffineToGL(t:AffineTransform, m:FloatArray) {
            m[0] = t.a
            m[1] = t.b
            m[2] = 0f
            m[3] = 0f

            m[4] = t.c
            m[5] = t.d
            m[6] = 0f
            m[7] = 0f

            m[8] = 0f
            m[9] = 0f
            m[10] = 1f
            m[11] = 0f

            m[12] = t.tx
            m[13] = t.ty
            m[14] = 0f
            m[15] = 1f
        }

        @JvmStatic
        fun GLToCGAffine(m:FloatArray, t:AffineTransform) {
            t.a = m[0]
            t.b = m[1]
            t.c = m[4]
            t.d = m[5]
            t.tx = m[12]
            t.ty = m[13]
        }

        @JvmStatic
        fun AffineTransformMakeIdentity():AffineTransform {
            return __CCAffineTransformMake(1f, 0f, 0f, 1f, 0f, 0f)
        }

        @JvmStatic
        fun __CCSizeApplyAffineTransform(size:Size, t:AffineTransform):Size {
            val s:Size = Size()
            s.width = t.a*size.width + t.c*size.height
            s.height = t.b*size.width + t.d*size.height
            return s
        }

        @JvmStatic
        fun RectApplyAffineTransform(rect: Rect, anAffineTransform: AffineTransform):Rect {
            val top:Float = rect.getMinY()
            val left:Float = rect.getMinX()
            val right:Float = rect.getMaxX()
            val bottom:Float = rect.getMaxY()

            val topLeft:Vec2 = __CCPointApplyAffineTransform(Vec2(left, top), anAffineTransform)
            val topRight:Vec2 = __CCPointApplyAffineTransform(Vec2(right, top), anAffineTransform)
            val bottomLeft:Vec2 = __CCPointApplyAffineTransform(Vec2(left, bottom), anAffineTransform)
            val bottomRight:Vec2 = __CCPointApplyAffineTransform(Vec2(right, bottom), anAffineTransform)

            val minX:Float = min(min(topLeft.x, topRight.x), min(bottomLeft.x, bottomRight.x))
            val maxX:Float = max(max(topLeft.x, topRight.x), max(bottomLeft.x, bottomRight.x))
            val minY:Float = min(min(topLeft.y, topRight.y), min(bottomLeft.y, bottomRight.y))
            val maxY:Float = max(max(topLeft.y, topRight.y), max(bottomLeft.y, bottomRight.y))

            return Rect(minX, minY, maxX-minX, maxY-minY)
        }

        @JvmStatic
        fun RectApplyTransform(rect: Rect, transform: Mat4):Rect {
            val top:Float = rect.getMinY()
            val left:Float = rect.getMinX()
            val bottom:Float = rect.getMaxY()
            val right:Float = rect.getMaxX()

            val topLeft:Vec3 = Vec3(left, top, 0f)
            val topRight:Vec3 = Vec3(right, top, 0f)
            val bottomLeft:Vec3 = Vec3(left, bottom, 0f)
            val bottomRight:Vec3 = Vec3(right, bottom, 0f)

            transform.transformPoint(topLeft)
            transform.transformPoint(topRight)
            transform.transformPoint(bottomLeft)
            transform.transformPoint(bottomRight)

            val minX = min(min(topLeft.x, topRight.x), min(bottomLeft.x, bottomRight.x))
            val minY = min(min(topLeft.y, topRight.y), min(bottomLeft.y, bottomRight.y))
            val maxX = max(max(topLeft.x, topRight.x), max(bottomLeft.x, bottomRight.x))
            val maxY = max(max(topLeft.y, topRight.y), max(bottomLeft.y, bottomRight.y))

            return Rect(minX, minY, maxX-minX, maxY-minY)
        }

        @JvmStatic
        fun __CCAffineTransformMake(a:Float, b:Float, c:Float, d:Float, tx:Float, ty:Float):AffineTransform {
            val t:AffineTransform = AffineTransform()
            t.a = a
            t.b = b
            t.c = c
            t.d = d
            t.tx = tx
            t.ty = ty
            return t
        }

        @JvmStatic
        fun __CCPointApplyAffineTransform(point:Vec2, t:AffineTransform):Vec2 {
            val p:Vec2 = Vec2()
            p.x = t.a*point.x + t.c*point.y + t.tx
            p.y = t.b*point.x + t.d*point.y + t.ty
            return p
        }

        @JvmStatic
        fun PointApplyTransform(point: Vec2, transform: Mat4):Vec2 {
            val vec:Vec3 = Vec3(point.x, point.y, 0f)
            transform.transformPoint(vec)
            return Vec2(vec.x, vec.y)
        }

        @JvmStatic
        fun AffineTransformTranslate(t: AffineTransform, tx: Float, ty: Float):AffineTransform {
            return __CCAffineTransformMake(t.a, t.b, t.c, t.d, t.tx + t.a * tx + t.c * ty, t.ty + t.b * tx + t.d * ty)
        }

        @JvmStatic
        fun AffineTransformRotate(t: AffineTransform, angle: Float):AffineTransform {
            val sine:Float = sin(angle)
            val cosine:Float = cos(angle)

            return __CCAffineTransformMake(    t.a * cosine + t.c * sine,
                t.b * cosine + t.d * sine,
                t.c * cosine - t.a * sine,
                t.d * cosine - t.b * sine,
                t.tx,
                t.ty)
        }

        @JvmStatic
        fun AffineTransformScale(t: AffineTransform, sx: Float, sy:Float): AffineTransform {
            return __CCAffineTransformMake(t.a * sx, t.b * sx, t.c * sy, t.d * sy, t.tx, t.ty)
        }

        @JvmStatic
        fun AffineTransformConcat(t1: AffineTransform, t2: AffineTransform): AffineTransform {
            return __CCAffineTransformMake(    t1.a * t2.a + t1.b * t2.c, t1.a * t2.b + t1.b * t2.d, //a,b
                t1.c * t2.a + t1.d * t2.c, t1.c * t2.b + t1.d * t2.d, //c,d
                t1.tx * t2.a + t1.ty * t2.c + t2.tx,                  //tx
                t1.tx * t2.b + t1.ty * t2.d + t2.ty)
        }

        @JvmStatic
        fun AffineTransformEqualToTransform(t1: AffineTransform, t2: AffineTransform):Boolean {
            return (t1.a == t2.a && t1.b == t2.b && t1.c == t2.c && t1.d == t2.d && t1.tx == t2.tx && t1.ty == t2.ty)
        }

        @JvmStatic
        fun AffineTransformInvert(t: AffineTransform):AffineTransform {
            val determinant:Float = 1f / (t.a * t.d - t.b * t.c)
            return __CCAffineTransformMake(determinant * t.d, -determinant * t.b, -determinant * t.c, determinant * t.a,
                determinant * (t.c * t.ty - t.d * t.tx), determinant * (t.b * t.tx - t.a * t.ty) )
        }

        @JvmStatic
        fun TransformConcat(t1: Mat4, t2: Mat4):Mat4 { return t1.multiplyRet(t2)}
    }

    val IDENTITY:AffineTransform = AffineTransformMakeIdentity()
    val AffineTransformIdentity:AffineTransform = AffineTransformMakeIdentity()


    var a:Float = 0f
    var b:Float = 0f
    var c:Float = 0f
    var d:Float = 0f
    var tx:Float = 0f
    var ty:Float = 0f
}