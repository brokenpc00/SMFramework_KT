package com.brokenpc.smframework.base.types

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class Rect : Cloneable {
    companion object {
        val ZERO:Rect = Rect(0f, 0f, 0f, 0f)
    }

    constructor() {
        set(0f, 0f, 0f, 0f)
    }
    constructor(p:Vec2, s:Size) {
        set(p, s)
    }
    constructor(r:Rect) {
        set(r)
    }
    constructor(x:Float, y: Float, width: Float, height: Float) {
        set(x, y, width, height)
    }

    fun set(r:Rect) {
        origin.set(r.origin)
        size.set(r.size)
    }
    fun set(p:Vec2, s:Size) {
        origin.set(p)
        size.set(s)
    }
    fun set(x:Float, y:Float, width:Float, height:Float) {
        origin.set(x, y)
        size.set(width, height)
    }

    var size:Size = Size()
    var origin:Vec2 = Vec2()

    fun equal(rect: Rect):Boolean {
        return origin.equal(rect.origin) && size.equal(rect.size)
    }

    fun getMaxX():Float {return origin.x+size.width}
    fun getMaxY():Float {return origin.y+size.height}
    fun getMinX():Float {return origin.x}
    fun getMinY():Float {return origin.y}
    fun getMidX():Float {return origin.x+size.width/2}
    fun getMidY():Float {return origin.y+size.height/2}

    fun containsPoint(point:Vec2):Boolean {
        return (point.x>=getMinX() && point.x<=getMaxX() && point.y>=getMinY() && point.y<=getMaxY())
    }

    fun intersectsRect(rect: Rect):Boolean {
        return !(getMaxX()<rect.getMinX() || getMaxY()<rect.getMinY() || getMinX()>rect.getMinX() || getMinY()>rect.getMinY())
    }

    fun intersectsCircle(center:Vec2, radius:Float):Boolean {
        val rectangleCenter:Vec2 = Vec2((origin.x+size.width)/2, (origin.y+size.height)/2)
        val w:Float = size.width/2
        val h:Float = size.height/2

        val dx:Float = abs(center.x - rectangleCenter.x)
        val dy:Float = abs(center.y - rectangleCenter.y)

        if (dx>(radius+w) || dy>(radius+h)) return false

        val circleDistance:Vec2 = Vec2(abs(center.x-origin.x-w), abs(center.y-origin.y-h))
        if (circleDistance.x<=w) return true
        if (circleDistance.y<=h) return true

        val cornerDistanceSq:Float = (circleDistance.x-w).toDouble().pow(2).toFloat() + (circleDistance.y-h).toDouble().pow(2).toFloat()
        return cornerDistanceSq <= radius.toDouble().pow(2)
    }

    fun merge(rect: Rect) {
        val minX:Float = min(getMinX(), rect.getMinX())
        val minY:Float = min(getMinY(), rect.getMinY())
        val maxX:Float = max(getMaxX(), rect.getMaxX())
        val maxY:Float = max(getMaxY(), rect.getMaxY())
        set(minX, minY, maxX-minX, maxY-minY)
    }

    fun unionWithRect(rect: Rect):Rect {
        var thisLeftX:Float = origin.x
        var thisRightX:Float = origin.x+size.width
        var thisTopY:Float = origin.y
        var thisBottomY:Float = origin.y+size.height

        if (thisRightX<thisLeftX) {
           val tmp:Float = thisRightX
            thisRightX = thisLeftX
            thisLeftX = tmp
        }
        if (thisBottomY<thisTopY) {
            val tmp:Float = thisBottomY
            thisBottomY = thisTopY
            thisTopY = tmp
        }

        var otherLeftX:Float = rect.origin.x
        var otherRightX:Float = rect.origin.x+rect.size.width
        var otherTopY:Float = rect.origin.y
        var otherBottomY:Float = rect.origin.y+rect.size.height

        if (otherRightX<otherLeftX) {
            val tmp:Float = otherRightX
            otherRightX = otherLeftX
            otherLeftX = tmp
        }

        if (otherBottomY<otherTopY) {
            val tmp:Float = otherBottomY
            otherBottomY = otherTopY
            otherTopY = tmp
        }

        val combinedLeftX:Float = min(thisLeftX, otherLeftX)
        val combinedRightX:Float = max(thisRightX, otherRightX)
        val combinedTopY:Float = min(thisTopY, otherTopY)
        val combinedBottomY:Float = max(thisBottomY, otherBottomY)
        return Rect(combinedLeftX, combinedTopY, combinedRightX-combinedLeftX, combinedBottomY-combinedTopY)
    }

    fun Clone():Rect {return Rect(this)}
}