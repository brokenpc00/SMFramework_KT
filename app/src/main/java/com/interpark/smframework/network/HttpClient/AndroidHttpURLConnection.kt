package com.brokenpc.smframework.network.HttpClient

import android.util.Log
import com.brokenpc.smframework.ClassHelper
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

class AndroidHttpURLConnection {
    companion object {
        const val POST_METHOD = "POST"
        const val PUT_METHOD = "PUT"
        const val GET_METHOD = "GET"

        @JvmStatic
        fun createHttpURLConnection(linkUrl: String): HttpURLConnection? {
            var url: URL
            var urlConnection: HttpURLConnection
            try {
                url = URL(linkUrl)
                urlConnection = url.openConnection() as HttpURLConnection

                // Accept-Encoding
                urlConnection.setRequestProperty("Accept-Encoding", "identity")
                urlConnection.doInput = true
            } catch (e: IOException) {
                Log.e("URLConnection exception", e.toString())
                return null
            }

            return urlConnection
        }

        @JvmStatic
        fun setReadAndConnectTimeout(
            urlConnection: HttpURLConnection,
            readMillisecond: Int,
            connectMillisecond: Int
        ) {
            urlConnection.readTimeout = readMillisecond
            urlConnection.connectTimeout = connectMillisecond
        }

        @JvmStatic
        fun setRequestMethod(urlConnection: HttpURLConnection, method: String) {
            try {
                urlConnection.requestMethod = method
                if (method.equals(POST_METHOD, true) || method.equals(PUT_METHOD, true)) {
                    urlConnection.doInput = true
                }
            } catch (e: IOException) {
                Log.e("URLConnection exception", e.toString())
            }
        }

        @JvmStatic
        fun setVerifySSL(urlConnection: HttpURLConnection, sslFileName: String) {
            val httpsURLConnection:HttpsURLConnection = urlConnection as HttpsURLConnection

            try {
                val caInput:InputStream = if (sslFileName.startsWith("/")) {
                    BufferedInputStream(FileInputStream(sslFileName))
                } else {
                    val assetString = "assets/"
                    val assetfilenameString = sslFileName.substring(assetString.length)
                    BufferedInputStream(ClassHelper.getActivity()!!.assets.open(assetfilenameString))
                }

                val cf:CertificateFactory = CertificateFactory.getInstance("X.509")
                val ca:Certificate = cf.generateCertificate(caInput)

                println("ca=" + (ca as X509Certificate).subjectDN)
                caInput.close()

                val keyStoryType:String = KeyStore.getDefaultType()
                val keyStore = KeyStore.getInstance(keyStoryType)
                keyStore.load(null, null)
                keyStore.setCertificateEntry("ca", ca)

                val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
                val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
                tmf.init(keyStore)

                val context = SSLContext.getInstance("TLS")
                context.init(null, tmf.trustManagers, null)

                httpsURLConnection.sslSocketFactory = context.socketFactory
            } catch (e: IOException) {
                Log.e("URLConnection exception", e.toString())
            }
        }

        @JvmStatic
        fun addRequestHeader(urlConnection: HttpURLConnection, key: String, value: String) {
            urlConnection.setRequestProperty(key, value)
        }

        @JvmStatic
        fun connect(http: HttpURLConnection): Int {
            var suc = 0
            try {
                http.connect()
            } catch (e: IOException) {
                suc = 1
            }

            return suc
        }

        @JvmStatic
        fun disconnect(http: HttpURLConnection) {
            http.disconnect()
        }

        @JvmStatic
        fun sendRequest(http: HttpURLConnection, byteArray: ByteArray?) {
             try {
                 val out = http.outputStream
                 if (byteArray!=null) {
                     out.write(byteArray)
                     out.flush()
                 }
                 out.close()
             } catch (e: IOException) {
                 Log.e("URLConnection exception", e.toString())
             }
        }

        @JvmStatic
        fun getResponseHeader(http: HttpURLConnection): String? {
            val headers = http.headerFields ?: return null

            var header:String? = null

            for ((key, value) in headers) {
                if (key!=null) {
                    header += "$key:"
                }
                header += listToString(value, ",") + "\n"
            }
            for (entry in headers) {
                val key = entry.key
                header += if (key==null) {
                    listToString(entry.value, ",") + "\n"
                } else {
                    key + ":" + listToString(entry.value, ",") + "\n"
                }
            }

            return header
        }

        @JvmStatic
        fun getResponseHeaderByIdx(http: HttpURLConnection, idx: Int) : String? {
            val headers = http.headerFields ?: return null

            var header:String? = null
            var counter = 0
            for ((key, value) in headers) {
                if (counter==idx) {
                    if (null!=key) {
                        header += "$key:"
                    }
                    header += listToString(value, ",") + "\n"
                    break
                }
                counter++
            }

            return header
        }

        @JvmStatic
        fun getResponseHeaderByKey(http: HttpURLConnection, inputKey: String?) : String? {
            if (inputKey==null) return null

            val headers = http.headerFields ?: return null

            var header: String? = null

            for ((key, value) in headers) {
                if (inputKey.equals(key, true)) {
                    header = if ("set-cookie".equals(inputKey, true)) {
                        combineCookies(value, http.url.host)
                    } else {
                        listToString(value, ",")
                    }
                    break
                }
            }

            return header
        }

        @JvmStatic
        fun getResponseHeaderByKeyInt(http: HttpURLConnection, key: String): Int {
            val value = http.getHeaderField(key)

            return value?.toInt() ?: 0
        }

        @JvmStatic
        fun listToString(list: List<String>?, strInterVal: String): String? {
            if (list==null) {
                return null
            }

            val result = StringBuilder()
            var flag = false
            for (strtmp in list) {
                var str:String? = strtmp
                if (flag) {
                    result.append(strInterVal)
                }
                if (null==str) {
                    str = ""
                }
                result.append(str)
                flag = true
            }
            return result.toString()
        }

        @JvmStatic
        fun combineCookies(list: List<String>, hostDomain: String?): String? {
            val sbCookies = StringBuilder()
            var domain = hostDomain
            var tailmatch = "FALSE"
            var path = "/"
            var secure = "FALSE"
            var key: String? = null
            var value: String? = null
            var expires: String? = null

            for (str in list) {
                val parts:List<String> = str.split(";")
                for (part in parts) {
                    val firstIndex = part.indexOf("=")
                    if (-1==firstIndex) continue

                    val item: Array<String> = arrayOf(
                        part.substring(0, firstIndex), part.substring(
                            firstIndex + 1
                        )
                    )
                    if ("expires".equals(item[0].trim(), true)) {
                        expires = str2Seconds(item[1].trim())
                    } else if ("path".equals(item[0].trim(), true)) {
                        path = item[1]
                    } else if ("secure".equals(item[0].trim(), true)) {
                        secure = item[1]
                    } else if ("domain".equals(item[0].trim(), true)) {
                        domain = item[1]
                    } else if ("version".equals(item[0].trim(), true) || "max-age".equals(
                            item[0].trim(),
                            true
                        )) {
                        // do nothing
                    } else {
                        key = item[0]
                        value = item[1]
                    }
                }

                if (null==domain) {
                    domain = "none"
                }

                sbCookies.append(domain)
                sbCookies.append('\t')
                sbCookies.append(tailmatch)
                sbCookies.append('\t')
                sbCookies.append(path)
                sbCookies.append('\t')
                sbCookies.append(secure)
                sbCookies.append('\t')
                sbCookies.append(expires)
                sbCookies.append('\t')
                sbCookies.append(key)
                sbCookies.append('\t')
                sbCookies.append(value)
                sbCookies.append('\n')
            }

            return sbCookies.toString()
        }

        @JvmStatic
        fun str2Seconds(strTime: String): String {
            val c:Calendar = Calendar.getInstance()
            var milliseconds: Long = 0
            try {
                c.time = SimpleDateFormat("EEE, dd-MMM-yy hh:mm:ss zzz", Locale.US).parse(strTime)!!
                milliseconds = c.timeInMillis / 1000
            } catch (e: ParseException) {
                Log.e("URLConnection exception", e.toString())
            }

            return milliseconds.toString()
        }
    }
}