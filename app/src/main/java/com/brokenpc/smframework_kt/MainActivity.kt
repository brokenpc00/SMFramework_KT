package com.brokenpc.smframework_kt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.brokenpc.smframework.ClassHelper
import com.brokenpc.smframework.SMSurfaceView
import com.brokenpc.smframework.base.SMScene
import com.brokenpc.smframework.base.SceneParams
import com.brokenpc.smframework.nativeImageProcess.ImageProcessing
import com.brokenpc.smframework_kt.scene.HelloBrokenpcScene

class MainActivity : AppCompatActivity(), ClassHelper.HelperListener {
    private var _surfaceView: SMSurfaceView? = null
    private var _displayRawWidth:Int = 0
    private var _displayRawHeight:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        // Example of a call to a native method
//        findViewById<TextView>(R.id.sample_text).text = ImageProcessing.stringFromJNI()

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

        val sceneParam: SceneParams = SceneParams()

        val scene = HelloBrokenpcScene.create(_surfaceView!!.getDirector()!!, sceneParam, SMScene.SwipeType.MENU)
        _surfaceView!!.startSMFrameWorkScene(scene)
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

    /**
     * A native method that is implemented by the 'SMFramework_ImgPrc' native library,
     * which is packaged with this application.
     */
    companion object {
        // Used to load the 'SMFramework_ImgPrc' library on application startup.
        init {
//            System.loadLibrary("SMFramework_ImgPrc")
        }
    }
}