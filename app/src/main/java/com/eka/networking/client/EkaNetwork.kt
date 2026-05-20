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
import okhttp3.Interceptor

@Keep
object EkaNetwork {

    private val creators = mutableMapOf<String, MutableMap<String, ApiServiceCreator>>()
    private val configs = mutableMapOf<String, NetworkConfig>()
    private val interceptors = mutableMapOf<String, List<Interceptor>>()

    @JvmOverloads
    fun init(networkConfig: NetworkConfig, interceptors: List<Interceptor> = emptyList()) {
        creators[networkConfig.appId] = emptyMap<String, ApiServiceCreator>().toMutableMap()
        configs[networkConfig.appId] = networkConfig
        this.interceptors[networkConfig.appId] = interceptors
    }

    @Synchronized
    fun creatorFor(appId: String, service: String): ApiServiceCreator {
        val config = configs[appId]
            ?: throw IllegalStateException("Invalid appId: $appId. Please initialize EkaNetwork with a valid NetworkConfig.")

        if (config.isDebugApp) {
            Log.d("EkaNetwork", "creatorFor: $service, Object: $this")
        }

        val extraInterceptors = interceptors[appId] ?: emptyList()
        val headerInterceptor: HeaderInformationInterceptor = HeaderInformationInterceptorImpl(
            tokenStorage = config.tokenStorage,
            headers = config.headers.toMutableMap().also {
                it.remove("auth")
                it.remove("Authorization")
            }
        )
        val authApi = RetrofitServiceCreator(
            appConfig = config,
            extraInterceptors = extraInterceptors,
            headerInformationInterceptor = headerInterceptor,
        ).create(
            serviceClass = AuthApi::class.java,
            serviceUrl = config.baseUrl
        )

        return creators[appId]?.getOrPut(service) {
            RetrofitServiceCreator(
                appConfig = config,
                extraInterceptors = extraInterceptors,
                authInterceptor = AuthInterceptorImpl(
                    tokenProvider = DefaultTokenProvider(
                        tokenStorage = config.tokenStorage,
                        authApi = authApi
                    )
                ),
                headerInformationInterceptor = headerInterceptor,
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