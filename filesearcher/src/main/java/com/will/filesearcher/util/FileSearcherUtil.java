package com.will.filesearcher.util;

import java.text.DecimalFormat;

/**
 * Created by Will on 2017/11/1.
 */

public class FileSearcherUtil {

    public static String byteSizeFormatter(long size){
        if(size == 0){
            return "0b";
        }
        String[] units = new String[]{"b","kb","mb","gb"};
        double power = Math.log10(1024);
        int level = (int)(Math.log10(size)/power);
        double factor = Math.pow(1024,level);
        return new DecimalFormat("#.##").format((double)size/factor) + units[level];
    }
}
