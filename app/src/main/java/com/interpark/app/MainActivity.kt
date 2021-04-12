package com.brokenpc.app

import android.graphics.Point
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.brokenpc.smframework.ClassHelper
import com.brokenpc.smframework.SMSurfaceView
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.app.scene.HelloBrokenpcScene
import com.brokenpc.smframework.base.SMScene

class MainActivity : FragmentActivity(), ClassHelper.HelperListener {

    private var _surfaceView:SMSurfaceView? = null
    private var _displayRawWidth:Int = 0
    private var _displayRawHeight:Int = 0

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val version:Int = Build.VERSION.SDK_INT
//        if (version>=13) {
//            var size:Point = Point()
//            display!!.getRealSize(size)
//            _displayRawWidth = size.x;
//            _displayRawHeight = size.y;
//        } else {
            var displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            _displayRawWidth = displayMetrics.widthPixels
            _displayRawHeight = displayMetrics.heightPixels
//        }

        _surfaceView = SMSurfaceView(this)
        addContentView(_surfaceView, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        _surfaceView?.getDirector()?.setDisplayRawSize(_displayRawWidth, _displayRawHeight)
        ClassHelper.init(this)

        val sceneParam:SceneParams = SceneParams()

        val scene = HelloBrokenpcScene.create(_surfaceView!!.getDirector()!!, sceneParam, SMScene.SwipeType.MENU)
        _surfaceView!!.startSMFrameWorkScene(scene)

        //DisplayMetrics
    }

    override fun runOnGLThread(runnable: Runnable) {
        _surfaceView?.queueEvent(runnable)
        }

    override fun showDialog(title: String, message: String) {

    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        _surfaceView?.saveInstanceState(outState)
    }

    override fun onBackPressed() {
        if (_surfaceView==null || _surfaceView?.onBackPressed()==false) {
            super.onBackPressed()
        }
    }
}