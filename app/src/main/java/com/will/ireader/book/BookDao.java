package com.will.ireader.book;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.will.ireader.book.Book;

import java.util.List;

/**
 * created  by will on 2019/7/26 10:50
 */
@Dao
public interface BookDao {

    @Query("SELECT * FROM book")
    List<Book> getAllBooks();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addBook(Book... book);

    @Update
    void updateBook(Book book);

    @Delete
    void deleteBook(Book book);

    @Query("SELECT * FROM  book WHERE path LIKE :path")
    Book getBook(String path);

}
