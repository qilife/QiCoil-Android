package com.Meditation.Sounds.frequencies;

import android.content.Context;
import android.os.AsyncTask;
import android.os.LocaleList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.work.Configuration;

import com.Meditation.Sounds.frequencies.api.ApiListener;
import com.Meditation.Sounds.frequencies.db.QFDatabase;
import com.Meditation.Sounds.frequencies.db.dao.PlaylistDAO;
import com.Meditation.Sounds.frequencies.feature.base.BaseActivity;
import com.Meditation.Sounds.frequencies.models.Playlist;
import com.Meditation.Sounds.frequencies.tasks.BaseTask;
import com.Meditation.Sounds.frequencies.tasks.UpdateDurationOfAllPlaylistTask;
import com.Meditation.Sounds.frequencies.tasks.UpdatePlaylistInforVer10Task;
import com.Meditation.Sounds.frequencies.utils.Constants;
import com.Meditation.Sounds.frequencies.utils.FilesUtils;
import com.Meditation.Sounds.frequencies.utils.LanguageUtils;
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper;
import com.appsflyer.AppsFlyerLib;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;

public class QApplication extends MultiDexApplication implements ApiListener, Configuration.Provider {
    private static QApplication INSTANCE;
    static int APP_VERSION = 12;
    private ArrayList<BaseActivity> mStacksActivity;

    public static boolean isActivityDownloadStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
//        AppCompatDelegate.setApplicationLocales(LanguageUtils.Companion.getLocaleList(this));

        INSTANCE = this;
        AppsFlyerLib.getInstance().init("aNPCN6auSrzidSGCeMrg9R", null, this);
        AppsFlyerLib.getInstance().start(this);
        int version = SharedPreferenceHelper.getInstance().getInt(SharedPreferenceHelper.SHARED_PREF_APP_VERSION);
        if (version < APP_VERSION) {
            onUpgradeVersion(version, APP_VERSION);
            SharedPreferenceHelper.getInstance().setInt(SharedPreferenceHelper.SHARED_PREF_APP_VERSION, APP_VERSION);
        }
        mStacksActivity = new ArrayList<>();
    }

    public void addActivityToStack(BaseActivity activity) {
        if (mStacksActivity.size() > 0 && mStacksActivity.get(mStacksActivity.size() - 1).getClass() == activity.getClass()) {
            return;
        }
        mStacksActivity.add(activity);
    }

    public void removeActivityToStack(BaseActivity activity) {
        if (mStacksActivity.size() > 0 && mStacksActivity.get(mStacksActivity.size() - 1).getClass() == activity.getClass()) {
            mStacksActivity.remove(mStacksActivity.size() - 1);
        }
    }

    public BaseActivity getTopActivity() {
        return mStacksActivity.size() > 0 ? mStacksActivity.get(mStacksActivity.size() - 1) : null;
    }

    public static QApplication getInstance() {
        return INSTANCE;
    }

    public void onUpgradeVersion(int oldVersion, int newVersion) {
        if (oldVersion < 9) {
            try {
                FilesUtils.deleteRecursive(new File(FilesUtils.getSdcardStore(), Constants.DEFAULT_DATA_FOLDER));
            } catch (Throwable e) {
            }
            QFDatabase database = QFDatabase.getDatabase(this);
            database.albumDAO().clear();
            database.songDAO().clear();
            database.playlistDAO().clear();
            database.playlistItemDAO().clear();
            database.playlistItemSongDAO().clear();
        }
        if (oldVersion < 10) {
            new UpdatePlaylistInforVer10Task(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (oldVersion < 11) {
            new UpdateDurationOfAllPlaylistTask(this, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (oldVersion < 12) {
            PlaylistDAO playlistDao = QFDatabase.getDatabase(this).playlistDAO();
            Playlist playlist = playlistDao.getFirstModifiedPlaylist();
            if (playlist != null) {
                playlist.getTitle();
                if (playlist.getTitle().equalsIgnoreCase("Playlist 1")) {
                    playlistDao.updateFromUserFromId(playlist.getId(), 1);
                }
            }
        }
    }

    @Override
    public void onConnectionOpen(BaseTask task) {

    }

    @Override
    public void onConnectionSuccess(BaseTask task, Object data) {

    }

    @Override
    public void onConnectionError(BaseTask task, Exception exception) {

    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        if (BuildConfig.DEBUG) {
            return new Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.DEBUG)
                    .setExecutor(Executors.newSingleThreadExecutor())
                    .setTaskExecutor(Executors.newSingleThreadExecutor())
                    .build();
        } else {
            return new Configuration.Builder()
                    .setMinimumLoggingLevel(android.util.Log.ERROR)
                    .setExecutor(Executors.newSingleThreadExecutor())
                    .setTaskExecutor(Executors.newSingleThreadExecutor())
                    .build();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
