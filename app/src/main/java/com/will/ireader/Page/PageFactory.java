package com.will.ireader.Page;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Vector;

/**
 * Created by Will on 2016/2/2.
 */
public class PageFactory {
    private int displayHeight,displayWidth;//实际屏幕尺寸
    private int pageHeight,pageWidth;//文字排版页面尺寸
    private int lineNumber;//行数
    private int lineSpace = 3;//行距;
    private int mapperFileLength;//映射到内存中Book的字节数
    private int fontSize = 30;
    private int margin = 30;//文字显示距离屏幕实际尺寸的偏移量
    private Paint myPaint;
    private int begin;//当前阅读的字节数_开始
    private int end;//当前阅读的字节数_结束
    private MappedByteBuffer mappedFile;//映射到内存中的文件
    private RandomAccessFile randomFile;//关闭Random流时使用
    private Vector<String> content = new Vector<String>();
    private boolean isPageDown = true;//转换翻页方向；
    private Bitmap pageBackground;
    public PageFactory(int height,int width,int size){
        displayHeight = height;
        displayWidth = width;
        fontSize = size;
        pageHeight = displayHeight - margin*2 - fontSize;
        pageWidth = displayWidth -margin*2;
        myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setTextSize(fontSize);
        myPaint.setColor(Color.BLACK);
        lineNumber = pageHeight/(fontSize+lineSpace);
    }
    public void openBook(String path,int[] position){
        File file = new File(path);
        mapperFileLength = (int)file.length();
        try {
            randomFile = new RandomAccessFile(file, "r");
            mappedFile = randomFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, (long) mapperFileLength);
        }catch(Exception e){
            e.printStackTrace();
        }
        begin = position[0];
        end = position[1];
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
            strParagraph = new String(byteTemp,"GB2312");
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
                end -= (strParagraph).getBytes("GB2312").length;
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
                strParagraph = new String(byteTemp,"GB2312");
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
                 begin += lines.get(0).getBytes("GB2312").length;
                 lines.remove(0);
             }catch(UnsupportedEncodingException e){
                 e.printStackTrace();
             }
            }
        }
        end = begin;//通过以上一系列运行，得到向上翻页后的第一个position，并将其赋给end，再调用pageDown方法。
        return lines;
    }
    public void printPage(Canvas canvas){
        if( content.size() == 0){
            end = begin;
            content = pageDown();
        }
        if(content.size()>0){
            int y = margin;
            if(pageBackground != null){
                Rect rect = new  Rect(0,0,displayWidth,displayHeight);
                canvas.drawBitmap(pageBackground,null,rect,null);
            }else{
                canvas.drawColor(Color.WHITE);
            }
            for(String line : content){
                y += fontSize+lineSpace;
                canvas.drawText(line,margin,y,myPaint);
            }
            float percent = (float) begin /mapperFileLength*100;
            DecimalFormat format = new DecimalFormat("#0.00");
            String strPercent = format.format(percent);
            int length = (int ) myPaint.measureText(strPercent);
            canvas.drawText(strPercent+"%",(displayWidth-length)/2,displayHeight-margin,myPaint);
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
        float position = percent*mapperFileLength/100;
        end = (int) position;
        nextPage();
    }
    public void setPageBackground(Bitmap bitmap){
        pageBackground = bitmap;
    }

}
