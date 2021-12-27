package com.brokenpc.smframework.util.security

import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.NoSuchAlgorithmException

class SecurityUtil {
    companion object {
        var TAG = SecurityUtil.javaClass.simpleName

        @JvmStatic
        @Throws (UnsupportedEncodingException::class)
        fun getByteString(s: ByteArray): String {
            val startIdx = 16
            val bytes = s.size - 16

            return String(s, startIdx, bytes,   charset("UTF-8"))
        }

        @JvmStatic
        fun md5Byte(securityString: String): ByteArray? {
            try {
                val digest = java.security.MessageDigest.getInstance("MD5")
                digest.update(securityString.toByteArray(charset("UTF-8")))
                return digest.digest()
            } catch (e: NoSuchAlgorithmException) {
                return null
            } catch (e: UnsupportedEncodingException) {
                return null
            }
        }

        @JvmStatic
        fun concatenateByteArrays(a: ByteArray?, b: ByteArray?): ByteArray? {
            if (a==null || b==null) {
                if (a==null && b!=null) return b
                if (b==null && a!=null) return a
                if (a==null && b==null) return null
            }

            val result = ByteArray(a!!.size+b!!.size)
            System.arraycopy(a, 0, result, 0, a.size)
            System.arraycopy(b, 0, result, a.size, b.size)
            return result
        }
    }
}