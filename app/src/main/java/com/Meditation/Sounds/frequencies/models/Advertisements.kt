package com.Meditation.Sounds.frequencies.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Advertisements : Serializable {
    @SerializedName("enable")
    var enable: Boolean? = false
    @SerializedName("enable_banner")
    var enableBanner: Boolean? = false
}