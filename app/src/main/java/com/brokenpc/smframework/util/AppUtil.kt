package com.brokenpc.smframework.util

import android.content.Context
import android.os.Environment
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Pattern

class AppUtil {

    val TAG = AppUtil.javaClass.simpleName

    companion object {

        @JvmStatic
        fun isValidEmailAddress(email: String) : Boolean {
            val regex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(email).find()
        }

        @JvmStatic
        fun checkParams(map: Map<String, String?>): Map<String, String> {
            var returnMap = mutableMapOf<String, String>()
            for ((key, value) in map) {
                if (value == null) {
                    returnMap[key] = ""
                } else {
                    returnMap[key] = value
                }
            }
            return returnMap as Map<String, String>
        }

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

        @JvmStatic
        fun getExternalPicturesDir(context: Context, type: String): File {
            val dir:File = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/" + type)

            if (!dir.exists()) {
                dir.mkdir()
            }

            return dir
        }

        const val HASH_ALGORITHM = "MD5"
        @JvmStatic
        fun getMD5(data: ByteArray?): ByteArray? {
            var hash:ByteArray? = null

            if (data==null) return null

            try {
                val digest = MessageDigest.getInstance(HASH_ALGORITHM)
                digest.update(data)
                hash = digest.digest()
            } catch (e: NoSuchAlgorithmException) {

            }

            return hash
        }

        @JvmStatic
        fun getCurDate(): String {
            val cal: Calendar = Calendar.getInstance()
            return String.format(
                "%04d%02d%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(
                    Calendar.DATE
                )
            )
        }

        @JvmStatic
        fun getCurTime(): String {
            val cal: Calendar = Calendar.getInstance()
            return String.format(
                "%02d%02d%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(
                    Calendar.SECOND
                )
            )
        }

        @JvmStatic
        fun getCurDateTime(): String {
            val cal: Calendar = Calendar.getInstance()
            return String.format(
                "%04d%02d%02d%02d%02d%02d",
                cal.get(Calendar.YEAR),
                cal.get(
                    Calendar.MONTH
                ) + 1,
                cal.get(Calendar.DATE),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(
                    Calendar.SECOND
                )
            )
        }

        @JvmStatic
        fun getSaveFileTimeFormat(): String {
            val cal: Calendar = Calendar.getInstance()
            return String.format(
                "%04d%02d%02d_%02d%02d%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)
            )
        }
    }


}