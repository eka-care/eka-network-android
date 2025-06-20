package com.eka.networking.creator

interface ApiServiceCreator {
    fun <T> create(
        serviceClass: Class<T>,
        serviceUrl : String? = null,
        converterFactoryType: ConverterFactoryType = ConverterFactoryType.MOSHI
    ): T
}