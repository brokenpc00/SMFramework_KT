package com.brokenpc.smframework.util

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class KeyGenerateUtil {
    companion object {
        private const val HASH_ALGORITHM = "MD5"
        private const val RADIX = 36 // 10 digit 26 letters

        @JvmStatic
        fun generate(imageUrl:String):String {
            val md5:ByteArray? = getMD5(imageUrl.toByteArray())
            val bi:BigInteger = BigInteger(md5).abs()
            return bi.toString(RADIX)
        }

        @JvmStatic
        fun getMD5(data:ByteArray):ByteArray? {
            var hash:ByteArray? = null
            try {
                val digest:MessageDigest = MessageDigest.getInstance(HASH_ALGORITHM)
                digest.update(data)
                hash = digest.digest()
            } catch (e:NoSuchAlgorithmException) {
                // nothing to do
            }

            return hash
        }
    }
}