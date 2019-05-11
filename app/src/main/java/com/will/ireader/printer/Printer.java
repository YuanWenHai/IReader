package com.will.ireader.printer;

import android.util.Log;

import com.will.ireader.bean.Book;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class Printer {

    private final Book book;
    private RandomAccessFile file;
    private FileReader reader;

    public Printer(Book book) {
        this.book = book;
    }

    private void initialize (){
        String bookStr = BookReader.read(new File(book.getPath()), Charset.forName("gbk"));
        if(bookStr != null){

        }

    }



    private void printLine(){

    }


    private void printParagraph(long start,long end){
        try{
            file.readByte();
            file.getChannel().map(FileChannel.MapMode.READ_ONLY,0,file.length());

        }catch (IOException i){
            Log.e("error on","read random file");
            i.printStackTrace();
        }
    }


}
