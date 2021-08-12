package com.Meditation.Sounds.frequencies.models
import com.google.gson.annotations.SerializedName


class PlaylistArraysItem {
    @SerializedName("playlist_name")
    var playlist_name: String? = ""
    @SerializedName("song_of_album")
    var songOfAlbum: ArrayList<SongOfAlbum> = ArrayList()

    class SongOfAlbum {
        @SerializedName("album_name")
        var album_name: String? = ""
        @SerializedName("track_name")
        var track_name: String? = ""
    }
}
