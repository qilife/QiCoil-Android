package com.Meditation.Sounds.frequencies.lemeor.data.model

data class AuthResponse(var token: String, var user: User)

data class User(
        var id: Int,
        var name: String,
        var email: String,
        var is_active: Int,
        var unlocked_tiers: List<Int>,
        var unlocked_categories: List<Int>,
        var unlocked_albums: List<Int>
)

data class PassEmailResponse(var token: String, var message: String)

data class Status(var status: Boolean)

data class Message(var message: String)

data class StatusMessage(var status: Boolean, var message: String)

data class Array (var data: List<Int>)