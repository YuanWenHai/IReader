package com.will.Stardust.chapter;

import android.util.Log;

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
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by will on 2016/11/5.
 */

public class ChapterFactory {
    private static final String REGULAR_ZHANG = "第[0-9零一二两三四五六七八九十百千万 ]+章 .*?\\s";
    private static final String REGULAR_JIE = "第[0-9零一二两三四五六七八九十百千万 ]+节 .*?\\s";
    private static final String REGULAR_HUI = "第[0-9零一二两三四五六七八九十百千万 ]+回 .*?\\s";


    private static ChapterFactory instance;


    private Book book;

    private List<Chapter> list;

    public static ChapterFactory getInstance(Book book) {
        if (instance == null) {
            instance = new ChapterFactory(book);
        } else {
            instance.book = book;
        }
        return instance;
    }

    public static ChapterFactory getInstance() {
        return instance;
    }

    private ChapterFactory(Book book) {
        this.book = book;
    }

    private List<Chapter> searchChapters(String string, String regularEx) {
        List<Chapter> list = new ArrayList<>();
        Pattern regularPattern = Pattern.compile(regularEx);
        Matcher matcher = regularPattern.matcher(string);
        Chapter chapter;
        Log.e("ready to find", "!");
        while (matcher.find()) {
            Log.e("find", matcher.group());
            chapter = new Chapter();
            chapter.setBookName(book.getBookName());
            chapter.setChapterName(matcher.group());
            chapter.setChapterPosition(matcher.start());
            list.add(chapter);
        }/*
        if(matcher.find()){
            Log.e("group count is",matcher.groupCount()+"");
            Log.e("group is",matcher.group());
            for(int i=0;i<matcher.groupCount();i++){
                chapter = new Chapter();
                chapter.setBookName(book.getBookName());
                chapter.setChapterName(matcher.group(i));
                chapter.setChapterPosition(matcher.start(i));
                chapter.setChapterNumber(i+1);
                list.add(chapter);
            }
        }*/
        return list;
    }

    //这里。。挺蠢的
    public boolean getChapter() {
        List<Chapter> list = DBHelper.getInstance().getChapters(book.getBookName());
        if (list.size() == 0) {
            String book = getBookFromDisk();
            list = searchChapters(book, REGULAR_ZHANG);
            if (list.size() == 0) {
                list = (searchChapters(book, REGULAR_JIE));
                if (list.size() == 0) {
                    list = (searchChapters(book, REGULAR_HUI));
                }
            }
            if (list.size() > 0) {
                DBHelper.getInstance().saveChapters(list);
            }
        }
        this.list = list;
        return list.size() > 0;
    }

    public List<Chapter> getChapterList() {
        return list;
    }

    private String getBookFromDisk() {
        StringBuilder builder = new StringBuilder();
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(new File(book.getPath())), "GBK");
            BufferedReader reader = new BufferedReader(isr);
            String temp;
            while ((temp = reader.readLine()) != null) {
                builder.append(temp);
            }
        } catch (FileNotFoundException f) {
            f.printStackTrace();
            Util.makeToast("未发现" + book.getBookName() + "文件");
        } catch (IOException i) {
            i.printStackTrace();
        }
        return builder.toString();
    }

    public void recycle() {
        instance = null;
    }

    public  void getChapterFromFile() {
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(new File(book.getPath()),"r");
            String temp;
            if ((temp = randomAccessFile.readLine())!= null){
                Log.e("read",temp);
            }else{
                Log.e("read","is null!");
            }
        } catch (FileNotFoundException f) {
            f.printStackTrace();
            Util.makeToast("未发现" + book.getBookName() + "文件");
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
}
