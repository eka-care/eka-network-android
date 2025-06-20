package com.eka.networking.creator

import android.util.Log
import com.eka.networking.adapter.JSONArrayAdapter
import com.eka.networking.adapter.JSONObjectAdapter
import com.eka.networking.client.NetworkConfig
import com.eka.networking.interceptor.AuthInterceptor
import com.eka.networking.interceptor.HeaderInformationInterceptor
import com.eka.networking.response.NetworkResponseCallAdapterFactory
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import com.squareup.moshi.Moshi
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.brotli.BrotliInterceptor
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

internal class RetrofitServiceCreator(
    private val appConfig: NetworkConfig,
    private val authInterceptor: AuthInterceptor? = null,
    private val headerInformationInterceptor: HeaderInformationInterceptor,
) : ApiServiceCreator {

    private val okHttpClient = OkHttpClient.Builder().apply {
        connectTimeout(appConfig.apiCallTimeOutInSec, TimeUnit.SECONDS)
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

    private val defaultRemoteMoshi = Moshi.Builder().build().newBuilder()
        .add(JSONArrayAdapter)
        .add(JSONObjectAdapter).build()

    override fun <T> create(
        serviceClass: Class<T>,
        serviceUrl : String?,
        converterFactoryType: ConverterFactoryType,
    ): T = createRetrofitBuilder(serviceUrl = serviceUrl, converterFactoryType = converterFactoryType).build().create(serviceClass)

    private fun createRetrofitBuilder(serviceUrl : String?, converterFactoryType: ConverterFactoryType) =
        with(Retrofit.Builder()) {
            client(okHttpClient)
            addCallAdapterFactory(NetworkResponseCallAdapterFactory)
            addConverterFactory(
                when (converterFactoryType) {
                    ConverterFactoryType.MOSHI -> {
                        MoshiConverterFactory.create(
                            defaultRemoteMoshi
                        )
                    }

                    ConverterFactoryType.GSON -> {
                        GsonConverterFactory.create()
                    }
                }
            )
            baseUrl(serviceUrl ?: "https://api.eka.care")
        }
}

enum class ConverterFactoryType {
    GSON,
    MOSHI
}