package com.Meditation.Sounds.frequencies.api.models;

import com.Meditation.Sounds.frequencies.models.Advertisements;
import com.Meditation.Sounds.frequencies.models.FlashSale;
import com.Meditation.Sounds.frequencies.models.Reminder;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Admin on 4/13/2017.
 */

public class GetFlashSaleOutput {
    @SerializedName("flash_sale")
    public FlashSale flashSale;
    @SerializedName("reminder")
    public Reminder reminder;
    @SerializedName("advertisements")
    public Advertisements advertisements;
}
