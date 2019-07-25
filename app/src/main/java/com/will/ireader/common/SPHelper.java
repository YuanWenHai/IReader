package com.will.ireader.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.will.ireader.base.MyApplication;
import com.will.ireader.bean.Book;

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
    public int getFontSize(int fallback){
        return config.getInt("font_size",fallback);
    }
    public int getFontSize(){
        return config.getInt("font_size",16);
    }
    public void setFontSize(int size){
        configEditor.putInt("font_size",size).apply();
    }



    public void setNightMode(boolean which){
        configEditor.putBoolean("night_mode",which).apply();
    }
    public boolean isNightMode(){
        return config.getBoolean("night_mode",false);
    }


    public void setBookmark(String bookIdentifier, int position){
        bookmarkEditor.putInt(bookIdentifier+"_bookmark",position).apply();
    }
    public int getBookmark(String bookIdentifier){
        return bookmark.getInt(bookIdentifier+"_bookmark",0);
    }
    public void clearAllBookMarkData(){
        bookmarkEditor.clear().apply();
    }


    public String getBookCharset(String bookIdentifier,String defaultValue){
        return config.getString(bookIdentifier,defaultValue);
    }
    public void setBookCharset(String bookIdentifier, String encoding){
        configEditor.putString(bookIdentifier,encoding).apply();
    }
    public void deleteBookMark(String bookName){
        bookmarkEditor.remove(bookName).apply();
    }
}
