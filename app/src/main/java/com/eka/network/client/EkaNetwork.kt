package com.eka.network.client

import android.util.Log
import com.eka.network.creator.ApiServiceCreator
import com.eka.network.creator.RetrofitServiceCreator
import com.eka.network.interceptor.AuthInterceptorImpl
import com.eka.network.interceptor.HeaderInformationInterceptor
import com.eka.network.interceptor.HeaderInformationInterceptorImpl
import com.eka.network.service.AuthApi
import com.eka.network.token.DefaultTokenProvider
import com.eka.network.token.TokenProvider
import com.eka.network.token.TokenStorage

object EkaNetwork {

    private val creators = mutableMapOf<String, ApiServiceCreator>()
    private lateinit var authBaseUrl: String

    fun init(authBaseUrl: String) {
        this.authBaseUrl = authBaseUrl
    }

    @Synchronized
    fun creatorFor(service: String, config: NetworkConfig): ApiServiceCreator {
        if(config.isDebugApp) {
            Log.d("EkaNetwork", "creatorFor: $service, Object: $this")
        }
        if (!::authBaseUrl.isInitialized) {
            throw IllegalStateException("EkaNetwork must be initialized with authBaseUrl before creating service creators.")
        }

        val headerInterceptor: HeaderInformationInterceptor = HeaderInformationInterceptorImpl(
            headers = config.headers
        )
        val authApi = RetrofitServiceCreator(
            appConfig = config,
            headerInformationInterceptor = headerInterceptor
        ).create(AuthApi::class.java, baseUrlOverride = authBaseUrl)

        return creators.getOrPut(service) {
            RetrofitServiceCreator(
                appConfig = config,
                authInterceptor = AuthInterceptorImpl(
                    tokenProvider = DefaultTokenProvider(
                        tokenStorage = config.tokenStorage,
                        authApi = authApi
                    )
                ),
                headerInformationInterceptor = headerInterceptor
            )
        }
    }

    fun clear(service: String) {
        creators.remove(service)
    }

    fun clearAll() {
        creators.clear()
    }
}