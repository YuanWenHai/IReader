package com.will.ireader.worker;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * created  by will on 2019/8/1 16:45
 */
public class AppWorker {

    private static AppWorker mInstance;


    private static final String THREAD_NAME = "ireader_worker";
    private HandlerThread mThread;
    private Handler mHandler;

    private AppWorker(){
        mThread = new HandlerThread(AppWorker.THREAD_NAME);
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
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

    public Handler getHandler(){
        return mHandler;
    }

    public void destroy(){
        mThread.quit();
        mThread = null;
    }





}
