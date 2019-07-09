package com.will.ireader.printer;

import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.Log;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Printer {

    private final Book book;

    private int startPos;
    private long endPos;


    public Printer(Book book) {
        this.book = book;
    }





    public String[] printPageForward(int availableWidth,int availableHeight,Paint paint){
        int rowCount = (int)(availableHeight/paint.getTextSize());
        String[] pageContent = new String[rowCount];
        for(int i=0;i<pageContent.length;i++){
            pageContent[i] = printLineForward(availableWidth,paint);
        }
        return  pageContent;
    }
    public String[] printPageBackward(int availableWidth,int availableHeight,Paint paint){
        movePosToPreviousPage(availableWidth,availableHeight,paint);
        return printPageForward(availableWidth,availableHeight,paint);
    }





    private String printLineForward(int availableWidth, Paint paint){
        if(startPos >= book.bytes().capacity()-1){
            return "";
        }
        int lineEnd = startPos;
        byte[] bytes;
        String line = "";
        for(;;){
                if((lineEnd != startPos && book.bytes().get(lineEnd) == 10) || lineEnd == book.bytes().capacity()-1){
                int length = lineEnd - startPos;
                bytes = new byte[length];
                for(int i=0;i<length;i++){
                    bytes[i] = book.bytes().get(startPos+i);
                }
                startPos = lineEnd;
                break;
            }
            lineEnd++;
        }
        try{
            String paragraph = new String(bytes,Charset.forName(book.getCharset()));
            int textCount = paint.breakText(paragraph,true,availableWidth,null);
            line = paragraph.substring(0,textCount);
            String remain = paragraph.substring(textCount);
            if(remain.length() > 0){
                int returnedIndex = remain.getBytes(book.getCharset()).length;
                startPos -= returnedIndex;
            }
        }catch (UnsupportedEncodingException u){
            u.printStackTrace();
            Log.e("error on printer","unsupported charset!");
        }
        return line;
    }


    // TODO: 2019/7/9  考虑不足一页时的问题
    private void movePosToPreviousPage(int availableWidth,int availableHeight,Paint paint){

        int rowCount = (int)(availableHeight/paint.getTextSize());
        for(int a=0;a<rowCount;a++){
            int lineEnd = startPos;
            byte[] bytes;
            for(;;){
                if((book.bytes().get(lineEnd) == 10 && lineEnd != startPos) || startPos == 0){
                    int length = startPos - lineEnd;
                    bytes = new byte[length];
                    for(int i=0;i<length;i++){
                        bytes[i] = book.bytes().get(lineEnd+i);
                    }
                    startPos = lineEnd;
                    break;
                }
                lineEnd--;
            }
            try{
                String paragraph = new String(bytes,book.getCharset());
                int textCount = paint.breakText(paragraph,true,availableWidth,null);
                String line = paragraph.substring(0,textCount);
                String remain = paragraph.substring(textCount);
                if(remain.length() > 0){
                    startPos += remain.getBytes(book.getCharset()).length;
                }
            }catch (UnsupportedEncodingException u){
                u.printStackTrace();
                Log.e("error on printer","unsupported charset");
            }
        }



    }



}
