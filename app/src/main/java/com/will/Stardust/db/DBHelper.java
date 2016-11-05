package com.will.Stardust.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.will.Stardust.bean.Book;
import com.will.Stardust.bean.Chapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class DBHelper {
    private static DBHelper instance;
    private SQLiteDatabase db;


    private DBHelper(){
        db = new DBOpenHelper().getWritableDatabase();
    }

    public static DBHelper getInstance(){
        if(instance == null){
            synchronized(DBHelper.class){
                if(instance == null){
                    instance = new DBHelper();
                }
            }
        }
        return instance;
    }
    public List<Book> getAllBook(){
        Cursor cursor = db.rawQuery("SELECT * FROM book order by access_time desc",null);
        List<Book> list = new ArrayList<>();
        Book book;
        while (cursor.moveToNext()){
            book = new Book();
            book.setBookName(cursor.getString(cursor.getColumnIndex("book_name")));
            book.setPath(cursor.getString(cursor.getColumnIndex("book_path")));
            book.setAccessTime(cursor.getLong(cursor.getColumnIndex("access_time")));
            list.add(book);
        }
        cursor.close();
        return list;
    }
    public List<Chapter> getChapters(String bookName){
        Cursor cursor = db.rawQuery("SELECT * FROM chapter WHERE book_name=?",new String[]{bookName});
        List<Chapter> list = new ArrayList<>();
        Chapter chapter;
        while (cursor.moveToNext()){
            chapter = new Chapter();
            chapter.setBookName(cursor.getString(cursor.getColumnIndex("book_name")));
            chapter.setChapterName(cursor.getString(cursor.getColumnIndex("chapter_name")));
            chapter.setChapterNumber(cursor.getInt(cursor.getColumnIndex("chapter_number")));
            chapter.setChapterPosition(cursor.getInt(cursor.getColumnIndex("chapter_position")));
            list.add(chapter);
        }
        cursor.close();
        return list;
    }
    public void saveChapters(final List<Chapter> list){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues cv;
                for(Chapter chapter :list){
                    cv = new ContentValues();
                    cv.put("book_name", chapter.getBookName());
                    cv.put("chapter_name", chapter.getChapterName());
                    cv.put("chapter_number", chapter.getChapterNumber());
                    cv.put("chapter_position", chapter.getChapterPosition());
                    db.insert("chapter","book_name",cv);
                }
            }
        }).start();
    }
    public void saveBook( Book book){
        ContentValues cv = new ContentValues();
        cv.put("book_name",book.getBookName());
        cv.put("book_path",book.getPath());
        cv.put("access_time",book.getAccessTime());
        db.insert("book","book,name",cv);
    }
    public void saveBook(final List<Book> list){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(final Book book : list){
                    saveBook(book);
                }
            }
        }).start();

    }
    public void deleteBookWithChapters(final Book book){
        db.delete("book","book_name=?",new String[]{book.getBookName()});
        db.delete("chapter","book_name=?",new String[]{book.getBookName()});
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.delete("book","book_name=?",new String[]{book.getBookName()});
                db.delete("chapter","book_name=?",new String[]{book.getBookName()});
            }
        }).start();
    }
    public void clearAllData(){
        db.delete("book",null,null);
        db.delete("chapter",null,null);
    }
    public void updateBookAccessTime(Book book){
        ContentValues cv = new ContentValues();
        cv.put("access_time",book.getAccessTime());
        db.update("book",cv,"book_path=?",new String[]{book.getPath()});
    }
}
