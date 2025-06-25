package com.eka.networking.client

import com.eka.networking.token.TokenStorage

interface INetworkConfig {
    val appId: String
    val baseUrl: String
    val isDebugApp: Boolean
    val appVersionName: String
    val appVersionCode: Int
    val apiCallTimeOutInSec: Long
    val headers: Map<String, String>
    val tokenStorage: TokenStorage
}