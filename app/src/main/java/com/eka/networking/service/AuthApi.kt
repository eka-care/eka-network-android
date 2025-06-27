package com.eka.networking.service

import com.haroldadmin.cnradapter.NetworkResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("connect-auth/v1/account/refresh-token")
    suspend fun refresh(
        @Body authRefreshRequest: AuthRefreshRequest
    ): NetworkResponse<AuthRefreshResponse?, AuthRefreshResponse?>
}