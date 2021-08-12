package com.Meditation.Sounds.frequencies.models

import androidx.room.Embedded
import androidx.room.Ignore

class PlaylistItemSongAndSong{
    constructor()

    @Ignore
    constructor(item: PlaylistItemSong, song:Song){
        this.item = item
        this.song = song
    }

    @Embedded
    var item = PlaylistItemSong()

    @Embedded
    var song = Song()
}

