package com.will.Stardust.common;

import android.util.Log;
import android.widget.Toast;

import com.will.Stardust.base.MyApplication;
import com.will.Stardust.bean.Book;

import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
        String encoding = SPHelper.getInstance().getBookEncoding(book);
        if(encoding.isEmpty()){

            UniversalDetector detector = new UniversalDetector(null);
            byte[] bytes = new byte[8096];
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
            encoding = detector.getDetectedCharset();
            SPHelper.getInstance().setBookEncoding(book,encoding);
        }
        Log.e(book.getBookName(),encoding);
        return encoding;


    }
}
