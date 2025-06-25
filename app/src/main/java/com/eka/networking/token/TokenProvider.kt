package com.eka.networking.token

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun refreshTokenIfNeeded(): String?
    fun clearToken()
}