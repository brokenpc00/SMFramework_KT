package com.brokenpc.smframework_kt.sprite.sspack

import android.graphics.Point
import android.graphics.Rect
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class RectanglePacker(packingAreaWidth: Int, packingAreaHeight: Int) {
    private val _packedRects: ArrayList<Rect> = ArrayList()
    private val _anchors: ArrayList<Point> = ArrayList()

    private val _packingAreaWidth = packingAreaWidth
    private val _packingAreaHeight = packingAreaHeight

    private var _actualPackingAreaWidth = 1
    private var _actualPackingAreaHeight = 1

    companion object {
        val _sAnchorRankComparator:Comparator<Point> = AnchorRankComparer()
    }

    class AnchorRankComparer() : Comparator<Point> {
        override fun compare(left: Point, right: Point): Int {
            return (left.x + left.y) - (right.x + right.y)
        }
    }

    init {
        _anchors.add(Point(0, 0))
    }

    @Throws(OutOfSpaceException::class)
    fun pack(rectWidth: Int, rectHeight: Int): Point {
        val pt = Point(0, 0)

        if (!tryPack(rectWidth, rectHeight, pt)) {
            throw OutOfSpaceException("[[[[[ Rectangle does not fit in packing area")
        }

        return pt
    }

    private fun tryPack(rectWidth: Int, rectHeight: Int, pt: Point): Boolean {
        val anchorIndex = selectAnchorRecursive(rectWidth, rectHeight, _actualPackingAreaWidth, _actualPackingAreaHeight)

        if (anchorIndex==-1) {
            pt.x = 0
            pt.y = 0
            return false
        }

        val anchor = _anchors[anchorIndex]
        pt.x = anchor.x
        pt.y = anchor.y

        optimizePlacement(pt, rectWidth, rectHeight)

        val blockAnchor = ((pt.x + rectWidth) > anchor.x) && ((pt.y + rectHeight) > anchor.y)

        if (blockAnchor) {
            _anchors.removeAt(anchorIndex)
        }

        insertAnchor(Point(pt.x+rectWidth, pt.y))
        insertAnchor(Point(pt.x, pt.y+rectHeight))

        _packedRects.add(Rect(pt.x, pt.y, pt.x+rectWidth, pt.y+rectHeight))

        return true
    }

    private fun optimizePlacement(pt: Point, rectWidth: Int, rectHeight: Int) {
        val rect = Rect(pt.x, pt.y, pt.x+rectWidth, pt.y+rectHeight)

        var left = pt.x
        while (isFree(rect, _packingAreaWidth, _packingAreaHeight)) {
            left = rect.left
            --rect.left
            --rect.right
        }

        rect.left = pt.x
        rect.right = pt.x+rectWidth

        var top = pt.y
        while (isFree(rect, _packingAreaWidth, _packingAreaHeight)) {
            top = rect.top
            --rect.top
            --rect.bottom
        }

        if ((pt.x - left) > (pt.y - top)) {
            pt.x = left
        } else {
            pt.y = top
        }
    }

    private fun selectAnchorRecursive(rectWidth: Int, rectHeight: Int, testedPackingAreaWidth: Int, testedPackingAreaHeight: Int): Int {
        val freeAnchorIndex = findFirstFreeAnchor(rectWidth, rectHeight, testedPackingAreaWidth, testedPackingAreaHeight)

        if (freeAnchorIndex!=-1) {
            _actualPackingAreaWidth = testedPackingAreaWidth
            _actualPackingAreaHeight = testedPackingAreaHeight
            return freeAnchorIndex
        }

        val canEnlargeWidth = (testedPackingAreaWidth < _packingAreaWidth)
        val canEnlargeHeight = (testedPackingAreaHeight < _packingAreaHeight)
        val shouldEnlargeHeight = (!canEnlargeWidth) || (testedPackingAreaHeight < testedPackingAreaWidth)

        if (canEnlargeHeight && shouldEnlargeHeight) {
            return selectAnchorRecursive(rectWidth, rectHeight, testedPackingAreaWidth, _packingAreaHeight.coerceAtMost(testedPackingAreaHeight*2))
        }
        if (canEnlargeWidth) {
            return selectAnchorRecursive(rectWidth, rectHeight, _packingAreaWidth.coerceAtMost(testedPackingAreaWidth*2), testedPackingAreaHeight)
        }

        return -1
    }

    private fun findFirstFreeAnchor(rectWidth: Int, rectHeight: Int, testedPackingAreaWidth: Int, testedPackingAreaHeight: Int): Int {
        val rect = Rect(0, 0, rectWidth, rectHeight)

        for (index in 0 until _anchors.size) {
            val anchor = _anchors[index]
            rect.set(anchor.x, anchor.y, anchor.x+rectWidth, anchor.y+rectHeight)

            if (isFree(rect, testedPackingAreaWidth, testedPackingAreaHeight)) {
                return index
            }
        }

        return -1
    }

    private fun isFree(rect: Rect, testedPackingAreaWidth: Int, testedPackingAreaHeight: Int): Boolean {
        val leavesPackingArea = (rect.left < 0) || (rect.top < 0) || (rect.right > testedPackingAreaWidth) || (rect.bottom > testedPackingAreaHeight)

        if (leavesPackingArea) return false

        for (index in 0 until _packedRects.size) {
            if (Rect.intersects(_packedRects[index], rect)) {
                return false
            }
        }

        return true
    }

    private fun insertAnchor(anchor: Point) {
        var insertIndex = Collections.binarySearch(_anchors, anchor, _sAnchorRankComparator)

        if (insertIndex < 0) {
            insertIndex = insertIndex.inv()
        }

        _anchors.add(insertIndex, anchor)
    }
}