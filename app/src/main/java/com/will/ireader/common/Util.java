package com.will.ireader.common;

import android.content.Context;
import android.widget.Toast;

import com.will.ireader.base.MyApplication;

import java.util.Locale;

/**
 * Created by will on 2016/10/29.
 */

public class Util {
    private static Toast mToast;
    public static void makeToast(String message){
        if(mToast == null){
            mToast = Toast.makeText(MyApplication.getGlobalContext(),"",Toast.LENGTH_SHORT);
        }
        mToast.setText(message);
        mToast.show();
    }

    public static int getPXFromDP(int dp){
        float density = MyApplication.getGlobalContext().getResources().getDisplayMetrics().density;
        return (int)(dp*density);
    }
    public static int getPXFromDP(Context context,int dp){
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dp*density);
    }

    public static String getFileSizeInUnit(long fileLength){
        String[] units = new String[]{"b","kb","mb","gb"};
        double c = Math.log10(1024);
        int unitIndex = (int) (Math.log10(fileLength)/c);
        double value = fileLength/Math.pow(1024,unitIndex);
        return String.format(Locale.CHINESE,"%.2f"+units[unitIndex],value);
    }
}
