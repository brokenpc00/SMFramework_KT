package com.brokenpc.smframework_kt.sprite.sspack

class MiscHelper {
    companion object {
        fun findNextPowerOfTwo(value: Int): Int {
            var k = value
            k--
            var i = 1
            while (i<(4*8)) {
                k = k or k shr i
                i = i shl 1
            }

            return k + 1
        }
    }
}