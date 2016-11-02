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
