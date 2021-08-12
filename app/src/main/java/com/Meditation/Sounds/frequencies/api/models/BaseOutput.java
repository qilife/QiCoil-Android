package com.Meditation.Sounds.frequencies.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dcmen on 8/31/2016.
 */
public class BaseOutput {
    @SerializedName("code")
    public int code;
    @SerializedName("success")
    public boolean success;
    @SerializedName("error")
    public String error;
    @SerializedName("errorCode")
    public String errorCode;
}
