package com.Meditation.Sounds.frequencies.api;

import android.content.Context;

import com.Meditation.Sounds.frequencies.BuildConfig;
import com.Meditation.Sounds.frequencies.api.exception.ApiException;
import com.Meditation.Sounds.frequencies.api.models.ForgotPasswordOutput;
import com.Meditation.Sounds.frequencies.api.models.GetAPKsNewVersionOutput;
import com.Meditation.Sounds.frequencies.api.models.GetAlbumOutput;
import com.Meditation.Sounds.frequencies.api.models.GetAlbumPrioritysOutput;
import com.Meditation.Sounds.frequencies.api.models.GetProfileOutput;
import com.Meditation.Sounds.frequencies.api.models.GetTokenOutput;
import com.Meditation.Sounds.frequencies.api.models.LoginOutput;
import com.Meditation.Sounds.frequencies.api.models.RegisterOutput;
import com.Meditation.Sounds.frequencies.api.objects.ChangePasswordInput;
import com.Meditation.Sounds.frequencies.api.objects.CheckAlbumInput;
import com.Meditation.Sounds.frequencies.api.objects.ForgotPasswordInput;
import com.Meditation.Sounds.frequencies.api.objects.LoginInput;
import com.Meditation.Sounds.frequencies.api.objects.RegisterInput;
import com.Meditation.Sounds.frequencies.http.HttpApiWithSessionAuth;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dcmen on 08/31/16.
 */
public class TaskApi {
    // URL
    public static final String TASK_WS = "http://www.quantum.ingeniusstudios.com/api";
    public static final String TASK_WS_ADMIN_PANEL = "http://www.quantum.ingeniusstudios.com/api";
    public static final String TASK_WS_PLASH_SALE = "https://irateadmin.ingeniusstudios.com/irateconfigs/v2/iOS_QiCoil_Meditation_Sounds.json";

    private HttpApiWithSessionAuth mHttpApi;
    private String mDomain;
    private String mDomainQiCoil;
    private Context mContext;
    private Gson mGson;

    public TaskApi(Context context) {
        mContext = context;
        mHttpApi = new HttpApiWithSessionAuth(context);
        mGson = new Gson();
        mDomain = TASK_WS;
        mDomainQiCoil = TASK_WS_ADMIN_PANEL;
    }

    public TaskApi setCredentials(String token) {
        if (token == null || token.length() == 0)
            mHttpApi.clearCredentials();
        else
            mHttpApi.setCredentials(token);
        return this;
    }

    public String getFullUrl(String subUrl) {
        return mDomain + subUrl;
    }

    public String getFullUrlQiCoil(String subUrl) {
        return mDomainQiCoil + subUrl;
    }

    public String getUrlQiCoil(String subUrl) {
        return "http://www.combined.ingeniusstudios.com/public/api" + subUrl;
    }

    public GetTokenOutput getToken() throws ApiException, JSONException, IOException {
        JSONObject requestData = new JSONObject();
        requestData.put("email", "hoanghuyhung@live.com");
        JSONObject data = mHttpApi.doHttpPost(getFullUrl("/token"), requestData.toString());
        GetTokenOutput output = mGson.fromJson(data.toString(), GetTokenOutput.class);
        return output;
    }

    public LoginOutput loginByEmail(LoginInput input) throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpPost(getFullUrlQiCoil("/login"), new Gson().toJson(input));
        return mGson.fromJson(data.toString(), LoginOutput.class);
    }

    public String checkAlbums(CheckAlbumInput input) throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpPost(getUrlQiCoil("/checkfreealbum"), new Gson().toJson(input));
        return data.toString();
    }

    public RegisterOutput registerByEmail(RegisterInput input) throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpPost(getFullUrlQiCoil("/register"), new Gson().toJson(input));
        return mGson.fromJson(data.toString(), RegisterOutput.class);
    }

    public GetProfileOutput getProfile() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpPost(String.format(getFullUrlQiCoil("/profile")), new Gson().toJson(""));
        return mGson.fromJson(data.toString(), GetProfileOutput.class);
    }

    public ForgotPasswordOutput forgotPassword(ForgotPasswordInput input) throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpPost(String.format(getFullUrlQiCoil("/recovery")), new Gson().toJson(input));
        return mGson.fromJson(data.toString(), ForgotPasswordOutput.class);
    }

    public GetProfileOutput changePassword(ChangePasswordInput input) throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpPost(String.format(getFullUrlQiCoil("/change-password")), new Gson().toJson(input));
        return mGson.fromJson(data.toString(), GetProfileOutput.class);
    }

    public GetAlbumOutput getAlbums() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/albumsV4"));
        GetAlbumOutput output = mGson.fromJson(data.toString(), GetAlbumOutput.class);
        return output;
    }

    public GetAPKsNewVersionOutput getAPKsNewVersion() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAPKFiles"));
        GetAPKsNewVersionOutput output = mGson.fromJson(data.toString(), GetAPKsNewVersionOutput.class);
        return output;
    }

    public GetAPKsNewVersionOutput getDefaultPlaylist() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getPlaylistJsonFilesV4"));
        GetAPKsNewVersionOutput output = mGson.fromJson(data.toString(), GetAPKsNewVersionOutput.class);
        return output;
    }

    public GetAlbumPrioritysOutput getAlbumPrioritys() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAlbumsPriorityV4"));
        GetAlbumPrioritysOutput output = mGson.fromJson(data.toString(), GetAlbumPrioritysOutput.class);
        return output;
    }

    public GetAlbumOutput getAlbumsAdvanced() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAlbumsAdvancedV4"));
        return mGson.fromJson(data.toString(), GetAlbumOutput.class);
    }

    public GetAlbumPrioritysOutput getAlbumAdvancedPrioritys() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAlbumsAdvancedPriorityV4"));
        return mGson.fromJson(data.toString(), GetAlbumPrioritysOutput.class);
    }

    public GetAlbumOutput getAlbumsHigherAbundance() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAlbumsHigherAbundanceV4"));
        return mGson.fromJson(data.toString(), GetAlbumOutput.class);
    }

    public GetAlbumPrioritysOutput getAlbumsHigherAbundancePriority() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAlbumsHigherAbundancePriorityV4"));
        return mGson.fromJson(data.toString(), GetAlbumPrioritysOutput.class);
    }

    public GetAlbumOutput getAlbumsHigherQuantum() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAlbumsHigherQuantumV4"));
        return mGson.fromJson(data.toString(), GetAlbumOutput.class);
    }

    public GetAlbumPrioritysOutput getAlbumsHigherQuantumPriority() throws ApiException, JSONException, IOException {
        JSONObject data = mHttpApi.doHttpGet(getFullUrl("/getAlbumsHigherQuantumPriorityV4"));
        return mGson.fromJson(data.toString(), GetAlbumPrioritysOutput.class);
    }
}
