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
        Cursor cursor = db.rawQuery("SELECT * FROM book order by _id desc",null);
        List<Book> list = new ArrayList<>();
        Book book;
        while (cursor.moveToNext()){
            book = new Book();
            book.setBookName(cursor.getString(cursor.getColumnIndex("book_name")));
            book.setPath(cursor.getString(cursor.getColumnIndex("book_path")));
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
    public void saveChapters(List<BookDetail> list){
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
    public void saveBook(Book book){
        ContentValues cv = new ContentValues();
        cv.put("book_name",book.getBookName());
        cv.put("book_path",book.getPath());
        db.insert("book","book,name",cv);
    }
    public void saveBook(List<Book> list){
        for(Book book : list){
            saveBook(book);
        }
    }
}
