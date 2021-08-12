package com.Meditation.Sounds.frequencies.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.Meditation.Sounds.frequencies.feature.main.MainActivity;
import com.Meditation.Sounds.frequencies.utils.Constants;


/**
 * Created by dcmen on 20/11/2017.
 */
public class INotificationBroascast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.hasExtra(Constants.ETRAX_FLASH_SALE_TYPE)){
            int type = intent.getIntExtra(Constants.ETRAX_FLASH_SALE_TYPE, 0);
            if(type == 0){
                return;
            }
            if(MainActivity.ourMainRunning){
                Intent i = new Intent(Constants.ACTION_RECEIVE_FLASHSALE_NOTIFICATION);
                i.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, type);
                context.sendBroadcast(i);
            } else {
                Intent ii = new Intent(context, MainActivity.class);
                ii.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, type);
                ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ii);
            }
        }
    }
}
