package com.Meditation.Sounds.frequencies.api.objects;

import com.google.gson.annotations.SerializedName;

public class RegisterInput {
    @SerializedName("name")
    public String name;
    @SerializedName("email")
    public String email;
    @SerializedName("password")
    public String password;
    @SerializedName("c_password")
    public String c_password;
    @SerializedName("deviceId")
    public String deviceId;
    @SerializedName("os")
    public int os;

    public RegisterInput(String name,String email, String password,String confirmPassword,String deviceId,int os){
        this.name = name;
        this.email = email;
        this.password = password;
        this.c_password = confirmPassword;
        this.deviceId = deviceId;
        this.os = os;
    }
}
