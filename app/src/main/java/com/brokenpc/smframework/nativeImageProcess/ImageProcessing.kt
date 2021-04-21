package com.brokenpc.smframework.nativeImageProcess

import android.graphics.Bitmap

class ImageProcessing {

    companion object {
        init {
            System.loadLibrary("SMFramework_ImgPrc")
        }

        @JvmStatic
        external fun testJniCall(): String?

        @JvmStatic
        external fun decodeRGBAnative(encoded: ByteArray?, encodedLength: Long, width: IntArray?, height: IntArray?): ByteArray?

        // for text
        @JvmStatic
        external fun stringFromJNI():String?

        @JvmStatic
        external fun callTest()

        // Image Process
        @JvmStatic
        external fun glGrabPixles(x:Int, y:Int, bitmap: Bitmap?, zeroNonVisiblePixels:Boolean)

        // util
        @JvmStatic
        external fun exitApp()

    }



}