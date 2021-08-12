package com.Meditation.Sounds.frequencies.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GetAlbumPrioritysOutput extends BaseOutput {
    @SerializedName("data")
    public ArrayList<String> albumPrioritys;
}
