package com.Meditation.Sounds.frequencies.models

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Profile : Serializable {
    @SerializedName("id")
    var id: Long = 0
    @SerializedName("name")
    var name: String? = null
    @SerializedName("email")
    var email: String? = null
    //    @SerializedName("deviceId")
//    var deviceId: String? = null
//    @SerializedName("os")
//    var os: Int = 0
    @SerializedName("isMaster")
    var isMaster: Int = 0
    @SerializedName("isPremium")
    var isPremium: Int = 0
    @SerializedName("isHighAbundance")
    var isHighAbundance: Int = 0
    @SerializedName("isHighQuantum")
    var isHighQuantum: Int = 0
    @SerializedName("isInnerCircle")
    var isInnerCircle: Int = 0
    @SerializedName("status")
    var status: Int = 0
    @SerializedName("isAdmin")
    var isAdmin: Int = 0

    @SerializedName("categories")
    var categories: ArrayList<Int>? = null
//    @SerializedName("registrationDate")
//    var registrationDate: Long = Date().time

    constructor()

    @Ignore
    constructor(name: String, email: String) {
        this.name = name
        this.email = email
    }
}
