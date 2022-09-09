package com.Meditation.Sounds.frequencies.api.objects;

import com.google.gson.annotations.SerializedName;

public class CheckAlbumInput {
    @SerializedName("user_id")
    public String user_id;

    public CheckAlbumInput(String user_id) {
        this.user_id = user_id;
    }
}
