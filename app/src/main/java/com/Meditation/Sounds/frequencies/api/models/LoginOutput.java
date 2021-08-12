package com.Meditation.Sounds.frequencies.api.models;

import com.Meditation.Sounds.frequencies.models.Profile;
import com.google.gson.annotations.SerializedName;

public class LoginOutput extends BaseOutput {
    @SerializedName("data")
    public Data data;

    public class Data{
        @SerializedName("token")
        public String token;
        @SerializedName("profile")
        public Profile profile;
    }
}
