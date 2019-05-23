package com.will.ireader.page;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.will.ireader.R;
import com.will.ireader.base.MyApplication;
import com.will.ireader.bean.Book;
import com.will.ireader.common.SPHelper;
import com.will.ireader.common.TaskManager;
import com.will.ireader.common.Util;
import com.will.ireader.view.pageview.PageTheme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 * Created by will on 2018/1/18.
 */

public class PageInfo implements Serializable{

    public static final String PAGE_INFO = "page_info";

    public static String bookStr;

    private int rowStartOffset;
    private int rowEndOffset;
    private int contentTopOffset;
    private int contentBottomOffset;
    private int lineSpacing;


    private int fontSize;

    private int startPosition;
    private int nextStartPosition;


    private Book book;

    private  int bottomBarSize;
    private  int bottomBarFontSize;
    private  int bottomBarStartOffset;
    private  int bottomBarEndOffset;

    private int fontMaxSize;
    private int fontMinSize;

    private int fontColor;
    private int backgroundColor;

    public PageInfo(Book book) {
        this.book  = book;
        initParams();
    }

    public int getFontSize() {
        return fontSize;
    }


    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
        SPHelper.getInstance().setFontSize(fontSize);
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
        //SPHelper.getInstance().setBookmarkStart(book.getBookName(),startPosition);
    }

    public int getNextStartPosition() {
        return nextStartPosition;
    }

    public void setNextStartPosition(int nextStartPosition) {
        this.nextStartPosition = nextStartPosition;
        //SPHelper.getInstance().setBookmarkStart(book.getBookName(),nextStartPosition);
    }


    public void prepare(final ReadCallback callback){
        File bookFile = new File(book.getPath());
        if(!bookFile.exists()){
            callback.onBookInvalid();
            return;
        }
        final Handler handler = new Handler(Looper.getMainLooper());
        callback.onStart();
        TaskManager.getInstance().execute(new TaskManager.Task() {
            @Override
            public void run(Thread thread) {
                read();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();
                    }
                });
            }
        });
    }

    private void read(){
        BufferedReader br;
        try{
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(new FileInputStream(book.getPath()),"gbk"));

            String line;
            while ((line = br.readLine()) != null){
                sb.append(line).append("\n");
            }
            br.close();
            PageInfo.bookStr =  sb.toString();
        }catch (FileNotFoundException f){
            Log.e("cannot find file at",book.getPath());
        }catch (IOException i){
            Log.e("IOException occurred in","read file in string!");
        }
    }

    private void initParams(){
        fontSize = SPHelper.getInstance().getFontSize();
        fontMaxSize = Util.getPXFromDP(20);
        fontMinSize = Util.getPXFromDP(5);

        startPosition = SPHelper.getInstance().getBookmarkStart(book.getBookName());

        bottomBarSize = Util.getPXFromDP(11);
        bottomBarFontSize = Util.getPXFromDP(10);
        bottomBarStartOffset = getRowStartOffset();
        bottomBarEndOffset = getRowEndOffset();

        boolean nightMode = SPHelper.getInstance().isNightMode();
        int textColorRes = nightMode ? R.color.nightModeTextColor : R.color.dayModeTextColor;
        fontColor = MyApplication.getGlobalContext().getResources().getColor(textColorRes);
        int backgroundColorRes = nightMode ? R.color.nightModeBackgroundColor : R.color.dayModeBackgroundColor;
        backgroundColor = MyApplication.getGlobalContext().getResources().getColor(backgroundColorRes);
    }

    public int getRowStartOffset() {
        return rowStartOffset;
    }

    public void setRowStartOffset(int rowStartOffset) {
        this.rowStartOffset = rowStartOffset;
    }

    public int getRowEndOffset() {
        return rowEndOffset;
    }

    public void setRowEndOffset(int rowEndOffset) {
        this.rowEndOffset = rowEndOffset;
    }

    public int getContentTopOffset() {
        return contentTopOffset;
    }

    public void setContentTopOffset(int contentTopOffset) {
        this.contentTopOffset = contentTopOffset;
    }

    public int getContentBottomOffset() {
        return contentBottomOffset;
    }

    public void setContentBottomOffset(int contentBottomOffset) {
        this.contentBottomOffset = contentBottomOffset;
    }

    public int getLineSpacing() {
        return lineSpacing;
    }

    public void setLineSpacing(int lineSpacing) {
        this.lineSpacing = lineSpacing;
    }

    public int getBottomBarSize() {
        return bottomBarSize;
    }

    public int getBottomBarFontSize() {
        return bottomBarFontSize;
    }

    public int getBottomBarStartOffset() {
        return bottomBarStartOffset;
    }

    public int getBottomBarEndOffset() {
        return bottomBarEndOffset;
    }

    public int getFontMaxSize() {
        return fontMaxSize;
    }

    public int getFontMinSize() {
        return fontMinSize;
    }

    public Book getBook() {
        return book;
    }

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public interface ReadCallback{
        void onStart();
        void onSuccess();
        void onBookInvalid();
    }
}
