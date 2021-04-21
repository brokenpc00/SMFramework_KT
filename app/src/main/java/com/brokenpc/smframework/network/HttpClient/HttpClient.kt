package com.brokenpc.smframework.network.HttpClient

import com.brokenpc.smframework.IDirector
import com.brokenpc.smframework.base.types.Ref
import java.net.HttpURLConnection

class HttpClient(director: IDirector) : Ref(director) {
     companion object {
         const val RESPONSE_BUFFER_SIZE:Int = 256
         var _httpClient:HttpClient? = null
     }

    private var _this:HttpClient? = null

    interface OnWriteCallback {
        fun onWrite(data: ByteArray, size: Int, nmemb: Int, stream: ByteArray)
    }

    class HttpURLConnectionWrapper(director: IDirector, httpClient: HttpClient) : Ref(director) {
        private var _client:HttpClient? = null
        private var _httpURLConnection:HttpURLConnection?
        private var _requestMethod: String
        private var _responseCookies: String
        private var _cookieFileName: String
        private var _url: String
        private var _contentLength: Int

        init {
            _client = httpClient
            _requestMethod = ""
            _responseCookies = ""
            _cookieFileName = ""
            _contentLength = 0
            _url = ""
            _httpURLConnection = null
        }

        fun setRequestMethod(method: String) {
            _requestMethod = method

        }
    }
}