package com.will.Stardust.chapter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.will.Stardust.Page.PageFactory;
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
    public static final String KEYWORD_ZHANG = "章 ";
    public static final String KEYWORD_JIE = "节 ";
    public static final String KEYWORD_HUI = "回 ";


    private Book book;
    private MappedByteBuffer mappedByteBuffer;
    private int mappedFileLength;
    private String encoding;
    private String keyword = KEYWORD_ZHANG;
    private ArrayList<Chapter> chapters = new ArrayList<>();
    private final ArrayList<Integer> positions = new ArrayList<>();

    private ProgressCallback progressCallback;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private volatile boolean hasChapters = true;
    private boolean checkHasChapter = true;
    public ChapterFactory() {
        book = PageFactory.getInstance().getBook();
        mappedByteBuffer = PageFactory.getInstance().getMappedFile();
        mappedFileLength = PageFactory.getInstance().getFileLength();
        encoding = PageFactory.getInstance().getEncoding();
    }

    public void getChapterFromFile(final LoadCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("loading","chapters");
                hasChapters = true;
                chapters.clear();
                findParagraphInBytePosition();
                DBHelper.getInstance().deleteChapters(book);
                findChapterParagraphPosition(callback);
            }
        }).start();

    }
    public List<Chapter> getChapterFromDB(){
        return DBHelper.getInstance().getChapters(book.getBookName());
    }

    private void findChapterParagraphPosition(final LoadCallback callback){
        chapters.clear();
        int i = 0;
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(book.getPath())), encoding);
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
            if(chapters.size() == 0){
                hasChapters = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onNotFound();
                    }
                });
                return;
            }
            Log.e("chapters","load completely");


            synchronized (positions){
                Log.e("start","insert data");
                for(int a=0;a<chapters.size();a++){
                    chapter = chapters.get(a);
                    chapter.setChapterBytePosition(positions.get(Math.max(chapter.getChapterParagraphPosition()-1,0)));
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
                   positions.clear();
                   //此处使用全局变量的遍历需要的时间似乎比局部变量更长?不确定.
                   byte[] fileBytes = new byte[mappedFileLength];
                   mappedByteBuffer.get(fileBytes);
                   mappedByteBuffer.position(0);
                   byte lastByte = 1;
                   boolean littleEndian = encoding.contains("LE");
                   for(int i=0;i<mappedFileLength;i++){
                       if(littleEndian){
                           if (fileBytes[i] == 0 && lastByte == 10){
                               positions.add(i-1);
                               if( i % 999 == 0 && progressCallback !=null){
                                   if(!hasChapters){
                                       return;
                                   }
                                   final int percent = i*100/mappedFileLength;
                                   mHandler.post(new Runnable() {
                                       @Override
                                       public void run() {
                                           progressCallback.currentPercentage(percent);
                                       }
                                   });
                               }
                           }
                       }else{
                           if(fileBytes[i] == 0x0a){
                               positions.add(i+1);
                               if( i % 1000 == 0 && progressCallback !=null){
                                   if(!hasChapters){
                                       return;
                                   }
                                   final int percent = i*100/mappedFileLength;
                                   mHandler.post(new Runnable() {
                                       @Override
                                       public void run() {
                                           progressCallback.currentPercentage(percent);
                                       }
                                   });
                               }
                           }
                       }

                       lastByte = fileBytes[i];
                   }
                   Log.e("positions","load completely");
               }
           }
       }).start();
    }
    public void setProgressCallback(ProgressCallback callback){
        progressCallback = callback;
    }
    public void setKeyword(String keyword){
        this.keyword = keyword;
    }
    public interface LoadCallback {
        void onFinishLoad(List<Chapter> list);
        void onNotFound();
    }
    public interface ProgressCallback {
        void currentPercentage(int percent);
    }
}
