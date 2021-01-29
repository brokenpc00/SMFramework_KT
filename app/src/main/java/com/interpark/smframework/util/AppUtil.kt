package com.brokenpc.smframework.util

import android.content.Context
import android.os.Environment
import com.brokenpc.smframework.ClassHelper
import java.io.File

class AppUtil {

    val TAG = AppUtil.javaClass.simpleName

    companion object {
        @JvmStatic
        fun getExternalFilesDir(context: Context, type: String):File {
            var dir = context.getExternalFilesDir(type)
            if (dir==null) {
                val path = "/Android/data/" + context.packageName + "/files/" + type
                dir = File(context.getExternalFilesDir(null)!!.absolutePath + path)
                if (!dir.exists()) {
                    dir.mkdir()
                }
            }
            return dir
        }

    }

}