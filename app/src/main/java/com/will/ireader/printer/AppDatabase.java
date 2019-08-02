package com.will.ireader.printer;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * created  by will on 2019/8/1 14:56
 */
@Database(entities = {Book.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NAME = "ireader_database";


    private static AppDatabase mInstance;

    public static AppDatabase getInstance(Context context){
        if(mInstance == null){
            synchronized (AppDatabase.class){
                if(mInstance == null){
                    mInstance = Room.databaseBuilder(context,AppDatabase.class,AppDatabase.NAME).build();
                }
            }
        }
        return mInstance;
    }
    public static void destroy(){
        AppDatabase.mInstance = null;
    }

    public abstract BookDao bookDao();

}
