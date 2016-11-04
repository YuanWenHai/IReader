package com.will.Stardust.bean;

import java.io.Serializable;

/**
 * Created by will on 2016/10/29.
 */

public class Book implements Serializable{
    private String name,path;
    private long accessTime = 0;
    public Book(){}
    public Book(String name,String path){
        this.name = name;
        this.path = path;
    }

    public Book setBookName(String name){
        this.name = name;
        return this;
    }
    public String getBookName(){
        return  name;
    }

    public Book setPath(String path){
        this.path = path;
        return this;
    }
    public String getPath(){
        return path;
    }


    public Book setAccessTime(long time){
        accessTime = time;
        return this;
    }
    public long getAccessTime(){
        return accessTime;
    }


    @Override
    public boolean equals(Object o) {
        if(o instanceof Book){
            Book book = (Book) o;
            return book.getBookName().equals(this.name) && book.getPath().equals(this.path);
        }else{
            return super.equals(o);
        }
    }
}
