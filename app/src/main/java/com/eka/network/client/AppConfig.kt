package com.eka.network.client

import com.eka.network.token.TokenStorage

interface INetworkConfig {
    val baseUrl: String
    val isDebugApp: Boolean
    val appVersionName: String
    val appVersionCode: Int
    val apiCallTimeOutInSec: Long
    val tokenStorage: TokenStorage
    val headers: Map<String, String>
}