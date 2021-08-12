package com.Meditation.Sounds.frequencies.api.objects;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordInput {
    @SerializedName("email")
    public String email;

    public ForgotPasswordInput(String email) {
        this.email = email;
    }
}
