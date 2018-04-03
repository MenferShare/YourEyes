package com.example.menfer.youreyes_v;

import android.app.Application;

import com.hyphenate.easeui.EaseUI;

/**
 * Created by Menfer on 2017/10/29.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        EaseUI.getInstance().init(this,null);
    }
}
