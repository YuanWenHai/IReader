package com.will.filesearcher.searchengine;

import android.os.Environment;

import com.will.filesearcher.util.FileSearcherUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Will on 2017/11/3.
 */

public class FileItem{
    static String storagePath = Environment.getExternalStorageDirectory().getPath() + File.separator;
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd,HH:mm");



    private String name,detail,path;
    private File file;
    private boolean isChecked;

    public FileItem(File file){
        this.file = file;
        this.name = file.getName();
        this.detail = FileSearcherUtil.byteSizeFormatter(file.length())+","+FileItem.formatter.format(new Date(file.lastModified()));
        this.path = file.getPath().replace(storagePath,"");
        isChecked = false;
    }

    public String getName() {
        return name;
    }

    public String getDetail() {
        return detail;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    public boolean isChecked() {
        return isChecked;
    }
    public void setChecked(boolean isChecked){
        this.isChecked = isChecked;
    }
}