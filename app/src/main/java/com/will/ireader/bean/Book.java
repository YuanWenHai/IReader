package com.will.ireader.bean;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.will.ireader.common.TaskManager;
import com.will.ireader.page.PageMaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by will on 2016/10/29.
 */

public class Book implements Serializable{
    private String name,path,encoding;
    private long accessTime = 0;
    private long startPosition;
    private long endPosition;
    private Handler handler;
    public Book(){}
    public Book(String name,String path){
        this.name = name;
        this.path = path;
    }

    public Book setBookName(String name){
        this.name = name;
        return this;
    }
    public String getBookName(){
        return  name;
    }

    public Book setPath(String path){
        this.path = path;
        return this;
    }
    public String getPath(){
        return path;
    }


    public Book setAccessTime(long time){
        accessTime = time;
        return this;
    }
    public long getAccessTime(){
        return accessTime;
    }

    public void setEncoding(String encoding){
        this.encoding = encoding;
    }
    public String getEncoding(){
        return encoding;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }


    @Override
    public boolean equals(Object o) {
        if(o instanceof Book){
            Book book = (Book) o;
            return book.getPath().equals(this.path);

        } else{
            return super.equals(o);
        }
    }

    public void readBookToMemory(final ReadCallback callback){
        File bookFile = new File(path);
        if(!bookFile.exists()){
            callback.onBookInvalid();
            return;
        }
        if(handler == null){
            handler = new Handler(Looper.getMainLooper());
        }
        callback.onStart();
        TaskManager.getInstance().execute(new TaskManager.Task() {
            @Override
            public void run(Thread thread) {
                final String bookStr = read();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish(bookStr);
                    }
                });
            }
        });
    }

    private String read(){
        BufferedReader br;
        try{
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new FileReader(new File(path)));

            String line;
            while ((line = br.readLine()) != null){
                sb.append(line);
            }
            br.close();
            return sb.toString();
        }catch (FileNotFoundException f){
            Log.e("cannot find file at",path);
            return null;
        }catch (IOException i){
            Log.e("IOException occurred in","read file in string!");
            return null;
        }
    }

    public interface ReadCallback{
        void onStart();
        void onFinish(String bookStr);
        void onBookInvalid();
    }
}
