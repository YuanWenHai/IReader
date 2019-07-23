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
    private int pageEndPos;

    private int pageStartPos;


    public Printer(Book book) {
        this.book = book;
        this.pageStartPos = book.getStartPosition();
    }





    public String[] printPageForward(int availableWidth,int availableHeight,Paint paint){
        pageStartPos = pageEndPos;
        int rowCount = (int)(availableHeight/paint.getTextSize());
        String[] pageContent = new String[rowCount];
        for(int i=0;i<pageContent.length;i++){
            pageContent[i] = printLineForward(availableWidth,paint);
        }
        book.setStartPosition(pageStartPos);
        return  pageContent;
    }
    public String[] printPageBackward(int availableWidth,int availableHeight,Paint paint){
        pageEndPos = findPreviousPageStart(availableWidth,availableHeight,paint);
        return printPageForward(availableWidth,availableHeight,paint);
    }
    public String[] reprintCurrentPage(int availableWidth,int availableHeight,Paint paint){
        pageEndPos = pageStartPos;
        return printPageForward(availableWidth,availableHeight,paint);
    }





    private String printLineForward(int availableWidth, Paint paint){
        if(pageEndPos >= book.bytes().capacity()){
            return "";
        }

        int pEnd = pageEndPos;
        while (pEnd < book.bytes().capacity()){
                if(book.bytes().get(pEnd) == 0x0a){
                    pEnd += 1; //将换行符(0x0a)一同加入段落,pEnd即currentPos总是指向下一段段首
                    break;
            }
            pEnd++;
        }
        int length = pEnd - pageEndPos;
        byte[] bytes = new byte[length];
        for(int i=0;i<length;i++){
            bytes[i] = book.bytes().get(pageEndPos +i);
        }
        pageEndPos = pEnd;

        String line = "";
        try{
            String paragraph = new String(bytes,Charset.forName(book.getCharset()));
            int textCount = paint.breakText(paragraph,true,availableWidth,null);
            line = paragraph.substring(0,textCount);
            String remain = paragraph.substring(textCount);
            if(remain.length() > 0){
                int returnedIndex = remain.getBytes(book.getCharset()).length;
                pageEndPos -= returnedIndex;
            }
        }catch (UnsupportedEncodingException u){
            u.printStackTrace();
            Log.e("error on printer","unsupported charset!");
        }
        return line;
    }


    private int findPreviousPageStart(int availableWidth, int availableHeight, Paint paint){
        if(pageStartPos <= 0){
            return 0 ;
        }
        int targetPos = pageStartPos;

        int rowCount = (int)(availableHeight/paint.getTextSize());
        ArrayList<String> lines = new ArrayList<>();
        while(lines.size() < rowCount){

            int pEnd = targetPos;
            while (pEnd > 0){
                if((book.bytes().get(pEnd) == 10 && targetPos-pEnd !=1)  || targetPos == 0){
                    pEnd += 1;//指针后移一位，不将上一段换行符加入本段
                    break;
                }
                pEnd--;
            }
            int length = targetPos - pEnd;
            byte[] bytes = new byte[length];
            for(int i=0;i<length;i++){
                bytes[i] = book.bytes().get(pEnd+i);
            }
            targetPos = pEnd;


            try{
                String paragraph = new String(bytes,book.getCharset());
                List<String> temp = new ArrayList<>();
                while(paragraph.length() > 0){
                    int lineCount = paint.breakText(paragraph,true,availableWidth,null);
                    temp.add(paragraph.substring(0,lineCount));
                    paragraph = paragraph.substring(lineCount);
                }
                lines.addAll(0,temp);
                //段落中未显示的行，将回退
                List<String> temp2 = new ArrayList<>();
                StringBuilder remain = new StringBuilder();
                //因为是上翻页，故从段首起将多余的行移除回退
                while (lines.size() > rowCount){
                    temp2.add(lines.remove(0));
                }

                for (String s: temp2){
                    remain.append(s);
                }

                String remainStr = remain.toString();
                if(remainStr.length() > 0){
                    targetPos += remainStr.getBytes(book.getCharset()).length;
                }

            }catch (UnsupportedEncodingException u){
                u.printStackTrace();
                Log.e("error on printer","unsupported charset");
            }
        }
        return targetPos;
    }


    /**
     * return current book progress in percentage
     * @return percentage
     */
    public float getProgress(){
        float c = (float) pageEndPos;
        float f = (float) book.bytes().capacity();
        return (c/f)*100;
    }



}
