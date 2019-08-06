package com.will.ireader.worker;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * created  by will on 2019/8/1 16:45
 */
public class AppWorker {

    private static AppWorker mInstance;


    private static final String THREAD_NAME = "ireader_worker";
    private HandlerThread mThread;
    private Handler mWorkerHandler;
    private Handler mMainHandler;

    private AppWorker(){
        mThread = new HandlerThread(AppWorker.THREAD_NAME);
        mThread.start();
        mWorkerHandler = new Handler(mThread.getLooper());
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public static AppWorker getInstance(){
        if(mInstance == null){
            synchronized(AppWorker.class){
                if(mInstance == null){
                    mInstance = new AppWorker();
                }
            }
        }
        return mInstance;
    }


    public void runOnWorkerThread(Runnable r){
        mWorkerHandler.post(r);
    }
    public void runOnWorkerThreadDelayed(Runnable r,long delay){
        mWorkerHandler.postDelayed(r,delay);
    }
    public void runOnMainThread(Runnable r){
        mMainHandler.post(r);
    }
    public void runOnMainThreadDelayed(Runnable r,long delay){
        mMainHandler.postDelayed(r,delay);
    }

    public void destroy(){
        mThread.quit();
        mInstance = null;
    }





}
