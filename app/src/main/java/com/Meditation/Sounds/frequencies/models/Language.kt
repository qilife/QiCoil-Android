package com.Meditation.Sounds.frequencies.models

import com.Meditation.Sounds.frequencies.R
import java.io.Serializable

data class Language(val image: Int = R.drawable.ic_english_usa, val name: String,val translateName: String , val code: String) : Serializable