package com.example.hp.sjtuphysics;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenSizeUtils {

    private static ScreenSizeUtils instance = null;
    private int screenWidth, screenHeight;
    private float displayDensity;

    public static ScreenSizeUtils getInstance (Context mContext) {
        if (instance == null) {
            synchronized (ScreenSizeUtils.class) {
                if (instance == null)
                    instance = new ScreenSizeUtils(mContext);
            }
        }
        return instance;
    }

    private ScreenSizeUtils (Context mContext) {
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        screenHeight = dm.heightPixels;// 获取屏幕分辨率高度
        displayDensity = dm.density;//获取屏幕像素密度
    }

    //获取屏幕宽度
    public int getScreenWidth() {
        return screenWidth;
    }

    //获取屏幕高度
    public int getScreenHeight() {
        return screenHeight;
    }

    public float getDisplayDensity() {
        return displayDensity;
    }
}
