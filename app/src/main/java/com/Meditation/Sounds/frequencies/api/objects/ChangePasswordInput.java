package com.Meditation.Sounds.frequencies.api.objects;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordInput {
    @SerializedName("old_password")
    public String old_password;
    @SerializedName("new_password")
    public String new_password;
    @SerializedName("confirm_password")
    public String confirm_password;

    public ChangePasswordInput(String old_password, String new_password, String confirm_password) {
        this.old_password = old_password;
        this.new_password = new_password;
        this.confirm_password = confirm_password;
    }
}
