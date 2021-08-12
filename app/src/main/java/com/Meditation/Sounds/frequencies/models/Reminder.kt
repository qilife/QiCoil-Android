package com.Meditation.Sounds.frequencies.models

import com.google.gson.annotations.SerializedName

class Reminder {
    @SerializedName("launch_time")
    var launchTime: String? = null
    @SerializedName("interval")
    var interval: Double? = null
    @SerializedName("messages")
    var messages: ArrayList<String>? = null
}