package com.will.Stardust;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.util.DisplayMetrics;

import com.will.Stardust.View.PageView;
import com.will.Stardust.bean.Book;
import com.will.Stardust.common.SPHelper;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Will on 2016/2/2.
 */
public class PageFactory {
    private int screenHeight, screenWidth;//实际屏幕尺寸
    private int pageHeight,pageWidth;//文字排版页面尺寸
    private int lineNumber;//行数
    private int lineSpace = 3;//行距;
    private int fileLength;//映射到内存中Book的字节数
    private int fontSize ;
    private static final int margin = 30;//文字显示距离屏幕实际尺寸的偏移量
    private Paint myPaint;
    private int begin;//当前阅读的字节数_开始
    private int end;//当前阅读的字节数_结束
    private MappedByteBuffer mappedFile;//映射到内存中的文件
    private RandomAccessFile randomFile;//关闭Random流时使用

    private String code = "GBK";
    private Context mContext;

    private SPHelper spHelper = SPHelper.getInstance();
    private boolean isNightMode = spHelper.isNightMode();
    private PageView mView;
    private Canvas mCanvas;
    private String batteryLevel = "";
    private ArrayList<String> content = new ArrayList<>();
    private BroadcastReceiver batteryReceiver;
    private Book book;

    private static PageFactory instance;

    public static PageFactory getInstance(PageView view,Book book){
        if(instance == null){
            synchronized (PageFactory.class){
                if(instance == null){
                    instance = new PageFactory(view);
                    instance.openBook(book);
                }
            }
        }
        return instance;
    }
    public static PageFactory getInstance(){
        return instance;
    }
    private PageFactory(PageView view){
        DisplayMetrics metrics = new DisplayMetrics();
        mContext = view.getContext();
        mView = view;

        ((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
        fontSize = spHelper.getFontSize();
        pageHeight = screenHeight - margin*2 - fontSize;
        pageWidth = screenWidth -margin*2;
        lineNumber = pageHeight/(fontSize+lineSpace);

        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setTextSize(fontSize);
        myPaint.setColor(isNightMode ? Color.WHITE : Color.BLACK);

        Bitmap bitmap = Bitmap.createBitmap(screenWidth,screenHeight, Bitmap.Config.ARGB_8888);
        mView.setBitmap(bitmap);
        mCanvas = new Canvas(bitmap);

        registerBatteryReceiver();
    }

    private void openBook(Book book){
        this.book = book;
        begin = spHelper.getBookmarkStart(book.getBookName());
        end = spHelper.getBookmarkEnd(book.getBookName());
        File file = new File(book.getPath());
        fileLength = (int) file.length();
            try {
                randomFile = new RandomAccessFile(file, "r");
                mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, (long) fileLength);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    //向后读取一个段落，返回二进制数组
    private byte[] readParagraphForward( int end){

        byte b0;
        int i = end;
        while(i < fileLength){
            b0 = mappedFile.get(i++);
            if(b0 == 0x0a){
                break;
            }
        }
        int nParaSize = i - end;
        byte[] buf = new byte[nParaSize];
        for (i = 0; i < nParaSize; i++) {
            buf[i] =  mappedFile.get(end + i);
        }
        return buf;

    }
    //向前读取一个段落
    private byte[] readParagraphBack(int begin){
        byte b0 ;
        int i = begin -1 ;
        while(i > 0){
            b0 = mappedFile.get(i);
            if(b0 == 0x0a && i != begin -1 ){
                i++;
                break;
            }
            i--;
        }
        int nParaSize = begin -i ;
        byte[] buf = new byte[nParaSize];
        for (int j = 0; j < nParaSize; j++) {
            buf[j] = mappedFile.get(i + j);
        }
        return buf;

    }
    //获取后一页的内容
private void pageDown(){
    String strParagraph = "";
    while((content.size()<lineNumber) && (end< fileLength)){
        byte[] byteTemp = readParagraphForward(end);
        end += byteTemp.length;
        try{
            strParagraph = new String(byteTemp,code);
        }catch(Exception e){
            e.printStackTrace();
        }
        strParagraph = strParagraph.replaceAll("\r\n","  ");
        strParagraph = strParagraph.replaceAll("\n", "  ");
        while(strParagraph.length   ()>0){
            int size = myPaint.breakText(strParagraph,true,pageWidth,null);
            content.add(strParagraph.substring(0,size));
            strParagraph = strParagraph.substring(size);
            if(content.size() >= lineNumber){
                break;
            }
        }
            if(strParagraph.length()>0){
                try{
                end -= (strParagraph).getBytes(code).length;
            }catch(Exception e){
                    e.printStackTrace();
                }
            }

    }
}
    //上翻页
    private  void pageUp(){
        String strParagraph = "";
        List<String> tempList = new ArrayList<>();
        while(tempList.size()<lineNumber && begin>0){
            byte[] byteTemp = readParagraphBack(begin);
            begin -= byteTemp.length;
            try{
                strParagraph = new String(byteTemp,code);
            }catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }
            strParagraph = strParagraph.replaceAll("\r\n","  ");
            strParagraph = strParagraph.replaceAll("\n","  ");
            while(strParagraph.length() > 0){
                int size = myPaint.breakText(strParagraph,true,pageWidth,null);
                tempList.add(strParagraph.substring(0, size));
                strParagraph = strParagraph.substring(size);
                if(tempList.size() >= lineNumber){
                    break;
                }
            }
            if(strParagraph.length() > 0){
              try{
                  begin+= strParagraph.getBytes(code).length;
              }catch (UnsupportedEncodingException u){
                  u.printStackTrace();
              }
            }
        }
    }
    public void printPage(){
        if(content.size()>0){
            int y = margin;
            if(isNightMode){
                mCanvas.drawColor(Color.BLACK);
                myPaint.setColor(Color.WHITE);
            }else{
                mCanvas.drawColor(Color.rgb(252,236,223));
                myPaint.setColor(Color.BLACK);
            }
            for(String line : content){
                y += fontSize+lineSpace;
                mCanvas.drawText(line,margin,y,myPaint);
            }
            float percent = (float) begin / fileLength *100;
            DecimalFormat format = new DecimalFormat("#0.00");
            String readingProgress = format.format(percent)+"%";
            int length = (int ) myPaint.measureText(readingProgress);
            mCanvas.drawText(readingProgress, (screenWidth - length) / 2, screenHeight - margin, myPaint);

            //显示时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
            String time = simpleDateFormat.format(new Date(System.currentTimeMillis()));
            mCanvas.drawText("Time:"+time,margin, screenHeight -margin,myPaint);

            //显示电量
            float[] widths = new float[batteryLevel.length()];
            float batteryLevelStringWidth = 0;
            myPaint.getTextWidths(batteryLevel, widths);
            for(float f : widths){
                batteryLevelStringWidth += f;
            }
            mCanvas.drawText(batteryLevel, screenWidth - margin - batteryLevelStringWidth, screenHeight - margin, myPaint);
            mView.invalidate();

        }
    }
    private void registerBatteryReceiver(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int scaledLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                batteryLevel = "电量："+String.valueOf(scaledLevel*100/scale);
            }
        };
        mContext.registerReceiver(batteryReceiver,intentFilter);
    }
    public void nextPage(){
        if(end >= fileLength){
            return;
        }else{
            content.clear();
            begin = end;
            pageDown();
        }
        printPage();
    }
    public void prePage(){
        if(begin <= 0){
            return;
        }else{
            content.clear();
            pageUp();
            end = begin;
            pageDown();
        }
        printPage();
    }
    public void saveBookmark(){
        SPHelper.getInstance().setBookmarkEnd(book.getBookName(),begin);
        SPHelper.getInstance().setBookmarkStart(book.getBookName(),begin);
    }
    public void setFontSize(int size){
        if(size < 15){
            return;
        }
        fontSize = size;
        myPaint.setTextSize(fontSize);
        pageHeight =  screenHeight - margin*2 - fontSize;
        lineNumber = pageHeight/(fontSize+lineSpace);
        end = begin;
        nextPage();
        SPHelper.getInstance().setFontSize(size);
    }
    public void increaseFontSize(){
       setFontSize(fontSize+1);
    }
    public void decreaseFontSize(){
        setFontSize(fontSize-1);
    }
    public int getFontSize(){
        return fontSize;
    }
    public int getFileLength(){
        return fileLength;
    }
    public MappedByteBuffer getMappedFile(){
        return mappedFile;
    }

    public void setPosition(int position){
        //begin = position;
        //prePage();
        end = position;
        nextPage();
    }
    public int getProgress(){
        return begin*100/ fileLength;
    }
    public int setProgress(int i){
        int origin = end;
        end = fileLength * i/100;
        if(end == fileLength){
            end--;
        }
        if(end == 0){
            nextPage();
        }else{
            nextPage();
            prePage();
            nextPage();
        }
        return origin;
    }

    public void setNightMode(boolean which){
        isNightMode = which;
        printPage( );
    }
    public Book getBook(){
        return book;
    }
    public String getCode(){
        return code;
    }

    public static void close(){
        if(instance != null){
            instance.mContext.unregisterReceiver(instance.batteryReceiver);
            try{
                instance.randomFile.close();
            }catch (IOException i){
                i.printStackTrace();
            }
            instance = null;
        }
    }
}
