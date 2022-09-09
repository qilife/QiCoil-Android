package com.Meditation.Sounds.frequencies.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class FlashSale : Serializable{
    @SerializedName("enable")
    var enable: Boolean? = null
    @SerializedName("init_delay")
    var initDelay: Float? = null
    @SerializedName("duration")
    var duration: Float? = null
    @SerializedName("interval")
    var interval: Float? = null
    @SerializedName("proposals_count")
    var proposalsCount: Float? = null
    @SerializedName("ntf")
    var ntf: Ntf? = null
}