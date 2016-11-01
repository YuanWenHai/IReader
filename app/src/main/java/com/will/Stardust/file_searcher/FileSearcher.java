package com.will.Stardust.file_searcher;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.will.Stardust.bean.Book;

import java.io.File;
import java.util.List;

/**
 * Created by will on 2016/10/29.
 */

public class FileSearcher {
    private Callback mCallback;
    private List<Book> list;
    private Handler handler;
    public FileSearcher(List<Book> list,Callback callback){
        this.list = list;
        mCallback = callback;
        handler = new Handler(Looper.getMainLooper());
    }
    public void startSearch(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                searchTXTFile(Environment.getExternalStorageDirectory());
                if(mCallback != null){
                   handler.post(new Runnable() {
                       @Override
                       public void run() {
                           mCallback.onFinish();
                       }
                   });
                }
            }
        }).start();
    }


    private void searchTXTFile(final File dir){
        if(mCallback != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mCallback.onSearch(dir.getName());
                }
            });
        }
        for(final File file : dir.listFiles()){
            if(!file.isDirectory()){
                if(file.getName().toUpperCase().contains(".TXT")){
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            list.add(new Book(file.getName(),file.getPath()));
                            mCallback.onFind();
                        }
                    });
                }
            }else{
                searchTXTFile(file);
            }
        }
    }
    public interface Callback {
        void onSearch(String pathName);
        void onFind();
        void onFinish();
    }
}
