package com.Meditation.Sounds.frequencies.utils;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by dcmen on 08/31/16.
 */
public class StringUtils {
    private static final DecimalFormat NUMBER_TIME_FORMATTER = (DecimalFormat) NumberFormat.getInstance();

    static {
        NUMBER_TIME_FORMATTER.applyPattern("00");
    }

    public static String getDateStringFromTimestampFull(long timestamp) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return mSimpleDateFormat.format(new Date(timestamp));
    }

    public static String getDateStringFromTimestamp(long timestamp) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return mSimpleDateFormat.format(new Date(timestamp - getOffsetInMillis()));
    }

    public static String getFullDate2StringFromTimestamp(long timestamp) {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("hh:mm aaa, dd/MM/yyyy");
        return mSimpleDateFormat.format(new Date(timestamp - getOffsetInMillis()));
    }

    public static int getOffsetInMillis() {
        TimeZone tz = TimeZone.getDefault();
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());
        return offsetInMillis;
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d");
    }

    public static boolean isContainStringVN(String source, String cs) {
        Locale locale = new Locale("VN", "vi");
        String normalizeString = removeAccent(source).toLowerCase();
        return (source.contains(cs.toLowerCase(locale))
                || normalizeString.contains(cs.toLowerCase(locale)));
    }

    public static boolean isEmpty(String text) {
        return (TextUtils.isEmpty(text) || text.toLowerCase().equals("null"));
    }

    public static boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isValidPhone(String phone) {
        String PHONE_PATTERN = "^\\+[0-9]{10,13}$";

        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public static String toJSONString(Map<String, String> params) {
        JSONObject object = new JSONObject();
        for (String key : params.keySet()) {
            try {
                object.put("key", params.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object.toString();
    }

    public static String getParamsRequest(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        if (params.size() > 0) {
            for (String key : params.keySet()) {
                if (builder.length() > 0) {
                    builder.append("&");
                }
                builder.append(key).append("=");
                try {
                    builder.append(URLEncoder.encode(params.get(key), Constants.getCHARSET()));
                } catch (UnsupportedEncodingException e) {
                    builder.append(params.get(key));
                }
            }
        }
        return builder.toString();
    }

    public static String eliminateDecimal(double num) {
        return String.format("%.0f", num);
    }

    public static String formatDistanceInKm(float distanceInKm) {
        return String.format(Locale.US, "%.1f", distanceInKm);
    }

    public static String toString(long milliseconds) {
        long second = Math.round(milliseconds / 1000f);
        int minute = (int) (second / 60);
        int hour = (int) (second / 3600);
        second = second % 60;
        minute = minute % 60;
        return (hour > 0 ? hour + ":" : "") + (minute > 0 ? NUMBER_TIME_FORMATTER.format(minute) : "00") + ":" + (second > 0 ? NUMBER_TIME_FORMATTER.format(second) : "00");
    }

    public static String getFileName(String uri) {
        if (uri.contains("/")) {
            return uri.substring(uri.lastIndexOf("/") + 1);
        } else if (uri.contains("\\")) {
            return uri.substring(uri.lastIndexOf("\\") + 1);
        }
        return uri;
    }
    public static String getFileNameWithoutExtension(String fileName){
        if(fileName.contains(".")){
            return fileName.substring(0,fileName.lastIndexOf("."));
        }else{
            return fileName;
        }
    }

    public static String getDeviceId(Context context) {
        String id = getUniqueID(context);
        if (id == null)
            id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return id;
    }

    private static String getUniqueID(Context context) {
        String telephonyDeviceId = "NoTelephonyId";
        String androidDeviceId = "NoAndroidId";
        try {
            androidDeviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
            if (androidDeviceId == null) {
                androidDeviceId = "NoAndroidId";
            }
        } catch (Exception e) {

        }
        try {
            String id = getStringIntegerHexBlocks(androidDeviceId.hashCode())
                    + "-"
                    + getStringIntegerHexBlocks(telephonyDeviceId.hashCode());
            return id;
        } catch (Exception e) {
            return null;
        }
    }


    public static String getStringIntegerHexBlocks(int value) {
        String result = "";
        String string = Integer.toHexString(value);
        int remain = 8 - string.length();
        char[] chars = new char[remain];
        Arrays.fill(chars, '0');
        string = new String(chars) + string;
        int count = 0;
        for (int i = string.length() - 1; i >= 0; i--) {
            count++;
            result = string.substring(i, i + 1) + result;
            if (count == 4) {
                result = "-" + result;
                count = 0;
            }
        }
        if (result.startsWith("-")) {
            result = result.substring(1, result.length());
        }
        return result;
    }

    public static String getFileExtension(String path) {
        if (path != null)
            return path.substring(path.lastIndexOf(".") + 1);
        else return "";
    }
}
