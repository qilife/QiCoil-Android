package com.Meditation.Sounds.frequencies.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.Meditation.Sounds.frequencies.QApplication;

/**
 * Created by dcmen on 08/31/16.
 */
public class SharedPreferenceHelper {
    public static final String SHARED_PREF_NAME = "WELIO2017";
    public static final String SHARED_PREF_KEYBOARD_HEIGH = "KEYBOARD_HEIGH";
    public static final String SHARED_PREF_APP_VERSION = "APP_VERSION";
    private static SharedPreferenceHelper mInstance;
    private SharedPreferences mSharedPreferences;

    private SharedPreferenceHelper(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPreferenceHelper getInstance() {
        if (mInstance == null) {
            mInstance = new SharedPreferenceHelper(QApplication.getInstance());
        }
        return mInstance;
    }

    public void set(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    public String get(String key) {
        return mSharedPreferences.getString(key, null);
    }

    public void removeKey(String key) {
        mSharedPreferences.edit().remove(key).apply();
    }

    public void clearSharePrefs() {
        mSharedPreferences.edit().clear().apply();
    }

    public void setKeyboardHeight(int keyboardHeight) {
        mSharedPreferences.edit().putInt(SHARED_PREF_KEYBOARD_HEIGH, keyboardHeight).apply();
    }

    public int getKeyboardHeight() {
        return mSharedPreferences.getInt(SHARED_PREF_KEYBOARD_HEIGH, 150);
    }

    public void setInt(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
    }

    public int getInt(String key) {
        return mSharedPreferences.getInt(key, 0);
    }


    public void setLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

    public long getLong(String key) {
        return mSharedPreferences.getLong(key, 0L);
    }

    public void setBool(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public boolean getBool(String key) {
        if (key != null
                && (key.equalsIgnoreCase(Constants.KEY_PURCHASED)
                || key.equalsIgnoreCase(Constants.KEY_PURCHASED_ADVANCED)
                || key.equalsIgnoreCase(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)
                || key.equalsIgnoreCase(Constants.KEY_PURCHASED_HIGH_QUANTUM))
                && mSharedPreferences.getBoolean(Constants.IS_UNLOCK_ALL, false)) {
            return true;
        }
        if (key != null) {
            if (key.equalsIgnoreCase(Constants.KEY_PURCHASED)) {
                return mSharedPreferences.getBoolean(Constants.KEY_PURCHASED, false) || mSharedPreferences.getBoolean(Constants.KEY_PURCHASED_DEVICE, false);
            }
            if (key.equalsIgnoreCase(Constants.KEY_PURCHASED_ADVANCED)) {
                return mSharedPreferences.getBoolean(Constants.KEY_PURCHASED_ADVANCED, false) || mSharedPreferences.getBoolean(Constants.KEY_PURCHASED_ADVANCED_DEVICE, false);
            }
            if (key.equalsIgnoreCase(Constants.KEY_PURCHASED_HIGH_ABUNDANCE)) {
                return mSharedPreferences.getBoolean(Constants.KEY_PURCHASED_HIGH_ABUNDANCE, false) || mSharedPreferences.getBoolean(Constants.KEY_PURCHASED_HIGHER_DEVICE, false);
            }
            if (key.equalsIgnoreCase(Constants.KEY_PURCHASED_HIGH_QUANTUM)) {
                return mSharedPreferences.getBoolean(Constants.KEY_PURCHASED_HIGH_QUANTUM, false) || mSharedPreferences.getBoolean(Constants.KEY_PURCHASED_HIGHER_QUANTUM_DEVICE, false);
            }
        }

        return mSharedPreferences.getBoolean(key, false);
    }

    public long getLastOpenedPlaylistId() {
        return mSharedPreferences.getLong("last_playlist_id", 0);
    }

    public void setLastOpenedPlaylistId(long playlistId) {
        mSharedPreferences.edit().putLong("last_playlist_id", playlistId).apply();
    }

    public boolean isShowDisclaimer() {
        return mSharedPreferences.getBoolean("is_show_disclaimer", false);
    }

    public void setShowDisclaimer(boolean isFirstTime) {
        mSharedPreferences.edit().putBoolean("is_show_disclaimer", isFirstTime).apply();
    }

    public void setPriceByCurrency(String priceKey, String value) {
        mSharedPreferences.edit().putString(priceKey, value).apply();
    }

    public String getPriceByCurrency(String priceKey) {
        String price = mSharedPreferences.getString(priceKey, null);
        if (price == null) {
            if (priceKey.equalsIgnoreCase(Constants.PRICE_7_DAY_TRIAL)) {
                return "$12.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_7_DAY_TRIAL_FLASH_SALE)) {
                return "$9.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_1_MONTH)) {
                return "$9.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_1_MONTH_FLASH_SALE)) {
                return "$9.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_1_YEAR)) {
                return "$99.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_1_YEAR_FLASH_SALE)) {
                return "$49.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_LIFETIME)) {
                return "$199.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_LIFETIME_FLASH_SALE)) {
                return "$99.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_FREE_TRIAL)) {
                return "$19.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_FREE_TRIAL_FLASH_SALE)) {
                return "$12.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_MONTHLY)) {
                return "$19.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_MONTHLY_FLASH_SALE)) {
                return "$12.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_YEAR)) {
                return "$199.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_YEAR_FLASH_SALE)) {
                return "$99.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_LIFETIME)) {
                return "$379.99";
            }  else if (priceKey.equalsIgnoreCase(Constants.PRICE_ADVANCED_LIFETIME_FLASH_SALE)) {
                return "$199.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_HIGHER_MONTHLY)) {
                return "$29.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_HIGHER_ANNUAL)) {
                return "$299.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_HIGHER_LIFETIME)) {
                return "$399.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_HIGHER_ANNUAL_FLASH_SALE)) {
                return "$199.99";
            } else if (priceKey.equalsIgnoreCase(Constants.PRICE_HIGHER_LIFETIME_FLASH_SALE)) {
                return "$299.99";
            }
        }
        return price;
    }
}