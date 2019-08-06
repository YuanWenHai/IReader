package com.will.ireader.printer;

import android.content.Context;
import android.util.Log;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.will.ireader.worker.AppWorker;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * created  by will on 2019/6/6 16:25
 */
@Entity
public class Book implements Serializable {



    @PrimaryKey(autoGenerate = true)
    private int id;


    private String path;
    private String name;
    private long size;

    private int currentPosition = 0;
    private String charset = "gbk";

    @Ignore
    private  RandomAccessFile randomFile;
    @Ignore
    private  MappedByteBuffer mappedFile;
    @Ignore
    private int byteLength = -1;



    public Book(String name,String path,long size){
        this.name = name;
        this.path = path;
        this.size = size;
    }


    public void initialize(){
        mappedFile = load();
        byteLength = mappedFile.capacity();
    }
    public void initialize(BookInitializeListener listener){
        AppWorker.getInstance().runOnWorkerThread(() -> {
           initialize();
           AppWorker.getInstance().runOnMainThread(listener::onFinish);
        });
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

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getByteLength() {
        if(byteLength == -1){
            throw (new RuntimeException("you must call initialize() before you invoke this method"));
        }
        return byteLength;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    public String getCharset() {
        return charset;
    }
    public void setCharset(String charset) {
        this.charset = charset;
    }


    public int getCurrentPosition() {
        return currentPosition;
    }
    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }


    public MappedByteBuffer bytes() {
       if(mappedFile == null){
           throw (new RuntimeException("you must call initialize() before you invoke this method"));
       }
       return mappedFile;
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
            mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY,start,end);
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
        mappedFile = null;
        if(randomFile == null ){
            return;
        }
        try{
            randomFile.close();
            randomFile = null;

        }catch (IOException i){
            i.printStackTrace();
            Log.e("book","close error");
        }
    }


    public void update(Context context){
        AppWorker.getInstance().runOnWorkerThread(() -> AppDatabase.getInstance(context).bookDao().updateBook(this));
        Log.e("current pos",currentPosition+"");
    }

    interface BookInitializeListener{
        void onFinish();
    }

}
