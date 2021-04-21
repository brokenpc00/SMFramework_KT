package com.interpark.smframework.util.security

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// 레인달 알고리즘 AES 암호화

class RijndaelSecurity {
    companion object {
        var TAG = RijndaelSecurity.javaClass.simpleName
        var secretKey: String = ""
        const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        var mStrIv: ByteArray? = null

        @JvmStatic
        fun getIv():IvParameterSpec {
            val rnd = SecureRandom()
            mStrIv = SecurityUtil.md5Byte(rnd.generateSeed(16).toString())

            return IvParameterSpec(mStrIv)
        }

        @JvmStatic
        fun generateKey(secretKey: String): SecretKeySpec {
            return SecretKeySpec(SecurityUtil.md5Byte(secretKey), "AES")
        }

        @JvmStatic
        fun encrypt(secretKey: String, text: String): String {
            var cipher: Cipher? = null
            val encrypted: ByteArray

            try {
                cipher = Cipher.getInstance(TRANSFORMATION)
            } catch (e: NoSuchAlgorithmException) {
                return ""
            } catch (e: NoSuchPaddingException) {
                return  ""
            }

            try {
                cipher.init(Cipher.ENCRYPT_MODE, generateKey(secretKey), getIv())
            } catch (e: InvalidKeyException) {
                return ""
            } catch (e: InvalidAlgorithmParameterException) {
                return ""
            }

            try {
                encrypted = cipher.doFinal(
                    SecurityUtil.concatenateByteArrays(
                        mStrIv, text.toByteArray(
                            charset("UTF-8")
                        )
                    )
                )
                val encodeCode = Base64.encode(encrypted, Base64.DEFAULT)
                return String(encodeCode, charset("UTF-8"))
            } catch (e: IllegalBlockSizeException) {
                return ""
            } catch (e: BadPaddingException) {
                return ""
            } catch (e: UnsupportedEncodingException) {
                return ""
            }
        }

        @JvmStatic
        fun decrypt(secretKey: String, code: String): String {
            var dec: String = ""

            var cipher: Cipher? = null

            val decrypted: ByteArray
            try {
                cipher = Cipher.getInstance(TRANSFORMATION)
            } catch (e: NoSuchAlgorithmException) {
                return ""
            } catch (e: NoSuchPaddingException) {
                return ""
            }

            try {
                cipher.init(Cipher.DECRYPT_MODE, generateKey(secretKey), IvParameterSpec(mStrIv))
            } catch (e: InvalidKeyException) {
                return ""
            } catch (e: InvalidAlgorithmParameterException) {
                return ""
            }

            try {
                val decodeCode = Base64.decode(code, Base64.DEFAULT)
                decrypted = cipher.doFinal(decodeCode)
                dec = SecurityUtil.getByteString(decrypted)
            } catch (e: IllegalBlockSizeException) {
                return ""
            } catch (e: BadPaddingException) {
                return ""
            } catch (e: UnsupportedEncodingException) {
                return ""
            }

            return dec
        }
    }
}