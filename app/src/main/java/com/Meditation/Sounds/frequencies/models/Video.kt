package com.Meditation.Sounds.frequencies.models

import androidx.room.Ignore
import java.io.Serializable

class Video : Serializable {
    var videoId: String = ""
    var title: String = ""
    var thumbnails: String = ""

    @Ignore
    constructor(id: String, title: String, image: String) {
        this.videoId = id
        this.title = title
        this.thumbnails = image
    }

    constructor()
}
