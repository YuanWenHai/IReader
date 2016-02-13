package com.will.ireader.file;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

/**
 * Created by Will on 2016/1/31.
 */
public class IReaderDB {
    public static final String DB_NAME = "book_list";
    public static final int  VERSION = 1;
    private static IReaderDB iReaderDB;
    private SQLiteDatabase db;
    private ArrayList<String> bookName;
    private ArrayList<String> bookPath;
    private ArrayList<String> dirName;
    private ArrayList<String> dirPath;
    private IReaderDB(Context context){
        IReaderOpenHelper helper = new IReaderOpenHelper(context,DB_NAME,null,VERSION);
        db = helper.getWritableDatabase();
    }
    public synchronized static IReaderDB getInstance(Context context){
        if(iReaderDB ==null){
            iReaderDB =  new IReaderDB(context);
        }
        return iReaderDB;
    }
    public void saveBook(String name,String path){
        getBook();
        if(!bookPath.contains(path)){
        ContentValues contentValues = new ContentValues();
        contentValues.put("book_name",name);
        contentValues.put("book_path",path);
        db.insert("BookList",null,contentValues);
    }}
    public void saveDir(String name,String path){
        getDir();
        if(!dirPath.contains(path)){
            ContentValues values = new ContentValues();
            values.put("dir_name",name);
            values.put("dir_path",path);
            db.insert("DirList",null,values);
        }
    }
    //查询文件内容，并为savebook提供path查询，避免重复数据存入表中
    private void getBook(){
        Cursor cursor;
        bookName = new ArrayList<String>();
        bookPath = new ArrayList<String>();
         cursor = db.query("BookList",null,null,null,null,null,null);
        if(cursor.moveToFirst()) {
            do {
                bookName.add(cursor.getString(cursor.getColumnIndex("book_name")));
                bookPath.add(cursor.getString(cursor.getColumnIndex("book_path")));
            } while (cursor.moveToNext());

        }if(cursor!= null){
            cursor.close();
        }
            }


    //查询目录内容
    private void getDir(){
        dirName = new ArrayList<String>();
        dirPath = new ArrayList<String>();

            Cursor cursor = db.query("DirList",null,null,null,null,null,null);
            if(cursor.moveToFirst()) {
                do {
                    dirName.add(cursor.getString(cursor.getColumnIndex("dir_name")));
                    dirPath.add(cursor.getString(cursor.getColumnIndex("dir_path")));
                } while (cursor.moveToNext());

            }
        if(cursor != null){
            cursor.close();
        }
    }


    public ArrayList<String> getBookName(){
        getBook();
        return bookName;
    }
    public ArrayList<String> getBookPath(){
        getBook();
        return bookPath;
    }
    public ArrayList<String> getDirName(){
        return dirName;
    }
    public ArrayList<String> getDirPath(){
        getDir();
        return dirPath;
    }
    public void deleteBook(String name){
        db.delete("BookList","book_name=?",new String[]{name});
    }
    public String getPath(String name){
        String path = "";
        Cursor cursor = db.query("BookList",null,"book_name=?",new String[]{name},null,null,null);
        if(cursor.moveToFirst()){
        path = cursor.getString(cursor.getColumnIndex("book_path"));
        }
        cursor.close();

        return path;
    }
    public void saveBookChapter(String bookName,String chapterName){
        ContentValues values = new ContentValues();
        values.put("book_name",bookName);
        values.put("chapter_name",chapterName);
        db.insert("Bookmark", null, values);
    }
    public String[]getBookChapter(String bookName){
        ArrayList<String> list = new ArrayList<String>();
        String name;
        Cursor cursor = db.query("Bookmark", null, "book_name=?", new String[]{bookName}, null, null, null);
        if(cursor.moveToFirst()){
            do{
                name = cursor.getString(cursor.getColumnIndex("chapter_name"));
                list.add(name);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return  list.toArray(new String[list.size()]);
    }
    public void deleteBookmark(String name){
        db.delete("Bookmark","book_name=?",new String[]{name});
    }
}
