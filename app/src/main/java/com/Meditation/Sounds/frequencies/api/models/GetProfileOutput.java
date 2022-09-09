package com.Meditation.Sounds.frequencies.api.models;

import com.Meditation.Sounds.frequencies.models.Profile;
import com.google.gson.annotations.SerializedName;

public class GetProfileOutput extends BaseOutput {
    @SerializedName("data")
    public Profile data;
}
