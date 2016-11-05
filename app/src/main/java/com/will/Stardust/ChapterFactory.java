package com.will.Stardust;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by will on 2016/11/5.
 */

public class ChapterFactory {
    private static final String REGULAR_STR = "第[0-9零一二三四五六七八九十百千万 ]+章 .*[\\s\\S]*?";

    public void getChapters(String string){
        Pattern regularPattern = Pattern.compile(REGULAR_STR);
        Matcher matcher = regularPattern.matcher(string);
        while (matcher.matches()){
           Log.e("matches","!");
        }
        Log.e("match finished","exe");
    }
}
