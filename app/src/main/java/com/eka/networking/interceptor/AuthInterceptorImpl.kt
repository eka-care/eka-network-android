package com.eka.networking.interceptor

import com.eka.networking.token.TokenProvider
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
                    header("Authorization", "Bearer $it")
                    header("auth", it)
                }
            }
            .build()

        val response = chain.proceed(requestWithAuth)

        if (response.code == 401) {
            response.close() // Close the failed response
            return runBlocking {
                val newToken = tokenProvider.refreshTokenIfNeeded()
                if (!newToken.isNullOrEmpty()) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .header("auth", newToken)
                        .build()
                    chain.proceed(retryRequest)
                } else {
                    response
                }
            }
        }

        return response
    }
}