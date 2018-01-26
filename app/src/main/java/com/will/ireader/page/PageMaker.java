package com.will.ireader.page;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.will.ireader.bean.Book;
import com.will.ireader.common.TaskManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by will on 2018/1/17.
 */

public class PageMaker {
    private Book book;
    private String bookStr;
    private Handler handler = new Handler(Looper.getMainLooper());

    public PageMaker(Book book){
        this.book = book;
    }

    public void parpare(final ReadCallback callback){
        File bookFile = new File(book.getPath());
        if(!bookFile.exists()){
            callback.onBookInvalid();
            return;
        }
        callback.onStart();
        TaskManager.getInstance().execute(new TaskManager.Task() {
            @Override
            public void run(Thread thread) {
                bookStr = read(book);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinish();
                    }
                });
            }
        });
    }

    private void print(Canvas canvas){


    }

    private String read(Book book){
        BufferedReader br;
        try{
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new FileReader(new File(book.getPath())));

            String line;
            while ((line = br.readLine()) != null){
                sb.append(line);
            }
            br.close();
            return sb.toString();
        }catch (FileNotFoundException f){
            Log.e("cannot find file at",book.getPath());
            return null;
        }catch (IOException i){
            Log.e("IOException occurred in","read file in string!");
            return null;
        }
    }

    private File getFileFromPath(String path){
        File file = new File(path);
        return file.exists() ? file : null;
    }
    public interface ReadCallback{
        void onStart();
        void onFinish();
        void onBookInvalid();
    }
}
