package com.eka.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object OkHttpDefaulter {

    fun OkHttpClient.Builder.fillClientDefaults(okHttpSetup : IOkHttpSetup) : OkHttpClient.Builder {
        this.apply {
            addInterceptor(DefaultNetworkInterceptor(okHttpSetup))
            callTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            connectTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            readTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            writeTimeout(okHttpSetup.timeoutsInSeconds(), TimeUnit.SECONDS)
            authenticator(AccessTokenAuthenticator(okHttpSetup))
        }
        return this
    }
}