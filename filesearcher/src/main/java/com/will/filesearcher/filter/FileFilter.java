package com.will.filesearcher.filter;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Will on 2017/11/1.
 */

public class FileFilter implements Serializable{
    private long minimumSize = 0;
    private long maxSize = -1;
    private String extension = null;
    private String keyword = null;
    private boolean showHidden;
    public void withSizeLimit(long minimum, long max){
        minimumSize = minimum;
        maxSize = max;
    }
    public void withExtension(String extension){
        this.extension = extension;
    }
    public void withKeyword(String keyword){
        this.keyword = keyword;
    }
    public void showHidden(boolean showHidden){
        this.showHidden = showHidden;
    }
    public boolean filter(File file){
        //Log.d("fileFilter",file.getName());
        return sizeFilter(file) && extensionFilter(file) && keywordFilter(file) && hiddenFileFilter(file);
    }
    public boolean isShowHidden(){
        return showHidden;
    }
    private boolean sizeFilter(File file){
        boolean sizeResult;
        sizeResult = file.length() >= minimumSize;
        if(maxSize > -1){
            sizeResult = sizeResult && file.length() <= maxSize;
        }
        return sizeResult;
    }
    private boolean extensionFilter(File file){
        if(extension == null || extension.isEmpty()){
            return true;
        }
        String fileName = file.getName();
        return fileName.contains(".") && fileName.substring(fileName.lastIndexOf(".")+1,fileName.length()).toUpperCase().equals(extension.toUpperCase());
    }
    private boolean keywordFilter(File file){
        if(keyword == null || keyword.isEmpty() ){
            return true;
        }
        String fileName = file.getName();
        return fileName.toUpperCase().contains(keyword.toUpperCase());
    }
    private boolean hiddenFileFilter(File file){
        return showHidden || !file.getName().startsWith(".");
    }
}
