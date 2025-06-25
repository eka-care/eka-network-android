package com.eka.networking.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("connect-auth/v1/account/refresh-token")
    suspend fun refresh(
        @Body authRefreshRequest: AuthRefreshRequest
    ): Response<AuthRefreshResponse?>
}