package com.will.Stardust.bean;

/**
 * Created by will on 2016/10/29.
 */

public class Book {
    private String name,path;
    public Book(){}
    public Book(String name,String path){
        this.name = name;
        this.path = path;
    }

    public void setBookName(String name){
        this.name = name;
    }
    public String getBookName(){
        return  name;
    }

    public void setPath(String path){
        this.path = path;
    }
    public String getPath(){
        return path;
    }
}
