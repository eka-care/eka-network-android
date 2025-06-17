package com.eka.network.client

import com.eka.network.token.TokenStorage

class NetworkConfig(
    override val baseUrl: String,
    override val appVersionName: String,
    override val appVersionCode: Int,
    override val isDebugApp: Boolean = false,
    override val apiCallTimeOutInSec: Long = 30L,
    override val tokenStorage: TokenStorage,
    override val headers: Map<String, String>
) : INetworkConfig