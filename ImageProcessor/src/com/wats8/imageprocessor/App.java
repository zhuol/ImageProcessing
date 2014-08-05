package com.wats8.imageprocessor;

import android.app.Application;

import me.kiip.sdk.Kiip;

/**
 * Created by zhuol on 8/5/2014.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Kiip kiip = Kiip.init(this, "586035f55e40e39a3ec97ce1be018386", "d060fbb7d5ba513088bc24ae8bfdcb18");
        Kiip.setInstance(kiip);
    }
}
