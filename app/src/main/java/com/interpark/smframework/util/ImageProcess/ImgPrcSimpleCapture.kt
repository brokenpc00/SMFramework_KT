package com.interpark.smframework.util.ImageProcess

import com.brokenpc.smframework.base.SMView
import com.brokenpc.smframework.base.types.Vec2

class ImgPrcSimpleCapture : ImageProcessFunction {
    private var _scale: Float = 1.0f

    constructor(scale: Float) : super() {
        setCaptureOnly()
        _scale = scale
    }

    override fun onPreProcess(view: SMView?): Boolean {
        val size = view!!.getContentSize()
        return startProcess(view, size, size.divide(2f).toVec2(), Vec2.MIDDLE, _scale, _scale)
    }

    override fun onProcessInBackground(): Boolean {
        // capture only
        return true
    }
}