package com.example.hp.sjtuphysics;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    //SharedPreferences 文件名
    private static final String PREF_NAME = "Physics";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    private static final String IS_AUTO_LOGIN = "IsAutoLogin";

    private static final String USER_NAME = "UserName";

    private static final String USER_PASSWORD = "UserPassword";

    private static final String DOWNLOAD_DIRECTORY = "DownloadDirectory";

    public PrefManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime){
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch(){
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setAutoLoginin(boolean isAutoLoginIn) {
        editor.putBoolean(IS_AUTO_LOGIN, isAutoLoginIn);
        editor.commit();
    }

    public boolean isAutoLogin() {
        return pref.getBoolean(IS_AUTO_LOGIN, false);
    }

    public void setUserName(String userName) {
        editor.putString(USER_NAME, userName);
        editor.commit();
    }

    public String getUserName() {
        return pref.getString(USER_NAME, "");
    }

    public void setUserPassword(String userPassword) {
        editor.putString(USER_PASSWORD, userPassword);
        editor.commit();
    }

    public String getUserPassword() {
        return pref.getString(USER_PASSWORD, "");
    }

    public void setDirectory(String directory) {
        editor.putString(DOWNLOAD_DIRECTORY, directory);
        editor.commit();
    }

    public String getDownloadDirectory() {
        return pref.getString(DOWNLOAD_DIRECTORY, _context.getString(R.string.download_file_path_default));
    }
}
