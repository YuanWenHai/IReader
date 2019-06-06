package com.will.ireader.printer;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * created  by will on 2019/6/6 16:25
 */
public class Book {


    private String path;
    private String name;
    private long startPosition;

    private RandomAccessFile randomFile;
    private MappedByteBuffer mappedFile;
    private Charset charset = Charset.forName("gbk");




    public Book(String name,String path){
        this.name = name;
        this.path = path;
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

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }


    private void initlaize () {

    }



    @Nullable
    public MappedByteBuffer load(long start, long end){
        if(mappedFile != null) {
            return  mappedFile;
        }
        File file = new File(path);
        start = Math.min(Math.max(start,0),file.length());
        end = Math.min(Math.max(end,0),file.length());

        try {
            randomFile = new RandomAccessFile(file, "r");
            mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY,start,end);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("book","load error");
            return null;
        }
        return mappedFile;
    }

    @Nullable
    public MappedByteBuffer load(){
        return  load(-1,Long.MAX_VALUE);
    }

    public void close() {
        try{
            randomFile.close();

        }catch (IOException i){
            i.printStackTrace();
            Log.e("book","close error");
        }
    }


}
