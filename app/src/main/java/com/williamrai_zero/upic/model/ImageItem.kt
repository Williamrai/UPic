package com.williamrai_zero.upic.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ImageItem(
    @SerializedName("url") val url: String,
    @SerializedName("created") val created: String,
    @SerializedName("updated") val update: String
)
