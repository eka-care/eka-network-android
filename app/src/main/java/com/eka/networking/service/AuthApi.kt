package com.eka.networking.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface AuthApi {
    @POST("phr/v3/auth/refresh")
    suspend fun refresh(
        @Body authRefreshRequest: AuthRefreshRequest
    ): Response<AuthRefreshResponse?>
}