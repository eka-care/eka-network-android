package com.eka.network.service

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AuthRefreshRequest(
    @SerializedName("sess") val sessionToken: String,
    @SerializedName("refresh") val refresh: String,
)