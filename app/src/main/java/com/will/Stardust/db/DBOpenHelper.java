package com.will.Stardust.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.will.Stardust.base.MyApplication;

/**
 * Created by Will on 2016/1/31.
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String NAME = "book.db";


    public static final String CREATE_BOOKLIST = "create table book (" +
            "id integer primary key autoincrement," +
            "book_name text," +
            "book_path text)";
    public static final String CREATE_BOOKMARK = "create table chapter (" +
            "id integer primary key autoincrement," +
            "book_name text," +
            "chapter_number integer," +
            "chapter_position integer," +
            "chapter_name)";
    public DBOpenHelper(){
        super(MyApplication.getGlobalContext(),NAME,null,VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_BOOKLIST);
        db.execSQL(CREATE_BOOKMARK);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }

}
