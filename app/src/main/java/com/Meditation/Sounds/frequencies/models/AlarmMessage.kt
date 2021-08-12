package com.Meditation.Sounds.frequencies.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class AlarmMessage : Serializable{
    @SerializedName("message")
    var message: String? = null
    @SerializedName("delay")
    var delay: Float? = null
}