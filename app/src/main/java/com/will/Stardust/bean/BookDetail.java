package com.will.Stardust.bean;

/**
 * Created by will on 2016/10/29.
 */

public class BookDetail extends Book {
    private String chapterName;
    private int chapterPosition,chapterNumber;


    public void setChapterName(String chapterName){
        this.chapterName = chapterName;
    }
    public String getChapterName(){
        return  chapterName;
    }

    public void setChapterPosition(int chapterPosition){
        this.chapterPosition = chapterPosition;
    }
    public int getChapterPosition(){
        return chapterPosition;
    }

    public void setChapterNumber(int chapterNumber){
        this.chapterNumber = chapterNumber;
    }
    public int getChapterNumber(){
        return chapterNumber;
    }
}
