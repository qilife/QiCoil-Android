package com.Meditation.Sounds.frequencies.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;

import com.Meditation.Sounds.frequencies.R;
import com.Meditation.Sounds.frequencies.api.models.GetFlashSaleOutput;
import com.google.gson.Gson;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {

    public static void createKeyHash(Context context){
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {
        }
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public static int getMediaTypeBasic(int albumPriority) {
        if (albumPriority <= 2) {
            return Constants.MEDIA_TYPE_BASIC_FREE;
        }
        return Constants.MEDIA_TYPE_BASIC;
    }

    public static long getFlashSaleRemainTime() {
        long flashSaleRemainTime = 0L;
        String jsonFlashSale = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE);
        if (jsonFlashSale != null && jsonFlashSale.length() > 0) {
            GetFlashSaleOutput flashsale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);
            if (flashsale.flashSale != null) {
                if (flashsale.flashSale.getEnable()) {
                    float initDelay = flashsale.flashSale.getInitDelay();
                    float duration = flashsale.flashSale.getDuration();
                    float interval = flashsale.flashSale.getInterval();

                    Calendar currentCal = Calendar.getInstance();

                    long fistIntallerAppTime = SharedPreferenceHelper.getInstance().getLong(Constants.ETRAX_FIRST_INSTALLER_APP_TIME);
                    long initFSTime = fistIntallerAppTime + (long) (initDelay * 60 * 60 * 1000);

                    if (currentCal.getTimeInMillis() - fistIntallerAppTime >= (long) (initDelay * 60 * 60 * 1000)) {
                        Calendar calInitFSTime = Calendar.getInstance();
                        calInitFSTime.setTimeInMillis(initFSTime);

                        int count = 0;
                        while (calInitFSTime.before(currentCal)) {
                            count++;
                            calInitFSTime.add(Calendar.SECOND, (int) (interval * 24 * 60 * 60));
                        }
                        if (count > 0) {
                            calInitFSTime.add(Calendar.SECOND, -1 * (int) (interval * 24 * 60 * 60));
                        }
                        if(SharedPreferenceHelper.getInstance().getInt(Constants.PREF_FLASH_SALE_COUNTERED) <= flashsale.flashSale.getProposalsCount()) {
                            flashSaleRemainTime = calInitFSTime.getTimeInMillis() + (long) (duration * 60 * 60 * 1000) - Calendar.getInstance().getTimeInMillis();
                        }
                    }
                }
            }
        }
        return flashSaleRemainTime;
    }

    public static String getDateFlashSale() {
        String timeString = "April 4th";
        SimpleDateFormat formatMonth = new SimpleDateFormat("MMMM");
        SimpleDateFormat formatDate = new SimpleDateFormat("d");
        try {
            timeString = formatMonth.format(Calendar.getInstance().getTime()) + " " + getDays(Integer.valueOf(formatDate.format(Calendar.getInstance().getTime())));
            String jsonFlashSale = SharedPreferenceHelper.getInstance().get(Constants.PREF_FLASH_SALE);
            if (jsonFlashSale != null && jsonFlashSale.length() > 0) {
                GetFlashSaleOutput flashsale = new Gson().fromJson(jsonFlashSale, GetFlashSaleOutput.class);
                if (flashsale.flashSale != null) {
                    if (flashsale.flashSale.getEnable()) {
                        float initDelay = flashsale.flashSale.getInitDelay();
                        float duration = flashsale.flashSale.getDuration();
                        float interval = flashsale.flashSale.getInterval();

                        Calendar currentCal = Calendar.getInstance();

                        long fistIntallerAppTime = SharedPreferenceHelper.getInstance().getLong(Constants.ETRAX_FIRST_INSTALLER_APP_TIME);
                        long initFSTime = fistIntallerAppTime + (long) (initDelay * 60 * 60 * 1000);

                        if (currentCal.getTimeInMillis() - fistIntallerAppTime >= (long) (initDelay * 60 * 60 * 1000)) {
                            Calendar calInitFSTime = Calendar.getInstance();
                            calInitFSTime.setTimeInMillis(initFSTime);

                            int count = 0;
                            while (calInitFSTime.before(currentCal)) {
                                count++;
                                calInitFSTime.add(Calendar.SECOND, (int) (interval * 24 * 60 * 60));
                            }
                            if (count > 0) {
                                calInitFSTime.add(Calendar.SECOND, -1 * (int) (interval * 24 * 60 * 60));
                            }
                            long flashSaleTime = calInitFSTime.getTimeInMillis() + (long) (duration * 60 * 60 * 1000);
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(flashSaleTime);
                            timeString = formatMonth.format(cal.getTime()) + " " + getDays(Integer.valueOf(formatDate.format(cal.getTime())));

                        }
                    }
                }
            }
        } catch (NumberFormatException e) {

        } catch (Exception ex) {

        }
        return timeString;
    }

    public static String getDays(int day) {
        String dayString = "";
        switch (day) {
            case 1:
            case 21:
            case 31:
                dayString = day + "st";
                break;
            case 2:
            case 22:
                dayString = day + "nd";
                break;
            case 3:
            case 23:
                dayString = day + "rd";
                break;
            default:
                dayString = day + "th";
                break;
        }
        return dayString;
    }

    public static boolean isConnectedToNetwork(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isConnected = false;
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = (activeNetwork != null) && (activeNetwork.isConnectedOrConnecting());
        }
        return isConnected;
    }
    public static int getCurrentVolume(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
}
