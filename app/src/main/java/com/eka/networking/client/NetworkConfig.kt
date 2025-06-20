package com.eka.networking.client

import com.eka.networking.token.TokenStorage

class NetworkConfig(
    override val appId: String,
    override val baseUrl: String,
    override val appVersionName: String,
    override val appVersionCode: Int,
    override val isDebugApp: Boolean = false,
    override val apiCallTimeOutInSec: Long = 30L,
    override val headers: Map<String, String>,
    override val tokenStorage: TokenStorage
) : INetworkConfig