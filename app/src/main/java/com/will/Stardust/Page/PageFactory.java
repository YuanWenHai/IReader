package com.will.Stardust.Page;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.BatteryManager;

import com.will.Stardust.file.IReaderDB;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Will on 2016/2/2.
 */
public class PageFactory {
    private int displayHeight,displayWidth;//实际屏幕尺寸
    private int pageHeight,pageWidth;//文字排版页面尺寸
    private int lineNumber;//行数
    private int lineSpace = 3;//行距;
    private int mapperFileLength;//映射到内存中Book的字节数
    private int fontSize ;
    private int margin = 30;//文字显示距离屏幕实际尺寸的偏移量
    private Paint myPaint;
    private int begin;//当前阅读的字节数_开始
    private int end;//当前阅读的字节数_结束
    private static MappedByteBuffer mappedFile;//映射到内存中的文件
    private RandomAccessFile randomFile;//关闭Random流时使用
    private Vector<String> content = new Vector<String>();
    private Bitmap pageBackground;
    private int position1 = 0;
    private int position2 = 0;
    private String keyWord = "章";
    private IReaderDB iReaderDB ;
    private String bookName;
    private static String wholeString = "none";
    private String code = "GBK";
    private boolean isNightMode = false;
    private int keywordPos = 0;
    private int nowPos;
    private String searchKey = "";
    private String level = "";
    private Context context;
    private int width;
    private ArrayList<Integer> chapterPositions;
    public String test;
    public int stringPosition;
    public PageFactory(int height, int width, int size, int fontColor){
        displayHeight = height;
        displayWidth = width;
        fontSize = size;
        pageHeight = displayHeight - margin*2 - fontSize;
        pageWidth = displayWidth -margin*2;
        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setTextSize(fontSize);
        myPaint.setColor(fontColor);
        lineNumber = pageHeight/(fontSize+lineSpace);

    }
    public PageFactory(){
    }
    public void openBook(String path,int[] position){
        begin = position[0];
        end = position[1];
        File file = new File(path);
        mapperFileLength = (int) file.length();
            try {
                randomFile = new RandomAccessFile(file, "r");
                mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, (long) mapperFileLength);
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] bookByte = new byte[mapperFileLength];
                    for (int i = 0; i < mapperFileLength; i++) {
                        bookByte[i] = mappedFile.get(i);
                    }
                    try {
                        wholeString = new String(bookByte, code);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }
    //向后读取一个段落，返回二进制数组
    private byte[] readParagraphForward( int end){
        /*int i = end;
        byte temp ;
        while(i<mapperFileLength){
            temp = mappedFile.get(i++);
            if (temp == 0x0a){
                break;
            }
        }
        int size = i - end ;
        byte[] byteTemp = new byte[size];
        for(i = 0;i<size;i++){
            byteTemp[i] = mappedFile.get(end+i);
        }
        return byteTemp;*/
        byte b0, b1 ;
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
        /*int i = begin - 1;
        byte temp;
        while(i>0){
            temp = mappedFile.get(i);
            if(temp == 0x0a && i != begin -1 ){
                i = i++;
                break;
            }
            i--;
        }
        int size = begin - i;
        byte[] byteTemp = new byte[size];
        for(int j = 0;j<size;j++){
            byteTemp[j] = mappedFile.get(j+i);
        }
        return byteTemp;*/
        // TODO Auto-generated method stub
        byte b0, b1 ;
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
private Vector<String> pageDown(){
    String strParagraph = "";
    Vector<String> lines = new Vector<String>();
    while((lines.size()<lineNumber) && (end<mapperFileLength)){
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
            lines.add(strParagraph.substring(0,size));
            strParagraph = strParagraph.substring(size);
            if(lines.size() >= lineNumber){
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
    return lines;
}
    //上翻页
    private Vector<String>pageUp(){
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
    public void printPage(final Canvas  canvas, Context context){
        if( content.size() == 0){
            end = begin;
            content = pageDown();
        }
        if(content.size()>0){
            int y = margin;
            if(isNightMode){
                canvas.drawColor(Color.BLACK);
            }else{
                if(pageBackground != null){
                    Rect rect = new  Rect(0,0,displayWidth,displayHeight);
                    canvas.drawBitmap(pageBackground,null,rect,null);
                }else{
                    canvas.drawColor(Color.rgb(252,236,223));
                }
            }
            for(String line : content){
                y += fontSize+lineSpace;
                canvas.drawText(line,margin,y,myPaint);
            }
            float percent = (float) begin /mapperFileLength*100;
            DecimalFormat format = new DecimalFormat("#0.00");
            String strPercent = format.format(percent);
            int length = (int ) myPaint.measureText(strPercent);
            canvas.drawText(strPercent + "%", (displayWidth - length) / 2, displayHeight - margin, myPaint);
            //显示时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            canvas.drawText("Time:" + hour + ":" + minute, margin, displayHeight - margin, myPaint);
            //显示电量
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
             BroadcastReceiver receiver  = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    PageFactory.this.context = context;
                    int scaledlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    level = "电量："+String.valueOf(scaledlevel*100/scale);
                    float[] widths = new float[level.length()];
                    width = 0;
                    myPaint.getTextWidths(level, widths);
                    for(int x = 0;x<level.length();x++){
                        width += Math.ceil(widths[x]);
                    }
                    canvas.drawText(level, displayWidth - margin - width, displayHeight - margin, myPaint);
                    context.unregisterReceiver(this);
                }
            };
            context.registerReceiver(receiver, intentFilter);
        }
    }
    public void printPage(Canvas canvas){
        if( content.size() == 0){
            end = begin;
            content = pageDown();
        }
        if(content.size()>0){
            int y = margin;
            if(isNightMode){
                canvas.drawColor(Color.BLACK);
            }else{
                if(pageBackground != null){
                    Rect rect = new  Rect(0,0,displayWidth,displayHeight);
                    canvas.drawBitmap(pageBackground,null,rect,null);
                }else{
                    canvas.drawColor(Color.rgb(252,236,223));
                }
            }
            for(String line : content){
                y += fontSize+lineSpace;
                canvas.drawText(line,margin,y,myPaint);
            }
            float percent = (float) begin /mapperFileLength*100;
            DecimalFormat format = new DecimalFormat("#0.00");
            String strPercent = format.format(percent);
            int length = (int ) myPaint.measureText(strPercent);
            canvas.drawText(strPercent + "%", (displayWidth - length) / 2, displayHeight - margin, myPaint);
            //显示时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            canvas.drawText("Time:"+hour+":"+minute,margin,displayHeight-margin,myPaint);
            //显示已得到的电量
            canvas.drawText(level, displayWidth - margin - width, displayHeight - margin, myPaint);
            //显示电量
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            final BroadcastReceiver receiver  = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    context.unregisterReceiver(this);
                    int scaledlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    level = "电量："+String.valueOf(scaledlevel*100/scale);

                }
            };
            float[] widths = new float[level.length()];
             width = 0;
            myPaint.getTextWidths(level, widths);
            for(int x = 0;x<level.length();x++){
                width += Math.ceil(widths[x]);
            }
            //canvas.drawText(level,displayWidth-margin-width,displayHeight-margin,myPaint);
            context.registerReceiver(receiver,intentFilter);

        }
    }
    public void nextPage(){
        if(end >= mapperFileLength){
            return;
        }else{
            content.clear();
            begin = end;
            content = pageDown();
        }

    }
    public void prePage(){
        if(begin <= 0){
            return;
        }else{
            content.clear();
             pageUp();//可能有错\
            content = pageDown();
        }
    }
    public int[] getPosition(){
        int[] p = {begin,end};
        return p;
    }
    public void setFontSize(int size){
        fontSize = size;
        myPaint.setTextSize(fontSize);
        lineNumber = pageHeight/(fontSize+lineSpace);
        end = begin;
        nextPage();
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
    public void setPageBackground(Canvas canvas,Bitmap bitmap){
        pageBackground = bitmap;
        printPage(canvas);
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
    public void setNightMode(Canvas canvas,boolean which){
        if(which){
            myPaint.setColor(Color.rgb(190, 190, 190));
        }else{
            myPaint.setColor(Color.BLACK);
        }
        isNightMode = which;
        printPage(canvas );
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
        printPage(canvas );
        return 0;
    }
    public void returnToOriginPos(Canvas canvas){
        setPosition(nowPos);
        printPage(canvas );
    }
    public void resetKeywordPos(){
        stringPosition = 0;
    }
    public void saveNowPos(){
        nowPos = begin;
    }
    public void closeStream(){
        if(randomFile != null){
        try{
            randomFile.close();
        }catch (IOException i){
            i.printStackTrace();
        }
        }
    }
    public void setFontColor(Canvas canvas,int color){
        myPaint.setColor(color);
        printPage(canvas);
    }
    public void initializeBook(String path){
        if(wholeString.equals("none")){
        openBook(path);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] bookByte = new byte[mapperFileLength];
                    for(int i = 0;i<mapperFileLength;i++){
                        bookByte[i] = mappedFile.get(i);
                    }
                    try{
                        wholeString = new String(bookByte,code);
                    }catch(UnsupportedEncodingException e){
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }
    public void openBook(String path){
        File file = new File(path);
        mapperFileLength = (int)file.length();
        try {
            randomFile = new RandomAccessFile(file, "r");
            mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, (long) mapperFileLength);
        }catch(Exception e){
            e.printStackTrace();
        }
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
}
