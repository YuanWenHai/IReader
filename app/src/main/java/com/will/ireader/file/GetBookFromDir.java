package com.will.ireader.file;

import android.content.Context;

import java.io.File;
import java.util.List;

/**
 * Created by Will on 2016/2/1.
 */
public class GetBookFromDir {
    IReaderDB iReaderDB;
    File file;
    public  void dirToBook(Context context){
        iReaderDB = IReaderDB.getInstance(context);
        List<String> list = iReaderDB.getDirPath();
        String[] dirList = (String[])list.toArray();
        for(int i =0;i<dirList.length;i++){
            file = new File(dirList[i]);
            File[] files = file.listFiles();
            recursion(files);
        }
    }
    //递归得到根目录并将txt文件目录与名字加入表格；
    private void recursion(File[] files ){
            if(!(files.length==0)){
                for(int i = 0;i<files.length;i++){
                   if( files[i].isDirectory()){
                       recursion(files[i].listFiles());
                   }else if (files[i].getName().toUpperCase().contains(".txt")){
                       iReaderDB.saveBook(files[i].getName(),files[i].getPath());
                   }
                }
            }
        }

    }

