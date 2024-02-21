package com.Meditation.Sounds.frequencies.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.Meditation.Sounds.frequencies.R;
import com.Meditation.Sounds.frequencies.api.models.GetFlashSaleOutput;
import com.Meditation.Sounds.frequencies.utils.Constants;
import com.Meditation.Sounds.frequencies.utils.QcAlarmManager;
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by DC-MEN on 4/15/2018.
 */

public class AlarmReceiver extends BroadcastReceiver {
    public static int countNotification = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        int type = intent.getIntExtra(Constants.ETRAX_FLASH_SALE_TYPE, 0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh::mm:ss", Locale.ENGLISH);
        Log.d("MENDATE", "AlarmReceiver-" + type + "-" + dateFormat.format(Calendar.getInstance().getTime()));
        String jsonFlashSale = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE);
        GetFlashSaleOutput flashSale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);

        if (type == Constants.ETRAX_REMINDER_NOTIFICATION) {
            if (flashSale != null && flashSale.reminder != null) {
                int position = SharedPreferenceHelper.getInstance().getInt(Constants.FREF_REMINDER_NOTIFICATION_ITEM_POSITION);
                ArrayList<String> messages = flashSale.reminder.getMessages();
                String currentMessage = "";
                if (messages != null && messages.size() > 0) {
                    if (position < messages.size() - 1) {
                        position++;
                    } else {
                        position = 0;
                    }
                    currentMessage = messages.get(position);
                    SharedPreferenceHelper.getInstance().setInt(Constants.FREF_REMINDER_NOTIFICATION_ITEM_POSITION, position);
                }
                if (currentMessage != null && currentMessage.length() > 0) {
                    sendNotificaiton(context, currentMessage, type);
                }
            }
            return;
        }

        if (flashSale != null && flashSale.flashSale != null) {
            if (type == Constants.ETRAX_FLASH_SALE_INIT) {
                SharedPreferenceHelper.getInstance().setInt(Constants.PREF_FLASH_SALE_COUNTERED, SharedPreferenceHelper.getInstance().getInt(Constants.PREF_FLASH_SALE_COUNTERED) + 1);
                if (SharedPreferenceHelper.getInstance().getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= flashSale.flashSale.getProposalsCount()) {
                    Intent i = new Intent(Constants.ACTION_RECEIVE_FLASHSALE_NOTIFICATION);
                    i.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, type);
                    context.sendBroadcast(i);
                }
            } else {
                if (SharedPreferenceHelper.getInstance().getInt(Constants.PREF_FLASH_SALE_COUNTERED) == 0) {
                    SharedPreferenceHelper.getInstance().setInt(Constants.PREF_FLASH_SALE_COUNTERED, 1);
                }
                String message = "";
                switch (type) {
                    case Constants.ETRAX_FLASH_SALE_FIRST_NOTIFICATION:
                        if (flashSale.flashSale.getNtf() != null && flashSale.flashSale.getNtf().getFirst() != null) {
                            message = flashSale.flashSale.getNtf().getFirst().getMessage();
                        }
                        break;
                    case Constants.ETRAX_FLASH_SALE_SECOND_NOTIFICATION:
                        if (flashSale.flashSale.getNtf() != null && flashSale.flashSale.getNtf().getSecond() != null) {
                            message = flashSale.flashSale.getNtf().getSecond().getMessage();
                        }
                        break;
                    case Constants.ETRAX_FLASH_SALE_THIRD_NOTIFICATION:
                        if (flashSale.flashSale.getNtf() != null && flashSale.flashSale.getNtf().getThird() != null) {
                            message = flashSale.flashSale.getNtf().getThird().getMessage();
                        }
                        break;
                }

                if (message != null && message.length() > 0) {
                    sendNotificaiton(context, message, type);
                }
            }
            if (SharedPreferenceHelper.getInstance().getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= flashSale.flashSale.getProposalsCount()) {
                QcAlarmManager.createAlarms(context);
            } else {
                QcAlarmManager.clearAlarms(context);
            }
        } else {
            QcAlarmManager.clearAlarms(context);
        }
    }

    public void sendNotificaiton(Context context, String message, int type) {
        String channelId = "Qi Coil Channel ID";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        Intent intentMain = new Intent(context, INotificationBroascast.class);
        intentMain.setAction(Long.toString(System.currentTimeMillis()));
        intentMain.putExtra(Constants.ETRAX_FLASH_SALE_TYPE, type);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intentMain, Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (countNotification > 2000) {
            countNotification = 1;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Qi Coil Channel", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }
        mNotificationManager.notify(++countNotification, mBuilder.build());
    }
}