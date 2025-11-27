package com.eka.networking.creator

import android.util.Log
import com.eka.networking.client.NetworkConfig
import com.eka.networking.interceptor.AuthInterceptor
import com.eka.networking.interceptor.HeaderInformationInterceptor
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

internal class RetrofitServiceCreator(
    private val appConfig: NetworkConfig,
    private val authInterceptor: AuthInterceptor? = null,
    private val headerInformationInterceptor: HeaderInformationInterceptor,
) : ApiServiceCreator {

    private val okHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(appConfig.apiCallTimeOutInSec, TimeUnit.SECONDS)
        callTimeout(appConfig.apiCallTimeOutInSec, TimeUnit.SECONDS)
        connectTimeout(appConfig.apiCallTimeOutInSec, TimeUnit.SECONDS)
        readTimeout(appConfig.apiCallTimeOutInSec, TimeUnit.SECONDS)
        writeTimeout(appConfig.apiCallTimeOutInSec, TimeUnit.SECONDS)
        connectionPool(
            ConnectionPool(
                maxIdleConnections = 5,
                keepAliveDuration = 5,
                timeUnit = TimeUnit.MINUTES
            )
        )
        authInterceptor?.let {
            addInterceptor(it)
        }
        addInterceptor(headerInformationInterceptor)
        if (appConfig.isDebugApp) {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            addInterceptor(CurlInterceptor(object : Logger {
                override fun log(message: String) {
                    Log.v("OkHttpCurl", message)
                }
            }))
        }
        addInterceptor(BrotliInterceptor)
    }.build()

    override fun <T> create(
        serviceClass: Class<T>,
        serviceUrl: String?,
    ): T = createRetrofitBuilder(
        serviceUrl = serviceUrl,
    ).build().create(serviceClass)

    private fun createRetrofitBuilder(serviceUrl: String?) =
        with(Retrofit.Builder()) {
            client(okHttpClient)
            addCallAdapterFactory(NetworkResponseAdapterFactory())
            addConverterFactory(GsonConverterFactory.create())
            baseUrl(serviceUrl ?: "https://api.eka.care")
        }
}