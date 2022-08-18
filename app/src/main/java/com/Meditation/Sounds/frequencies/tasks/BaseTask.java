package com.Meditation.Sounds.frequencies.tasks;

import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import com.Meditation.Sounds.frequencies.api.ApiListener;
import com.Meditation.Sounds.frequencies.api.TaskApi;
import com.Meditation.Sounds.frequencies.api.exception.ApiException;
import com.Meditation.Sounds.frequencies.utils.Constants;
import com.Meditation.Sounds.frequencies.utils.SharedPreferenceHelper;

import org.json.JSONException;
import java.io.IOException;


public abstract class BaseTask<Output> extends AsyncTask<Void,Exception, Output> {
    protected TaskApi mApi;
    private ApiListener<Output> mListener;
    private Exception mException = null;

    public BaseTask(Context context, @Nullable ApiListener<Output> listener) {
        mListener = listener;
        mApi = new TaskApi(context);
        if(SharedPreferenceHelper.getInstance().get(Constants.PREF_SESSION_ID) != null && SharedPreferenceHelper.getInstance().get(Constants.PREF_SESSION_ID).length() > 0) {
            mApi.setCredentials(SharedPreferenceHelper.getInstance().get(Constants.PREF_SESSION_ID));
        }
    }

    @Override
    @MainThread
    final protected void onPreExecute() {
        if (mListener != null) mListener.onConnectionOpen(this);
    }

    @Override
    final protected Output doInBackground(Void... params) {
        try {
            return callApiMethod();
        } catch (Exception e) {
            mException = e;
            return null;
        }
    }

    @Override
    @MainThread
    final protected void onPostExecute(Output output) {
        if (mListener != null && mException != null)
            mListener.onConnectionError(this, mException);
        else if (mListener != null)
            mListener.onConnectionSuccess(this, output);
    }

    /**
     * Override it with api method in YsApi which we want to execute
     *
     * @return
     * @throws ApiException
     * @throws JSONException
     * @throws IOException
     */
    protected abstract Output callApiMethod()
            throws ApiException, JSONException, IOException, Exception;

}
