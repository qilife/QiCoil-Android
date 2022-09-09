package com.Meditation.Sounds.frequencies.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Ntf : Serializable {
    @SerializedName("first")
    var first: AlarmMessage? = null
    @SerializedName("second")
    var second: AlarmMessage? = null
    @SerializedName("third")
    var third: AlarmMessage? = null
}