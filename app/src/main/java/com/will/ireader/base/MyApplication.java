package com.will.ireader.base;

import android.app.Application;
import android.content.Context;

/**
 * Created by will on 2016/10/29.
 */

public class MyApplication extends Application {
    private static Context mContext;
    @Override
    public void onCreate(){
        super.onCreate();
        mContext = getApplicationContext();
    }
    public static Context getGlobalContext(){
        return mContext;
    }

}
