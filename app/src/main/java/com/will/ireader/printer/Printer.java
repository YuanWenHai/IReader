package com.will.ireader.printer;

import android.util.Log;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class Printer {

    private final Book book;
    private RandomAccessFile file;
    private FileReader reader;
    private MappedByteBuffer mappedFile;


    public Printer(Book book) {
        this.book = book;
    }

    private void initialize (){
        this.mappedFile = book.load();
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
