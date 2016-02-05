package com.will.ireader.file;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Will on 2016/1/31.
 */
public class IReaderOpenHelper extends SQLiteOpenHelper {
    public static final String CREATE_BOOKLIST = "create table BookList (" +
            "id integer primary key autoincrement," +
            "book_name text," +
            "book_path text)";
    public static final String CREATE_DIRLIST = "create table DirList(" +
            "id integer primary key autoincrement," +
            "dir_name text," +
            "dir_path text)";
    public static final String CREATE_BOOKMARK = "create table Bookmark (" +
            "id integer primary key autoincrement," +
            "book_name text," +
            "chapter_number integer," +
            "chapter_position integer," +
            "chapter_name)";
    public IReaderOpenHelper(Context context,String name,SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_BOOKLIST);
        db.execSQL(CREATE_DIRLIST);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }

}
