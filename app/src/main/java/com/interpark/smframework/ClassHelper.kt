package com.brokenpc.smframework

import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.os.Vibrator
import java.io.File
import java.io.FilenameFilter
//import com.android.vending.expansion.zipfile
//import com.android.vending.expansion.zipfile.ZipResourceFile;
import com.brokenpc.app.MainActivity

//import com.android.vending.expansion.zipfile.APKExpansionSupport;

class ClassHelper {
    companion object {
        const val PREFS_NAME = "SMFrameWorkPrefFile"
        const val RUNNABLES_PER_FRAME = 5
        var TAG:String = ClassHelper.javaClass.simpleName

        private var _assetManager: AssetManager? = null
        private var _accelerometerEnabled = false
        private var _compassEnabled = false
        private var _activityVisible = false
        private var _packageName:String = ""
        private var _fileDirectory:String = ""
        private var _activity:Activity? = null
        private var _helperListener:HelperListener? = null
        private var _vibrateService:Vibrator? = null
        private var _assetsPath:String = ""
        private var _init:Boolean = false
//        private var _obbFile:ZipResourceFile? = null
//        private var _obbFile = APKExpansion
//        private var _obbFile:ZipResourceFile

        @JvmStatic
        fun getAssetManager():AssetManager? {return _assetManager}

        @JvmStatic
        fun getActivity():Activity? {return _activity}

        @JvmStatic
        fun init(activity: Activity) {
            _activity = activity
            ClassHelper._helperListener = activity as HelperListener
            if (!_init) {
                val pm:PackageManager = activity.packageManager
                val isSupportLowLatency:Boolean = pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)

                val sampleRate = 44100
                val bufferSizeInFrame = 192
                _fileDirectory = activity.filesDir.absolutePath
                _assetManager = activity.assets


                _init = true
            }
        }

        @JvmStatic
        fun isDirectoryExist(strPath:String):Boolean {
            val file:File = File(strPath)
            return file.isDirectory
        }

        @JvmStatic
        fun isFileExist(strPath: String):Boolean {
            val file:File = File(strPath)
            return file.isFile
        }

        @JvmStatic
        fun getAssetsPath():String {
            if (_assetsPath=="") {
                val pathToOBB:String = _activity!!.getExternalFilesDir(null)!!.absolutePath + "/Androiid/obb/" + ClassHelper._packageName

                val fileNames:Array<String>? = File(pathToOBB).list(object : FilenameFilter{
                    override fun accept(dir: File?, name: String?): Boolean {
                        return name!!.startsWith("main.") && name!!.endsWith(".obb")
                    }
                })

                var fullPathToOBB:String = ""
                if (fileNames!=null && fileNames.isNotEmpty()) {
                    fullPathToOBB = pathToOBB + "/" + fileNames[0]
                }

                val obbFile:File = File(fullPathToOBB)
                if (obbFile.exists()) {
                    _assetsPath = fullPathToOBB
                } else {
                    _assetsPath = _activity!!.applicationInfo.sourceDir
                }
            }

            return _assetsPath
        }

//        @JvmStatic
//        fun getObbFile():ZipResourceFile

        @JvmStatic
        fun getWritablePath():String {return ClassHelper._fileDirectory}

        @JvmStatic
        fun runOnGLThread(runnable: Runnable) {
            (_activity as MainActivity).runOnUiThread(runnable)
        }
    }

    interface HelperListener {
        fun showDialog(title:String, message:String)
        fun runOnGLThread(runnable: Runnable)
    }
}