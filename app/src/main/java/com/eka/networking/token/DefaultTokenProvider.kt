package com.eka.networking.token

import android.util.Base64
import com.eka.networking.service.AuthApi
import com.eka.networking.service.AuthRefreshRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DefaultTokenProvider(
    private val tokenStorage: TokenStorage,
    private val authApi: AuthApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TokenProvider {

    @Volatile
    private var isRefreshing = false
    private val refreshMutex = Mutex()

    override suspend fun getAccessToken(): String? {
        return withContext(dispatcher) {
            tokenStorage.getAccessToken()
        }
    }

    override suspend fun refreshTokenIfNeeded(): String? {
        return withContext(dispatcher) {
            refreshMutex.withLock {
                if (!shouldRefreshToken()) {
                    return@withLock tokenStorage.getAccessToken()
                }

                try {
                    isRefreshing = true
                    val refreshToken = tokenStorage.getRefreshToken()
                    val sessionToken = tokenStorage.getAccessToken()
                    val response = authApi.refresh(
                        AuthRefreshRequest(
                            sessionToken = sessionToken,
                            refresh = refreshToken
                        )
                    )
                    
                    val newAccessToken = response.body()?.sessionToken
                    val newRefreshToken = response.body()?.refreshToken

                    if (newAccessToken == null || newRefreshToken == null) {
                        throw IllegalStateException("Failed to refresh tokens: response is null")
                    }

                    tokenStorage.saveTokens(newAccessToken, newRefreshToken)

                    newAccessToken
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } finally {
                    isRefreshing = false
                }
            }
        }
    }

    override fun clearToken() {
        tokenStorage.onSessionExpired()
    }

    private fun shouldRefreshToken(): Boolean {
        return try {
            val token = tokenStorage.getAccessToken()
            val parts = token.split(".")
            if (parts.size != 3) return true

            val payloadJson = decodeBase64(parts[1])
            val json = JSONObject(payloadJson)
            val expSeconds = json.optLong("exp", -1)
            System.currentTimeMillis() > expSeconds
        } catch (e: Exception) {
            true
        }
    }

    private fun decodeBase64(encoded: String): String {
        val decodedBytes = Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        return String(decodedBytes, Charsets.UTF_8)
    }
}
