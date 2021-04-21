package com.brokenpc.smframework.util

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.ByteArrayInputStream


class NetworkStreamRequest(url:String, listener: Response.Listener<ByteArrayInputStream>, errorListener: Response.ErrorListener) : Request<ByteArrayInputStream>(Method.GET, url, errorListener) {

    private val _listener = listener


    companion object {
        const val IMAGE_TIMEOUT_MS = 1000
        const val IMAGE_MAX_RETRIES = 2
        const val IMAGE_BACKOFF_MULT = 2f

        private val _decodeLock:Any = Any()

    }

    init {
        retryPolicy = DefaultRetryPolicy(IMAGE_MAX_RETRIES, IMAGE_MAX_RETRIES, IMAGE_BACKOFF_MULT)
    }

    override fun getPriority(): Priority {
        return Priority.LOW
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<ByteArrayInputStream> {
        synchronized(_decodeLock) {
            return try {
                doParse(response)
            } catch (e:OutOfMemoryError) {
                VolleyLog.e("Caught OOM for %d byte image, url=%s", response?.data?.size, url)
                Response.error(ParseError(e))
            }
        }
    }

    private fun doParse(response: NetworkResponse?): Response<ByteArrayInputStream> {
        val data:ByteArray? = response?.data

        return if (data==null || data.isEmpty()) {
            Response.error(ParseError(response))
        } else {
            Response.success(ByteArrayInputStream(data), (HttpHeaderParser.parseCacheHeaders(response)))
        }
    }

    override fun deliverResponse(response: ByteArrayInputStream?) {
        _listener.onResponse(response)
    }
}