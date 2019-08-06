package com.will.ireader.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.ColorRes;

import com.will.ireader.base.MyApplication;
import com.will.ireader.bean.Book;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static String getEncoding(Book book){
        UniversalDetector detector = new UniversalDetector(null);
        byte[] bytes = new byte[1024];
        try{
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(book.getPath())));
            int length;
            while ((length = bufferedInputStream.read(bytes)) > 0){
                detector.handleData(bytes,0,length);
            }
            detector.dataEnd();
            bufferedInputStream.close();
        }catch (FileNotFoundException f){
            f.printStackTrace();
        }catch (IOException i){
            i.printStackTrace();
        }
        return detector.getDetectedCharset();
    }
    public static int getPXFromDP(int dp){
        float density = MyApplication.getGlobalContext().getResources().getDisplayMetrics().density;
        return (int)(dp*density);
    }
    public static int getPXFromDP(Context context,int dp){
        float density = context.getResources().getDisplayMetrics().density;
        return (int)(dp*density);
    }
    public static int getColorFromRes(Context context, @ColorRes int res){
        return context.getResources().getColor(res);
    }
    public static ProgressDialog createProgressDialog(Context context,String text){
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(text);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return dialog;
    }

    public static String getFileSizeInUnit(long fileLength){
        String[] units = new String[]{"b","kb","mb","gb"};
        double c = Math.log10(1024);
        int unitIndex = (int) (Math.log10(fileLength)/c);
        double value = fileLength/Math.pow(1024,unitIndex);
        return String.format(Locale.CHINESE,"%.2f"+units[unitIndex],value);
    }
}
