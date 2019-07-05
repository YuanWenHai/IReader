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




    @Nullable
    public String printLineForward(int targetWidth, Paint paint){
        if(startPos >= book.bytes().capacity()-1){
            return null;
        }

        int lineEnd = startPos;
        byte bytes[];
        String content = "";
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
            if(paint.measureText(paragraph) <= targetWidth){
                return paragraph;
            }


            int textCount = 1;
            for(;;){
                if(textCount == paragraph.length()){
                    break;
                }
                float length = paint.measureText(paragraph,0,textCount);
                if(length > targetWidth){
                    textCount--;
                    content = paragraph.substring(0,textCount);
                    break;
                }
                textCount++;
            }
            int returnedIndex = paragraph.substring(textCount).getBytes(book.getCharset()).length;
            startPos -= returnedIndex;
        }catch (UnsupportedEncodingException u){
            u.printStackTrace();
            Log.e("error on printer","unsupported charset!");
        }
        return content;
    }

    @Nullable
    public String printLineBackward(int textCount){
        if(startPos <= 0){
            return null;
        }
        int lineEnd = startPos;
        byte bytes[];
        String content = "";
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
            textCount = Math.min(textCount,paragraph.length());
            content = paragraph.substring(textCount);
            int returnedIndex = paragraph.substring(0,textCount).getBytes(book.getCharset()).length;
            startPos += returnedIndex;
        }catch (UnsupportedEncodingException u){
            u.printStackTrace();
            Log.e("error on printer","unsupported charset");
        }
        return content;
    }



}
