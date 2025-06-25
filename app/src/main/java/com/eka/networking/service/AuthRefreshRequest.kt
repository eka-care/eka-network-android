package com.eka.networking.service

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AuthRefreshRequest(
    @SerializedName("refresh_token") val refresh: String,
    @SerializedName("access_token") val sessionToken: String,
)