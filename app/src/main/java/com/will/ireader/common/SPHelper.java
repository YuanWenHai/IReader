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
    public int getFontSize(){
        return config.getInt("font_size",Util.getPXFromDP(12));
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


    public void setBookmarkStart(String bookName,int position){
        bookmarkEditor.putInt(bookName+"start",position).apply();
    }
    public int getBookmarkStart(String bookName){
        return bookmark.getInt(bookName+"start",0);
    }
    public void setBookmarkNextStart(String bookName, int position){
        bookmarkEditor.putInt(bookName+"next_start",position).apply();
    }
    public int getBookmarkNextStart(String bookName){
        return bookmark.getInt(bookName+"next_start",0);
    }

    public void clearAllBookMarkData(){
        bookmarkEditor.clear().apply();
    }

    public String getBookEncoding(Book book){
        return config.getString(book.getPath(),"");
    }
    public void setBookEncoding(Book book,String encoding){
        configEditor.putString(book.getPath(),encoding).apply();
    }
    public void deleteBookMark(String bookName){
        bookmarkEditor.remove(bookName).apply();
    }
}
