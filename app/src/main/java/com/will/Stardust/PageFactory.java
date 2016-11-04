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
import com.will.Stardust.file.IReaderDB;

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
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Will on 2016/2/2.
 */
public class PageFactory {
    private int screenHeight, screenWidth;//实际屏幕尺寸
    private int pageHeight,pageWidth;//文字排版页面尺寸
    private int lineNumber;//行数
    private int lineSpace = 3;//行距;
    private int mapperFileLength;//映射到内存中Book的字节数
    private int fontSize ;
    private static final int margin = 30;//文字显示距离屏幕实际尺寸的偏移量
    private Paint myPaint;
    private int begin;//当前阅读的字节数_开始
    private int end;//当前阅读的字节数_结束
    private static MappedByteBuffer mappedFile;//映射到内存中的文件
    private RandomAccessFile randomFile;//关闭Random流时使用

    private Bitmap pageBackground;
    private int position1 = 0;
    private int position2 = 0;
    private String keyWord = "章";
    private IReaderDB iReaderDB ;
    private String bookName;
    private static String wholeString = "none";
    private String code = "GBK";
    private int keywordPos = 0;
    private int nowPos;
    private String searchKey = "";
    private Context mContext;
    private int batteryLevelStringWidth;
    private ArrayList<Integer> chapterPositions;
    public String test;
    public int stringPosition;

    private SPHelper spHelper = SPHelper.getInstance();
    private boolean isNightMode = spHelper.isNightMode();
    private PageView mView;
    private Canvas mCanvas;
    private String batteryLevel = "";
    private List<String> content = new ArrayList<>();
    private BroadcastReceiver batteryReceiver;
    private Book book;
    public PageFactory(PageView view){
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
    public PageFactory(){
    }
    public void openBook(Book book){
        this.book = book;
        begin = spHelper.getBookmarkStart(book.getBookName());
        end = spHelper.getBookmarkEnd(book.getBookName());
        File file = new File(book.getPath());
        mapperFileLength = (int) file.length();
            try {
                randomFile = new RandomAccessFile(file, "r");
                mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, (long) mapperFileLength);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    //向后读取一个段落，返回二进制数组
    private byte[] readParagraphForward( int end){

        byte b0;
        int i = end;
        while(i < mapperFileLength){
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
    while((content.size()<lineNumber) && (end<mapperFileLength)){
        byte[] byteTemp = readParagraphForward(end);
        end += byteTemp.length;
        try{
            strParagraph = new String(byteTemp,code);
        }catch(Exception e){
            e.printStackTrace();
        }
        strParagraph = strParagraph.replaceAll("\r\n","  ");
        strParagraph = strParagraph.replaceAll("\n", "  ");
        while(strParagraph.length()>0){
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
    private List<String>pageUp(){
        String strParagraph = "";
        Vector<String> lines = new Vector<String>();
        while(lines.size()<lineNumber && begin>0){
            Vector<String> parLines = new Vector<String>();
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
                parLines.add(strParagraph.substring(0, size));
                strParagraph = strParagraph.substring(size);
            }
            lines.addAll(0,parLines);
            while(lines.size()>lineNumber){
             try{
                 begin += lines.get(0).getBytes(code).length;
                 lines.remove(0);
             }catch(UnsupportedEncodingException e){
                 e.printStackTrace();
             }
            }
        }
        end = begin;//通过以上一系列运行，得到向上翻页后的第一个position，并将其赋给end，再调用pageDown方法。
        return lines;
    }
    public void printPage(){
        if( content.size() == 0){
            end = begin;
            pageDown();
        }
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
            float percent = (float) begin /mapperFileLength*100;
            DecimalFormat format = new DecimalFormat("#0.00");
            String strPercent = format.format(percent);
            int length = (int ) myPaint.measureText(strPercent);
            mCanvas.drawText(strPercent + "%", (screenWidth - length) / 2, screenHeight - margin, myPaint);

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


            //mCanvas.drawText(batteryLevel,screenWidth-margin-batteryLevelStringWidth,screenHeight-margin,myPaint);

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
        if(end >= mapperFileLength){
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
            pageDown();
        }
        printPage();
    }
    public void saveBookmark(){
        SPHelper.getInstance().setBookmarkEnd(book.getBookName(),end);
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
    public void setPercent(float percent){
        if(percent <= 100){
        float position = percent*mapperFileLength/100;
        end = (int) position;
        if(end ==0) {
            nextPage();
        }else{
            nextPage();
            prePage();
            nextPage();
        }
        }
    }
    public void setPosition(int position){
        end = position;
        nextPage();
    }
    public ArrayList<String> getChapter(){
        ArrayList<String> list = new ArrayList<String>();
        chapterPositions = new ArrayList<Integer>();
        Pattern pattern = Pattern.compile("^[0-9零一二三四五六七八九十百千万 ]+$");
        int index = 0;
        int key;

        do{
            index = wholeString.indexOf("第",index+1);
            key = wholeString.indexOf(keyWord,index);
            if(index != -1 && key != -1){
                Matcher matcher = pattern.matcher(wholeString.substring(index+1,key));
                if(matcher.matches()){
                    list.add(wholeString.substring(index,wholeString.indexOf("\n",index)));
                    chapterPositions.add(index);
                }
            }
        }while(index != -1 && key != -1);
        return list;
    }
    public ArrayList<Integer>getChapterPositions(){
        return chapterPositions;
    }
    public void setKeyWord(String keyWord){
        this.keyWord = keyWord;
    }
    private int getByte(String string){
        byte[] bytes;
        int num = 0;
        try{

            bytes = string.getBytes(code);
            num = bytes.length;
        }catch(Exception e){
            e.printStackTrace();
        }
        return num;
    }
    public int getPositionFromChapter(String chapterName){
        int position = wholeString.indexOf(chapterName);
        String temp = wholeString.substring(0,position);
        int pos = 0;
        try{
            byte[] bytes =temp.getBytes(code);
            pos = bytes.length;
        }catch (UnsupportedEncodingException u){
            u.printStackTrace();
        }
        return pos;
    }
    private int getPositionFromKeyword(String keyword,int p){
        stringPosition = wholeString.indexOf(keyword,p+1);
        if(stringPosition != -1) {
            String temp = wholeString.substring(0, stringPosition);
            int pos = 0;

            try {
                byte[] bytes = temp.getBytes(code);
                pos = bytes.length;
            } catch (UnsupportedEncodingException u) {
                u.printStackTrace();
            }
            return pos;
        }else{
            return stringPosition;
        }
    }
    public void setNightMode(boolean which){
        isNightMode = which;
        printPage( );
    }
    public int searchContent(Canvas canvas,String key,String mode){
        if(mode.equals("content")) {
            searchKey = key;
            keywordPos = getPositionFromKeyword(key,stringPosition);
            if(keywordPos != -1){
            setPosition(keywordPos);
            }else{
                return keywordPos;
            }
        }else{
            keywordPos = getPositionFromKeyword(searchKey,stringPosition);
            if(keywordPos != -1) {
                setPosition(keywordPos);
            }else{
                return keywordPos;
            }
        }
        printPage();
        return 0;
    }
    public void setFontColor(Canvas canvas,int color){
        myPaint.setColor(color);
        printPage();
    }
    public int getCurrentWordNumber(int position){
        if(position>0){
        String temp = "";
        byte[] bytes = new byte[position];
        for(int i = 0;i<position;i++){
            bytes[i] = mappedFile.get(i);
        }
        try{
            temp = new String(bytes,"GBK");
        }catch(UnsupportedEncodingException u){
            u.printStackTrace();
        }
        int number = temp.length();
        return  number;
        }
        return -1;
    }
    public int getBegin(){
        return begin;
    }


    public void close(){
        mappedFile = null;
        mContext.unregisterReceiver(batteryReceiver);
        try{
            randomFile.close();
        }catch (IOException i){
            i.printStackTrace();
        }
    }
}
