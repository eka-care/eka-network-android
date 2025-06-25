package com.eka.networking.service

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AuthRefreshResponse(
    @SerializedName("access_token")
    val sessionToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String,
)