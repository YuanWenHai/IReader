package com.will.Stardust.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.will.Stardust.bean.Book;
import com.will.Stardust.bean.BookDetail;

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
    public List<BookDetail> getChapters(String bookName){
        Cursor cursor = db.rawQuery("SELECT * FROM chapter WHERE book_name=?",new String[]{bookName});
        List<BookDetail> list = new ArrayList<>();
        BookDetail bookDetail;
        while (cursor.moveToNext()){
            bookDetail = new BookDetail();
            bookDetail.setBookName(cursor.getString(cursor.getColumnIndex("book_name")));
            bookDetail.setChapterName(cursor.getString(cursor.getColumnIndex("chapter_name")));
            bookDetail.setChapterNumber(cursor.getInt(cursor.getColumnIndex("chapter_number")));
            bookDetail.setChapterPosition(cursor.getInt(cursor.getColumnIndex("chapter_position")));
            list.add(bookDetail);
        }
        cursor.close();
        return list;
    }
    public void saveChapters(final List<BookDetail> list){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ContentValues cv;
                for(BookDetail bookDetail :list){
                    cv = new ContentValues();
                    cv.put("book_name",bookDetail.getBookName());
                    cv.put("chapter_name",bookDetail.getChapterName());
                    cv.put("chapter_number",bookDetail.getChapterNumber());
                    cv.put("chapter_position",bookDetail.getChapterPosition());
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
