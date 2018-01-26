package com.will.ireader.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.will.ireader.bean.Book;
import com.will.ireader.bean.Chapter;

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
            book.setEncoding(cursor.getString(cursor.getColumnIndex("book_encoding")));
            book.setStartPosition(cursor.getLong(cursor.getColumnIndex("start_position")));
            book.setEndPosition(cursor.getLong(cursor.getColumnIndex("end_position")));
            list.add(book);
        }
        cursor.close();
        return list;
    }
    public ArrayList<Chapter> getChapters(String bookName){
        Cursor cursor = db.rawQuery("SELECT * FROM chapter WHERE book_name=?",new String[]{bookName});
        ArrayList<Chapter> list = new ArrayList<>();
        Chapter chapter;
        while (cursor.moveToNext()){
            chapter = new Chapter();
            chapter.setBookName(cursor.getString(cursor.getColumnIndex("book_name")));
            chapter.setChapterName(cursor.getString(cursor.getColumnIndex("chapter_name")));
            chapter.setChapterParagraphPosition(cursor.getInt(cursor.getColumnIndex("chapter_paragraph_position")));
            chapter.setChapterBytePosition(cursor.getInt(cursor.getColumnIndex("chapter_byte_position")));
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
                    cv.put("chapter_paragraph_position", chapter.getChapterParagraphPosition());
                    cv.put("chapter_byte_position", chapter.getChapterBytePosition());
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
        cv.put("book_encoding",book.getEncoding());
        cv.put("start_position",book.getStartPosition());
        cv.put("end_position",book.getEndPosition());
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

    /**
     * 更新数据库中的某条数据，但不能更新路径，因为路径是文件的标识
     * @param book 要更新的条目
     */
    public void updateBook(Book book){
        ContentValues cv = new ContentValues();
        cv.put("access_time",book.getAccessTime());
        cv.put("book_name",book.getBookName());
        cv.put("book_encoding",book.getEncoding());
        cv.put("start_position",book.getStartPosition());
        cv.put("end_position",book.getEndPosition());
        db.update("book",cv,"book_path=?",new String[]{book.getPath()});
    }

    public void updateBookStartAndEndPosition(Book book,long start,long end){
        ContentValues cv = new ContentValues();
        cv.put("start_position",start);
        cv.put("end_position",end);
        db.update("book",cv,"book_path=?",new String[]{book.getPath()});
    }

    public void deleteChapters(Book book){
        db.delete("chapter","book_name=?",new String[]{book.getBookName()});
    }
}
