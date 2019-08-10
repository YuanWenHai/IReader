package com.will.ireader.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.will.ireader.base.MyApplication;

/**
 * Created by will on 2016/11/3.
 */

public class SPHelper {
    private SharedPreferences config = MyApplication.getGlobalContext().getSharedPreferences("config", Context.MODE_PRIVATE);
    private SharedPreferences.Editor configEditor = config.edit();
    private SharedPreferences bookmark = MyApplication.getGlobalContext().getSharedPreferences("bookmark",Context.MODE_PRIVATE);
    private SharedPreferences.Editor bookmarkEditor = bookmark.edit();
    private static SPHelper instance;

    private SPHelper(){}

    public static SPHelper getInstance(){
        if(instance == null){
            synchronized(SPHelper.class){
                if(instance == null){
                    instance = new SPHelper();
                }
            }
        }
        return instance;
    }

    public void setNightMode(boolean which){
        configEditor.putBoolean("night_mode",which).apply();
    }
    public boolean isNightMode(){
        return config.getBoolean("night_mode",false);
    }


    public static final String DISPLAY_TYPE_NORMAL = "normal";
    public static final String DISPLAY_TYPE_NOTCHED = "notched";
    public void setDisplayType(String type){
        configEditor.putString("display_type",type).apply();
    }
    public String getDisplayType(){
        return config.getString("display_type",null);
    }
}
