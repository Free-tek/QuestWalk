package com.botosofttechnologies.questwalk;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class SharedPrefManager {
    private static final String KEY_ACCESS_TOKEN = "token";
    private static final String SHARED_PREF_NAME = "fcmsharedprefdemo";
    private static Context mCtx;
    private static SharedPrefManager mInstance;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        SharedPrefManager sharedPrefManager;
        synchronized (SharedPrefManager.class) {
            if (mInstance == null) {
                mInstance = new SharedPrefManager(context);
            }
            sharedPrefManager = mInstance;
        }
        return sharedPrefManager;
    }

    public boolean storeToken(String token) {
        Editor editor = mCtx.getSharedPreferences(SHARED_PREF_NAME, 0).edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    public String getToken() {
        return mCtx.getSharedPreferences(SHARED_PREF_NAME, 0).getString(KEY_ACCESS_TOKEN, null);
    }
}
