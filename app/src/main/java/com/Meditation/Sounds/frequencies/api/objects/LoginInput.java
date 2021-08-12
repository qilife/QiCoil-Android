package com.Meditation.Sounds.frequencies.api.objects;

import com.google.gson.annotations.SerializedName;

public class LoginInput {
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;

    public LoginInput(String email, String password){
        this.email = email;
        this.password = password;
    }
}
