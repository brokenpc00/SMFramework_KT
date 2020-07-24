package com.interpark.smframework.nativeImageProcess

import android.graphics.Bitmap

class ImageProcessing {

    companion object {
        init {
            System.loadLibrary("SMFramework_ImgPrc")
        }
    }

    // for text
    external fun stringFromJNI():String?
    external fun callTest()

    // Image Process
    external fun glGrabPixles(x:Int, y:Int, bitmap: Bitmap?, zeroNonVisiblePixels:Boolean)

    // util
    external fun exitApp()

}