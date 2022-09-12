package com.Meditation.Sounds.frequencies.api.models;

import com.google.gson.annotations.SerializedName;
import com.Meditation.Sounds.frequencies.models.Album;

import java.util.ArrayList;


public class GetAlbumOutput extends BaseOutput{
    @SerializedName("data")
    public ArrayList<Album> albums;
}
