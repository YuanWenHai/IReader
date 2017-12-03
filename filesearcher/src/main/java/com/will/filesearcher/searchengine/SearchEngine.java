package com.will.filesearcher.searchengine;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.will.filesearcher.filter.FileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Will on 2017/11/1.
 */

public class SearchEngine {
    private final File path;
    private final FileFilter filter;
    private boolean isSearching;
    private volatile boolean stop;
    private SearchEngineCallback callback;
    private CallbackExecutor callbackExecutor;

    public SearchEngine(File path, FileFilter filter){
        this.path = path;
        this.filter = filter;
    }
    public void start(final SearchEngineCallback callback){
        isSearching = true;
        stop = false;
        callbackExecutor = new CallbackExecutor(callback,200);
        new Thread(new Runnable() {
            @Override
            public void run() {
                findFileRecursively(path);
                callbackExecutor.onFinish();
                isSearching = false;
            }
        }).start();
    }
    public void start(){
        if(callback != null){
            start(callback);
        }
    }
    public void stop(){
       stop = isSearching;
    }
    public boolean isSearching(){
        return isSearching;
    }

    public void setCallback(SearchEngineCallback callback){
        this.callback = callback;
    }
    private void findFileRecursively(final File file){
        if(stop || !filter.isShowHidden() && file.getName().startsWith(".")){
            return;
        }
        //Log.d("file name",file.getName());
        if(file.isDirectory() ){
            File[] files = file.listFiles();
            if(files != null){
                callbackExecutor.onSearchDirectory(file);
                for(File f : files){
                    findFileRecursively(f);
                }
            }
        }else{
            if(filter.filter(file)){
                FileItem item = new FileItem(file);
                callbackExecutor.onFind(item);
            }
        }
    }

    public interface SearchEngineCallback{
        void onFind(List<FileItem> fileItems);
        void onSearchDirectory(File file);
        void onFinish();
    }

}
