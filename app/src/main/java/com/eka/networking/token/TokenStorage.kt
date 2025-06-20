package com.eka.networking.token

interface TokenStorage {
    fun getAccessToken(): String
    fun getRefreshToken(): String
    fun saveTokens(accessToken: String, refreshToken: String)
    fun onSessionExpired()
}