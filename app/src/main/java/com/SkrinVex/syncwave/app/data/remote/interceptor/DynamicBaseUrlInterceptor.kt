package com.SkrinVex.syncwave.app.data.remote.interceptor

import com.SkrinVex.syncwave.app.data.local.SessionDataStore
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class DynamicBaseUrlInterceptor(private val sessionDataStore: SessionDataStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var originalRequest = chain.request()
        val currentServerUrl = sessionDataStore.getServerUrlCached()

        val targetUrl = currentServerUrl.toHttpUrlOrNull()
        if (targetUrl != null) {
            val newHttpUrl = originalRequest.url.newBuilder()
                .scheme(targetUrl.scheme)
                .host(targetUrl.host)
                .port(targetUrl.port)
                .build()

            originalRequest = originalRequest.newBuilder()
                .url(newHttpUrl)
                .build()
        }

        return chain.proceed(originalRequest)
    }
}
