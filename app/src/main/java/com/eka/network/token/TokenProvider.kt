package com.eka.network.token

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun refreshTokenIfNeeded(): String?
    fun clearToken()
}