package com.will.ireader.printer;

import android.graphics.Paint;
import android.util.Log;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Printer {

    private final Book book;

    //当下，在文件bytes中的指针位置
    private int currentPos;

    private int pageStartPos;
    private long endPos;


    public Printer(Book book) {
        this.book = book;
    }





    public String[] printPageForward(int availableWidth,int availableHeight,Paint paint){
        pageStartPos = currentPos;
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
        if(currentPos >= book.bytes().capacity()){
            return "";
        }

        int pEnd = currentPos;
        while (pEnd < book.bytes().capacity()){
                if(book.bytes().get(pEnd) == 0x0a){
                    pEnd += 1; //将换行符(0x0a)一同加入段落,pEnd即currentPos总是指向下一段段首
                    break;
            }
            pEnd++;
        }
        int length = pEnd - currentPos;
        byte[] bytes = new byte[length];
        for(int i=0;i<length;i++){
            bytes[i] = book.bytes().get(currentPos +i);
        }
        currentPos = pEnd;

        String line = "";
        try{
            String paragraph = new String(bytes,Charset.forName(book.getCharset()));
            int textCount = paint.breakText(paragraph,true,availableWidth,null);
            line = paragraph.substring(0,textCount);
            String remain = paragraph.substring(textCount);
            if(remain.length() > 0){
                int returnedIndex = remain.getBytes(book.getCharset()).length;
                currentPos -= returnedIndex;
            }
        }catch (UnsupportedEncodingException u){
            u.printStackTrace();
            Log.e("error on printer","unsupported charset!");
        }
        return line;
    }


    private void movePosToPreviousPage(int availableWidth,int availableHeight,Paint paint){
        if(currentPos <= 0){
            currentPos = 0;
            return;
        }

        int rowCount = (int)(availableHeight/paint.getTextSize());
        ArrayList<String> lines = new ArrayList<>();
        while(lines.size() < rowCount){

            int pEnd = currentPos;
            while (pEnd > 0){
                if((book.bytes().get(pEnd) == 10 && currentPos-pEnd !=1)  || currentPos == 0){
                    pEnd += 1;//指针后移一位，不将上一段换行符加入本段
                    break;
                }
                pEnd--;
            }
            int length = currentPos - pEnd;
            byte[] bytes = new byte[length];
            for(int i=0;i<length;i++){
                bytes[i] = book.bytes().get(pEnd+i);
            }
            currentPos = pEnd;


            try{
                String paragraph = new String(bytes,book.getCharset());
                List<String> temp = new ArrayList<>();
                while(paragraph.length() > 0){
                    int lineCount = paint.breakText(paragraph,true,availableWidth,null);
                    temp.add(paragraph.substring(0,lineCount));
                    paragraph = paragraph.substring(lineCount);
                }
                lines.addAll(0,temp);
                if(lines.size() > rowCount){

                }

                if(paragraph.length() > 0){
                    currentPos += paragraph.getBytes(book.getCharset()).length;
                }

            }catch (UnsupportedEncodingException u){
                u.printStackTrace();
                Log.e("error on printer","unsupported charset");
            }
        }
    }



}
