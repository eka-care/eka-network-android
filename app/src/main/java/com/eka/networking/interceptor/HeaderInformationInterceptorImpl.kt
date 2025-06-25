package com.eka.networking.interceptor

import com.eka.networking.token.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

internal class HeaderInformationInterceptorImpl(
    private val tokenStorage: TokenStorage,
    private val headers: Map<String, String>
) :
    HeaderInformationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        headers.forEach {
            requestBuilder.header(it.key, it.value)
        }
        val accessToken = runBlocking { tokenStorage.getAccessToken() }
        requestBuilder.removeHeader("auth")
        requestBuilder.removeHeader("Authorization")
        requestBuilder.header("Authorization", "Bearer $accessToken")
        requestBuilder.header("auth", accessToken)
        return chain.proceed(request = requestBuilder.build())
    }
}