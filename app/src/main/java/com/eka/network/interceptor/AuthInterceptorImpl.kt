package com.eka.network.interceptor

import com.eka.network.token.TokenProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

internal class AuthInterceptorImpl(
    private val tokenProvider: TokenProvider
) : AuthInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = runBlocking { tokenProvider.getAccessToken() }

        val requestWithAuth = originalRequest.newBuilder()
            .apply {
                accessToken?.let {
                    addHeader("Authorization", "Bearer $it")
                    addHeader("auth", it)
                }
            }
            .build()

        val response = chain.proceed(requestWithAuth)

        if (response.code == 401) {
            response.close() // Close the failed response
            val newToken = runBlocking { tokenProvider.refreshTokenIfNeeded() }
            if (!newToken.isNullOrEmpty()) {
                val retryRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retryRequest)
            }
        }

        return response
    }
}