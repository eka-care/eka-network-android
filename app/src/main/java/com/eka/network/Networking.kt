package com.eka.network

import android.util.Log
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import com.moczul.ok2curl.CurlInterceptor
import com.moczul.ok2curl.logger.Logger
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.protobuf.ProtoConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.Collections
import java.util.concurrent.TimeUnit

class Networking private constructor() {

    private lateinit var okHttpSetup: IOkHttpSetup
    private lateinit var baseUrl: String
    private lateinit var retrofit: Retrofit
    private val servicesCache = Collections.synchronizedMap(LRUCache<String, Any>(10))

    companion object {

        private val instance by lazy {
            Networking()
        }

        fun init(baseUrl: String, curlLoggingEnabled : Boolean = false, okHttpSetup: IOkHttpSetup, converterFactoryType: ConverterFactoryType = ConverterFactoryType.GSON) {
            instance.init(baseUrl = baseUrl, curlLoggingEnabled = curlLoggingEnabled, okHttpSetup = okHttpSetup, converterFactoryType = converterFactoryType)
        }

        fun <T> create(clazz: Class<T>, curlLoggingEnabled : Boolean = false, baseUrl: String? = null, converterFactoryType: ConverterFactoryType = ConverterFactoryType.GSON): T =
            instance.create(clazz = clazz, curlLoggingEnabled = curlLoggingEnabled,baseUrl = baseUrl, converterFactoryType = converterFactoryType)

    }

    fun init(baseUrl: String, curlLoggingEnabled : Boolean = false, okHttpSetup: IOkHttpSetup, converterFactoryType: ConverterFactoryType) {
        if (this::baseUrl.isInitialized) {
            throw Exception("Networking is already initialised. Check if you are calling from multiple places")
        }
        this.baseUrl = baseUrl
        this.okHttpSetup = okHttpSetup
        retrofit = createClient(baseUrl = this.baseUrl, curlLoggingEnabled = curlLoggingEnabled, converterFactoryType = converterFactoryType)
    }

    @Synchronized
    fun <T> create(clazz: Class<T>, curlLoggingEnabled : Boolean = false, baseUrl: String? = null, converterFactoryType: ConverterFactoryType): T {
        val key = clazz.canonicalName
        return servicesCache.getOrPut(key) {
            if (!baseUrl.isNullOrEmpty() && !this.baseUrl.equals(baseUrl, true)) createClient(
                baseUrl = baseUrl, curlLoggingEnabled = curlLoggingEnabled, converterFactoryType = converterFactoryType
            ).create(clazz)
            else retrofit.create(clazz)
        } as T
    }


    private fun createClient(baseUrl: String, curlLoggingEnabled : Boolean = false, converterFactoryType: ConverterFactoryType): Retrofit {
        val builder = Retrofit.Builder().apply {
            baseUrl(baseUrl)
            addCallAdapterFactory(NetworkResponseAdapterFactory())
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
            addConverterFactory(if (converterFactoryType == ConverterFactoryType.PROTO) ProtoConverterFactory.create() else GsonConverterFactory.create())
        }
        val cookieManager = CookieManager()
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(DefaultNetworkInterceptor(okHttpSetup))
            .addInterceptor(AuthInterceptor(okHttpSetup))
            .callTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            .connectTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            .readTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            .writeTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            .authenticator(AccessTokenAuthenticator(okHttpSetup))
            .cookieJar(JavaNetCookieJar(cookieManager))

        if (curlLoggingEnabled) {
            clientBuilder.addInterceptor(interceptor)
                .addInterceptor(CurlInterceptor(object : Logger {
                    override fun log(message: String) {
                        Log.v("OkHttpCurl", message)
                    }
                }))
        }

        return builder.client(clientBuilder.build()).build()
    }

}

enum class ConverterFactoryType{
    GSON,
    PROTO
}