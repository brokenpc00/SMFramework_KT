package com.brokenpc.smframework_kt.sprite.sspack

import android.graphics.Rect

class ResInfo : Comparable<ResInfo> {
    var resId: Int
    var align: Int
    var texId: Int
    var width: Int = 0
    var height: Int = 0
    var rect: Rect? = null

    constructor(resId: Int) {
        this.texId = 1
        this.resId = resId
        this.align = Align.CENTER_VERTICAL
    }

    constructor(resId: Int, align: Int) {
        this.texId = 1
        this.resId = resId
        this.align = align
    }

    class Align {
        companion object {
            const val LEFT = 0x01
            const val RIGHT = 0x02
            const val CENTER_HORIZONTAL = 0x00

            const val TOP = 0x10
            const val BOTTOM = 0x20
            const val CENTER_VERTICAL = 0x40
        }
    }

    class SizeComparator : Comparator<ResInfo> {
        override fun compare(o1: ResInfo, o2: ResInfo): Int {
            return (o1.width+o1.height) - (o2.width+o2.height)
        }
    }

    override fun compareTo(info: ResInfo): Int {
        return (info.width+info.height) - (width+height)
    }

}