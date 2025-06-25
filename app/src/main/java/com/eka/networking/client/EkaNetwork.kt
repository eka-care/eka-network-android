package com.eka.networking.client

import android.util.Log
import androidx.annotation.Keep
import com.eka.networking.creator.ApiServiceCreator
import com.eka.networking.creator.RetrofitServiceCreator
import com.eka.networking.interceptor.AuthInterceptorImpl
import com.eka.networking.interceptor.HeaderInformationInterceptor
import com.eka.networking.interceptor.HeaderInformationInterceptorImpl
import com.eka.networking.service.AuthApi
import com.eka.networking.token.DefaultTokenProvider

@Keep
object EkaNetwork {

    private val creators = mutableMapOf<String, MutableMap<String, ApiServiceCreator>>()
    private val configs = mutableMapOf<String, NetworkConfig>()

    fun init(networkConfig: NetworkConfig) {
        creators.put(networkConfig.appId, emptyMap<String, ApiServiceCreator>().toMutableMap())
        configs[networkConfig.appId] = networkConfig
    }

    @Synchronized
    fun creatorFor(appId: String, service: String): ApiServiceCreator {
        val config = configs[appId]
            ?: throw IllegalStateException("Invalid appId: $appId. Please initialize EkaNetwork with a valid NetworkConfig.")

        if (config.isDebugApp) {
            Log.d("EkaNetwork", "creatorFor: $service, Object: $this")
        }

        val headerInterceptor: HeaderInformationInterceptor = HeaderInformationInterceptorImpl(
            tokenStorage = config.tokenStorage,
            headers = config.headers.toMutableMap().also {
                it.remove("auth")
                it.remove("Authorization")
            }
        )
        val authApi = RetrofitServiceCreator(
            appConfig = config,
            headerInformationInterceptor = headerInterceptor
        ).create(
            serviceClass = AuthApi::class.java,
            serviceUrl = config.baseUrl
        )

        return creators[appId]?.getOrPut(service) {
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
        } ?: throw IllegalStateException("No creator found for service: $service")
    }

    fun clear(service: String) {
        creators.remove(service)
    }

    fun clearAll() {
        creators.clear()
    }
}