package com.will.Stardust.chapter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.will.Stardust.PageFactory;
import com.will.Stardust.bean.Book;
import com.will.Stardust.bean.Chapter;
import com.will.Stardust.common.Util;
import com.will.Stardust.db.DBHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 2016/11/5.
 */

public class ChapterFactory {
    private static final String REGULAR_ZHANG = "第[0-9零一二两三四五六七八九十百千万 ]+章 .*?\\s";
    private static final String REGULAR_JIE = "第[0-9零一二两三四五六七八九十百千万 ]+节 .*?\\s";
    private static final String REGULAR_HUI = "第[0-9零一二两三四五六七八九十百千万 ]+回 .*?\\s";



    private Book book;
    private MappedByteBuffer mappedByteBuffer;
    private int mappedFileLength;
    private String code;
    private String keyword = "章 ";
    private ArrayList<Chapter> chapters;
    private final ArrayList<Integer> positions = new ArrayList<>();

    //private volatile boolean stopThread;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public ChapterFactory() {
        book = PageFactory.getInstance().getBook();
        mappedByteBuffer = PageFactory.getInstance().getMappedFile();
        mappedFileLength = PageFactory.getInstance().getFileLength();
        code = PageFactory.getInstance().getCode();
    }

    public void getChapter(final LoadCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                chapters = DBHelper.getInstance().getChapters(book.getBookName());
                if (chapters.size() != 0) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFinishLoad(chapters);
                        }
                    });
                }else{
                    Log.e("loading","chapters");
                    findParagraphInBytePosition();
                    findChapterParagraphPosition(callback);
                }
            }
        }).start();

    }

    private void findChapterParagraphPosition(final LoadCallback callback){
            int i = 0;
            try {
                InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(book.getPath())),code);
                BufferedReader reader = new BufferedReader(isr);
                String temp;
                Chapter chapter;
                Log.e("chapters","start loading");
                while ((temp = reader.readLine()) != null) {

                    if(temp.contains("第")&&temp.contains(keyword)){
                        chapter = new Chapter();
                        chapter.setChapterName(temp);
                        chapter.setBookName(book.getBookName());
                        chapter.setChapterParagraphPosition(i);
                        chapters.add(chapter);
                        //Log.e("chapter name",chapter.getChapterName());
                    }
                    i++;
                }
                Log.e("chapters","load completely");


                synchronized (positions){
                    Log.e("start","insert data");
                    for(int a=0;a<chapters.size();a++){
                        chapter = chapters.get(a);
                        chapter.setChapterBytePosition(positions.get(chapter.getChapterParagraphPosition()-1));
                        //Log.e("chapter position",chapter.getChapterBytePosition()+"");
                    }
                }
                Log.e("insert","completely");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFinishLoad(chapters);
                    }
                });
                DBHelper.getInstance().saveChapters(chapters);
            } catch (FileNotFoundException f) {
                f.printStackTrace();
                Util.makeToast("未发现" + book.getBookName() + "文件");
            } catch (IOException e) {
                e.printStackTrace();
            }

    }
    private void findParagraphInBytePosition(){
       new Thread(new Runnable() {
           @Override
           public void run() {
               synchronized (positions){
                   Log.e("positions","start loading");
                   byte[] bytes = new byte[mappedFileLength];
                   mappedByteBuffer.get(bytes);
                   for(int i=0;i<mappedFileLength;i++){
                       if(bytes[i] == 0x0a){
                           positions.add(i);
                       }
                   }
                   Log.e("positions","load completely");
               }
           }
       }).start();
    }
    interface LoadCallback {
        void onFinishLoad(List<Chapter> list);
    }
}
