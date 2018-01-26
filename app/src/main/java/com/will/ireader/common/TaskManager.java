package com.will.ireader.common;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by will on 2018/1/16.
 */

public class TaskManager {

    private ThreadPoolExecutor mExecutor;
    private LinkedBlockingQueue<Runnable> mQueue  = new LinkedBlockingQueue<>();
    private static TaskManager mInstance;

    private final int CORE_SIZE = 2;
    private final int MAX_SIZE = 2;


    public static TaskManager getInstance(){
        if(mInstance == null){
            synchronized(TaskManager.class){
                if(mInstance == null){
                    mInstance = new TaskManager();
                }
            }
        }
        return mInstance;
    }

    private TaskManager(){
        mExecutor = new ThreadPoolExecutor(CORE_SIZE,MAX_SIZE,60,TimeUnit.SECONDS,mQueue);
        mExecutor.allowCoreThreadTimeOut(true);
    }

    public void execute(Task task){
        mExecutor.execute(task);
    }




    public static abstract class Task implements Runnable{
        private Thread mAttachedThread;
        @Override
        public void run() {
            mAttachedThread = Thread.currentThread();
            run(mAttachedThread);
        }
        public abstract void run(Thread thread);

        public void stop(){
            mAttachedThread.interrupt();
        }
    }
}
