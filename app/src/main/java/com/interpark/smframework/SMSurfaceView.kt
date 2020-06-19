package com.interpark.smframework

import android.opengl.GLSurfaceView
import androidx.fragment.app.FragmentActivity

class SMSurfaceView : GLSurfaceView {

    var mActivity: FragmentActivity = FragmentActivity()

    constructor(activity: FragmentActivity) : super(activity) {

    }
}