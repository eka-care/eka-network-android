package com.eka.networking.creator

import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory

interface ApiServiceCreator {
    fun <T> create(
        serviceClass: Class<T>,
        serviceUrl: String? = null,
        converterFactory: Converter.Factory = GsonConverterFactory.create(),
    ): T
}