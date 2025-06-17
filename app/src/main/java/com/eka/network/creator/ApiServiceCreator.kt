package com.eka.network.creator

interface ApiServiceCreator {
    fun <T> create(
        serviceClass: Class<T>,
        baseUrlOverride: String? = null,
        converterFactoryType: ConverterFactoryType = ConverterFactoryType.MOSHI
    ): T
}