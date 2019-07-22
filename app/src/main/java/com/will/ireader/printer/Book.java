package com.will.ireader.printer;

import android.support.annotation.Nullable;
import android.util.Log;

import com.will.ireader.common.SPHelper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * created  by will on 2019/6/6 16:25
 */
public class Book implements Serializable {


    private String path;
    private String name;


    private int startPosition = -1;
    private int byteLength = -1;
    private RandomAccessFile randomFile;
    private static MappedByteBuffer mappedFile;
    private String charset;




    public Book(String name,String path){
        this.name = name;
        this.path = path;
    }


    public void initialize(){
        startPosition = SPHelper.getInstance().getBookmarkStart(path);
        charset = SPHelper.getInstance().getBookCharset(path,"gbk");
        Book.mappedFile = load();
        byteLength = Book.mappedFile.capacity();
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    public int getByteLength() {
        check();
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public String getCharset() {
        check();
        return charset;
    }
    public void setCharset(String charset) {
        this.charset = charset;
        SPHelper.getInstance().setBookCharset(getPath(),charset);
    }


    public int getStartPosition() {
        check();
        return startPosition;
    }
    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
        SPHelper.getInstance().setBookmarkStart(getPath(),startPosition);
    }


    public MappedByteBuffer bytes() {
       check();
       return Book.mappedFile;
    }

    private MappedByteBuffer load(long start, long end){
        if(mappedFile != null) {
            return  mappedFile;
        }
        File file = new File(path);
        start = Math.min(Math.max(start,0),file.length());
        end = Math.min(Math.max(end,0),file.length());

        try {
            randomFile = new RandomAccessFile(file, "r");
            this.mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY,start,end);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("book","load error");
            return null;
        }
        return mappedFile;
    }

    private MappedByteBuffer load(){
        return  load(-1,Long.MAX_VALUE);
    }

    public void close() {
        Book.mappedFile = null;
        if(randomFile == null ){
            return;
        }
        try{
            randomFile.close();

        }catch (IOException i){
            i.printStackTrace();
            Log.e("book","close error");
        }
    }

    private void check(){
        if(startPosition == -1 ||byteLength == -1 || charset == null || Book.mappedFile == null){
            throw (new RuntimeException("you must call initialize() before you invoke this method"));
        }
    }

}
