package com.eka.networking.interceptor

import okhttp3.Interceptor
import okhttp3.Response

internal class HeaderInformationInterceptorImpl(val headers: Map<String, String>) :
    HeaderInformationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        headers.forEach {
            requestBuilder.addHeader(it.key, it.value)
        }
        return chain.proceed(request = requestBuilder.build())
    }
}