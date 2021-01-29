package com.brokenpc.smframework.network.HttpClient

import com.brokenpc.smframework.util.FileUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class HttpCookie() {

    companion object {
        class CookiesInfo() {
            var domain: String = ""
            var tailmatch: Boolean = false
            var path: String = ""
            var secure: Boolean = false
            var name: String = ""
            var value: String = ""
            var expire: String = ""
        }
    }

    private var _cookieFileName:String = ""
    private var _cookies: ArrayList<CookiesInfo> = ArrayList()

    fun readFile() {
        val inString = FileUtils.getInstance().getStringFromFile(_cookieFileName)

        if (inString!!.isNotEmpty()) {
            val cookieList = ArrayList<String>()
            cookieList.clear()

            val cookieRead = Scanner(inString)
            cookieRead.useDelimiter("\n")

            while (cookieRead.hasNext()) {
                cookieList.add(cookieRead.next())
            }

            if (cookieList.isEmpty()) return;

            for (cookieval in cookieList) {
                var cookie = cookieval
                if (cookie.isEmpty()) return

                if (cookie.contains("#HttpOnly_")) {
                    cookie = cookie.substring(10)
                }

                if (cookie.substring(0, 1)=="#") {
                    continue
                }

                val co = CookiesInfo()
                val elems = ArrayList<String>()

                val infoRead = Scanner(cookie)
                infoRead.useDelimiter("\t")

                while (infoRead.hasNext()) {
                    elems.add(infoRead.next())
                }

                co.domain = elems[0]

                if (co.domain.substring(0, 1)==".") {
                    co.domain = co.domain.substring(0, 1)
                }

                co.tailmatch = elems[1]=="TRUE"
                co.path = elems[2]
                co.secure = elems[3]=="TRUE"
                co.expire = elems[4]
                co.name = elems[5]
                co.value = elems[6]
                _cookies.add(co)
            }
        }
    }

    fun writeFile() {
        val out = File(_cookieFileName)

        val memo = "# Netscape HTTP Cookie File\n# http://curl.haxx.se/docs/http-cookies.html\n\n"
        val sb = StringBuffer(memo)

        for (cookie in _cookies) {
            sb.append(cookie.domain)
            sb.append('\t')

            if (cookie.tailmatch) {
                sb.append("TRUE")
            } else {
                sb.append("FALSE")
            }
            sb.append('\t')
            sb.append(cookie.path)
            sb.append('\t')
            if (cookie.secure) {
                sb.append("TRUE")
            } else {
                sb.append("FALSE")
            }

            sb.append('\t')
            sb.append(cookie.expire)
            sb.append('\t')
            sb.append(cookie.name)
            sb.append('\t')
            sb.append(cookie.value)
            sb.append('\n')
        }

        if (out.canWrite()) {
            try {
                val stream = FileOutputStream(out)
                stream.write(sb.toString().toByteArray())
                stream.close()
            } catch (e: IOException) {

            }
        }
        sb.setLength(0)
    }

    fun setCookieFileName(fileName: String) {_cookieFileName = fileName}
    fun getCookies():ArrayList<CookiesInfo> {return _cookies}
    fun getMatchCookie(url: String):CookiesInfo? {
        for (cookie in _cookies) {
            if (url.contains(cookie.domain)) {
                return cookie
            }
        }

        return null
    }

    fun updateOrAddCookie(cookie: CookiesInfo) {
        for (i in 0 until _cookies.size) {
            if (_cookies[i].domain==cookie.domain) {
                _cookies[i] = cookie
                return
            }
        }
        _cookies.add(cookie)
    }
}