package com.Meditation.Sounds.frequencies.api.models;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordOutput extends BaseOutput {
    @SerializedName("data")
    public Data data;

    public class Data {
        @SerializedName("message")
        public String message;
    }
}
