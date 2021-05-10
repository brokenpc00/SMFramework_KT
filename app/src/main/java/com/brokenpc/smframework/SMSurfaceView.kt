package com.brokenpc.smframework

import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.types.Size

class SMSurfaceView : GLSurfaceView {

    private var _director:SMDirector? = null
    private var _softKeyShown:Boolean = false
    private var _multiTouch:Boolean = true

    fun isSoftKeyShown():Boolean {return _softKeyShown}
    fun setSoftKKeyShown(show:Boolean) {_softKeyShown = show}

    fun isMultiTouchEnable():Boolean {return _multiTouch}
    fun setMultiTouchEnable(enable:Boolean) {_multiTouch = enable}


    var _activity: FragmentActivity = FragmentActivity()

    constructor(activity: FragmentActivity) : super(activity, null) {
        val translucent = false
        _activity = activity
        init(activity, translucent)
    }

    constructor(activity: FragmentActivity, translucent:Boolean) : super(activity, null) {
        _activity = activity
        init(activity, translucent)
    }

    fun init(activity: FragmentActivity, translucent: Boolean) {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 0, 0)

        if (translucent) {
            holder.setFormat(PixelFormat.TRANSLUCENT)
        }

        _director = SMDirector(activity, this)

        setRenderer(_director)
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onResume() {
        _director?.onResume()

        super.onResume()
    }

    override fun onPause() {
        super.onPause()

        _director?.onPause()
    }

    fun getDirector():SMDirector? {return _director}

    fun startSMFrameWorkScene(scene:SMScene) {
        if (_director==null) {
            _director = SMDirector(_activity, this)

            setRenderer(_director)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        _director?.runWithScene(scene)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        _director!!.addTouchEvent(event)
        return true
    }

    fun onBackPressed():Boolean {
        val ret:Boolean = _director!!.onBackPressd()
        if (!ret) {
            _director = null
        }

        return ret
    }

    fun saveInstanceState(outState:Bundle) {

    }
}