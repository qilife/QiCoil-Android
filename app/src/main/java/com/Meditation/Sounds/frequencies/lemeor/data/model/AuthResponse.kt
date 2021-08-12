package com.Meditation.Sounds.frequencies.lemeor.data.model

data class AuthResponse(val token: String, val user: User)

data class User(
        val id: Int,
        val name: String,
        val email: String,
        val is_active: Int,
        val unlocked_tiers: List<Int>,
        val unlocked_categories: List<Int>,
        val unlocked_albums: List<Int>
)

data class PassEmailResponse(val token: String, val message: String)

data class Status(val status: Boolean)

data class Message(val message: String)

data class StatusMessage(val status: Boolean, val message: String)

data class Array (val data: List<Int>)